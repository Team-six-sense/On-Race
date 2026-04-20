"""티켓팅 매크로
서버 시간 기반 정밀 타이밍으로 버튼 순서 클릭 + VQA 자동 해결.
"""
import random
import shutil
import time
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from pathlib import Path

import pyautogui
import requests
import undetected_chromedriver as uc
from dotenv import load_dotenv
import os

load_dotenv(Path(__file__).parent / "config.env")
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from lib import (
    BezierMoverPyautogui,
    VQASolver,
    apply_proxy_to_options,
    load_proxies,
    pick_random_proxy,
)

# ── 설정 ──────────────────────────────────────────────────────────────────────

TARGET_URL     = os.getenv("TARGET_URL", "")
TARGET_TIME    = os.getenv("TARGET_TIME", "")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")

PROXIES_FILE = Path(__file__).parent / "proxies.txt"

VQA_PROMPT = (
    "영상을 분석해서 제시된 문제를 풀어 오로지 답만 출력해\n"
    "어떤 선수가 어떤 아이템을 밟았는지 기억해\n"
    "결과창의 lane을 비교해서 선수들의 기록을 매칭해\n"
    "아이템이 존재하는데, 아이템은 총 2가지야\n"
    "바나나 껍질을 밟으면 속도가 느려져\n"
    "파란색 화살표를 밟으면 속도가 빨라져"
)

VQA_RECORD_DURATION = 7  # 재생 버튼 누른 후 녹화 시간 (초)

# 클릭 순서:
# - 문자열            → 해당 텍스트 버튼 클릭
# - "체크박스"        → 미체크 체크박스 전체 클릭
# - 숫자(int/float)   → 해당 초만큼 대기
# - ("vqa", 초)       → 해당 초 이내에 /vqa URL 감지되면 VQA 진행, 아니면 다음으로
BUTTON_SEQUENCE = [
    "다음 단계로",
    "확인",
    "체크박스",
    "확인",
    ("vqa", 2),
]

VQA_KEYWORD = "vqa"

CHECKBOX_KEYWORD = "체크박스"

# VQA 관련
VQA_URL_SUFFIX  = "/vqa"
VQA_VIDEO_SEL   = "video"           # 재생 버튼 못 찾으면 영상 위치 클릭 (pointer-events-none → 부모 전달)
VQA_RADIO_SEL   = "button[role='radio']"   # 라디오 선택지
VQA_SUBMIT_TEXT = "제출하기"

CLICK_HOLD_MIN = 0.05   # 50ms
CLICK_HOLD_MAX = 0.15   # 150ms

DISABLE_ANIM = """
var s = document.createElement('style'); s.id = '__no_anim__';
s.textContent = '*{animation-duration:0ms!important;transition-duration:0ms!important}';
if (!document.getElementById('__no_anim__')) document.head.appendChild(s);
"""

# ── 서버 시간 동기화 ───────────────────────────────────────────────────────────

def get_server_offset(url: str, n: int = 5) -> float:
    """HTTP Date 헤더로 서버-로컬 시간 차이(초) 측정. RTT/2 보정."""
    offsets = []
    for _ in range(n):
        try:
            t0 = time.perf_counter()
            resp = requests.head(url, timeout=5)
            rtt = time.perf_counter() - t0
            server_dt = parsedate_to_datetime(resp.headers["Date"]).astimezone(timezone.utc)
            local_dt  = datetime.now(timezone.utc)
            offsets.append((server_dt - local_dt).total_seconds() + rtt / 2)
        except Exception as e:
            print(f"  [경고] 서버 시간 측정 실패: {e}")
        time.sleep(0.1)
    if not offsets:
        print("  [경고] 서버 시간 측정 전부 실패 → offset=0 사용")
        return 0.0
    offset = sum(offsets) / len(offsets)
    print(f"  서버 오프셋: {offset:+.3f}s (샘플 {len(offsets)}개)")
    return offset


def wait_until(target_utc: datetime, offset: float) -> None:
    """target_utc까지 고정밀 대기. offset = 서버-로컬 차이."""
    while True:
        remaining = (target_utc - datetime.now(timezone.utc)).total_seconds() - offset
        if remaining <= 0:
            break
        elif remaining > 1.0:
            time.sleep(remaining - 0.5)
        elif remaining > 0.05:
            time.sleep(0.005)
        # 50ms 이내: busy-wait

# ── 브라우저 ──────────────────────────────────────────────────────────────────

def make_driver() -> uc.Chrome:
    options = uc.ChromeOptions()
    chosen = pick_random_proxy(load_proxies(PROXIES_FILE))
    ext_dir = None
    if chosen:
        visible = chosen.split("@")[-1]  # 자격 증명 감추고 host:port만 표시
        print(f"  [프록시] 선택: {visible}")
        ext_dir = apply_proxy_to_options(options, chosen)
    else:
        print("  [프록시] 사용 안 함 (proxies.txt 없음/비어 있음)")
    driver = uc.Chrome(options=options, version_main=146)
    driver.maximize_window()
    driver._proxy_ext_dir = ext_dir
    return driver


def screen_pos(driver, element) -> dict:
    """요소 중심의 OS 절대 화면 좌표 반환."""
    return driver.execute_script("""
        var r = arguments[0].getBoundingClientRect();
        return {
            x: Math.round(r.left + r.width  / 2 + window.screenX + (window.outerWidth  - window.innerWidth)  / 2),
            y: Math.round(r.top  + r.height / 2 + window.screenY + (window.outerHeight - window.innerHeight))
        };
    """, element)


def wait_el(driver, css: str, timeout: float = 5):
    return WebDriverWait(driver, timeout, poll_frequency=0.001).until(
        EC.element_to_be_clickable((By.CSS_SELECTOR, css))
    )


def find_button_by_text(driver, text: str, timeout: float = 5):
    """버튼 텍스트(부분 일치)로 클릭 가능한 버튼 요소 반환."""
    xpath = f"//button[contains(normalize-space(.), '{text}')]"
    return WebDriverWait(driver, timeout, poll_frequency=0.001).until(
        EC.element_to_be_clickable((By.XPATH, xpath))
    )


def click_checkboxes(driver, mover: BezierMoverPyautogui):
    """페이지에 존재하는 미체크 체크박스를 모두 클릭."""
    checkboxes = driver.execute_script("""
        return Array.from(document.querySelectorAll(
            'input[type="checkbox"]:not(:checked), button[role="checkbox"][aria-checked="false"]'
        )).filter(el => {
            var r = el.getBoundingClientRect();
            return r.width > 0 && r.height > 0;
        });
    """)
    for cb in checkboxes:
        pos = screen_pos(driver, cb)
        mover.move_to(pos["x"], pos["y"])
        pyautogui.mouseDown(_pause=False)
        time.sleep(random.uniform(CLICK_HOLD_MIN, CLICK_HOLD_MAX))
        pyautogui.mouseUp(_pause=False)
        print(f"    [체크박스] 클릭")

# ── 클릭 ──────────────────────────────────────────────────────────────────────

def click_by_text(driver, mover: BezierMoverPyautogui, text: str, timeout: float = 5):
    """버튼 텍스트로 찾아 Bezier 이동 후 클릭."""
    el  = find_button_by_text(driver, text, timeout)
    pos = screen_pos(driver, el)
    mover.move_to(pos["x"], pos["y"])
    pyautogui.mouseDown(_pause=False)
    time.sleep(random.uniform(CLICK_HOLD_MIN, CLICK_HOLD_MAX))
    pyautogui.mouseUp(_pause=False)

# ── VQA ───────────────────────────────────────────────────────────────────────

def check_and_solve_vqa(driver, mover: BezierMoverPyautogui, solver: VQASolver) -> bool:
    """VQA 페이지 감지 시 재생 버튼 클릭 → 녹화 → Gemini 전송 → 답 입력. 처리 여부 반환."""
    if not driver.current_url.endswith(VQA_URL_SUFFIX):
        return False

    print("  [VQA] 감지됨")

    # 영상 bounding box 취득 (crop용)
    crop = None
    video_el = None
    try:
        video_el = WebDriverWait(driver, 5, poll_frequency=0.05).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, VQA_VIDEO_SEL))
        )
        box = driver.execute_script(
            "var r=arguments[0].getBoundingClientRect();"
            "return {x:Math.round(r.left),y:Math.round(r.top),w:Math.round(r.width),h:Math.round(r.height)};",
            video_el
        )
        crop = (box["x"], box["y"], box["w"], box["h"])
        print(f"  [VQA] 영상 영역: {crop}")
    except TimeoutException:
        print("  [VQA] 영상 요소 미발견 — 전체 화면 녹화")

    # 문제 텍스트 추출
    question_text = ""
    try:
        q_el = driver.find_element(By.CSS_SELECTOR, "h2, h3, .vqa-question, p.question")
        question_text = q_el.text.strip()
    except Exception:
        pass

    # 선택지 텍스트 추출
    option_texts = []
    try:
        labels = driver.find_elements(By.CSS_SELECTOR, "div[role='radiogroup'] label")
        for label in labels:
            option_texts.append(label.find_element(By.TAG_NAME, "span").text.strip())
    except Exception:
        pass

    # 프롬프트에 문제 + 선택지 추가
    enriched_prompt = VQA_PROMPT
    if question_text:
        enriched_prompt += f"\n\n문제: {question_text}"
    if option_texts:
        enriched_prompt += "\n선택지:\n" + "\n".join(f"- {t}" for t in option_texts)

    t_vqa_start = time.perf_counter()

    # 영상 중앙 클릭 (재생 버튼)
    if video_el is not None:
        pos = screen_pos(driver, video_el)
        mover.move_to(pos["x"], pos["y"])
        pyautogui.mouseDown(_pause=False)
        time.sleep(random.uniform(CLICK_HOLD_MIN, CLICK_HOLD_MAX))
        pyautogui.mouseUp(_pause=False)
        print("  [VQA] 영상 중앙 클릭 (재생 시작)")

    # 영상 영역만 녹화
    print(f"  [VQA] 녹화 시작 ({VQA_RECORD_DURATION}초, crop={crop})")
    t_rec_start = time.perf_counter()
    solver.start_recording(crop=crop)
    time.sleep(VQA_RECORD_DURATION)
    path = solver.stop_recording()
    t_rec_end = time.perf_counter()
    print(f"  [VQA] 녹화 완료: {path.name}  ({(t_rec_end - t_rec_start)*1000:.0f}ms)")
    print(f"  [VQA] 파일 크기: {path.stat().st_size / 1024:.1f} KB")
    if option_texts:
        print(f"  [VQA] 선택지: {option_texts}")

    t_api_start = time.perf_counter()
    answer = solver.ask(path, enriched_prompt).strip()
    t_api_end = time.perf_counter()
    t_total = t_api_end - t_vqa_start

    print(f"  [VQA] ── 시간 측정 ──────────────────")
    print(f"  [VQA]   녹화:          {(t_rec_end - t_rec_start)*1000:.0f}ms")
    print(f"  [VQA]   Gemini API:    {(t_api_end - t_api_start)*1000:.0f}ms")
    print(f"  [VQA]   VQA 전체:      {t_total*1000:.0f}ms")
    print(f"  [VQA] ────────────────────────────────")
    print(f"  [VQA] Gemini 응답:\n{answer}")

    # 라디오 선택지에서 정답 텍스트와 일치하는 항목 클릭
    radios = driver.find_elements(By.CSS_SELECTOR, VQA_RADIO_SEL)
    labels = driver.find_elements(By.CSS_SELECTOR, "div[role='radiogroup'] label")
    clicked = False
    for label in labels:
        span_text = label.find_element(By.TAG_NAME, "span").text.strip()
        if answer in span_text or span_text in answer:
            radio = label.find_element(By.CSS_SELECTOR, VQA_RADIO_SEL)
            pos = screen_pos(driver, radio)
            mover.move_to(pos["x"], pos["y"])
            pyautogui.mouseDown(_pause=False)
            time.sleep(random.uniform(CLICK_HOLD_MIN, CLICK_HOLD_MAX))
            pyautogui.mouseUp(_pause=False)
            print(f"  [VQA] 선택: {span_text!r}")
            clicked = True
            break

    if not clicked:
        # 정확히 일치하는 항목이 없으면 첫 번째 선택
        print(f"  [VQA] 정답 일치 항목 없음 — 첫 번째 선택지 선택")
        if radios:
            pos = screen_pos(driver, radios[0])
            mover.move_to(pos["x"], pos["y"])
            pyautogui.mouseDown(_pause=False)
            time.sleep(random.uniform(CLICK_HOLD_MIN, CLICK_HOLD_MAX))
            pyautogui.mouseUp(_pause=False)

    # 제출하기 버튼 클릭
    click_by_text(driver, mover, VQA_SUBMIT_TEXT, timeout=5)
    time.sleep(0.5)
    try:
        alert = driver.switch_to.alert
        print(f"  [VQA] Alert: {alert.text}")
        alert.accept()
    except Exception:
        pass
    print("  [VQA] 제출 완료")
    return True

# ── 메인 ──────────────────────────────────────────────────────────────────────

def main():
    from zoneinfo import ZoneInfo

    # ── 1. 초기화 + 브라우저 오픈
    print("=== 티켓팅 매크로 시작 ===")
    driver = make_driver()
    mover  = BezierMoverPyautogui()
    solver = VQASolver(driver=driver, api_key=GEMINI_API_KEY, fps=10, model="gemini-2.5-flash")

    driver.get(TARGET_URL)
    driver.switch_to.window(driver.current_window_handle)  # 포커스 확보
    WebDriverWait(driver, 15).until(
        lambda d: d.execute_script("return document.readyState") == "complete"
    )

    driver.execute_script(DISABLE_ANIM)

    # ── 2. 서버 시간 동기화 (최초 1회)
    print("[1] 서버 시간 측정 중...")
    offset = get_server_offset(TARGET_URL)

    # ── 3. 목표 시각 파싱 (KST → UTC)
    kst = ZoneInfo("Asia/Seoul")
    target_kst = datetime.strptime(TARGET_TIME, "%Y-%m-%d %H:%M:%S").replace(tzinfo=kst)
    target_utc = target_kst.astimezone(timezone.utc)

    remaining_total = (target_utc - datetime.now(timezone.utc)).total_seconds() - offset
    print(f"[2] 목표 시각: {TARGET_TIME} KST")
    print(f"    남은 시간: {remaining_total:.1f}초")
    print(f"    로그인 등 준비 후 대기 중... (자동 시작까지 카운트다운)")

    # ── 4. 목표 시각까지 대기 (카운트다운 출력, 브라우저 그대로 유지)
    last_printed = -1
    while True:
        remaining = (target_utc - datetime.now(timezone.utc)).total_seconds() - offset
        if remaining <= 0:
            break
        countdown = int(remaining)
        if countdown != last_printed and countdown <= 10:
            print(f"    {countdown}초 전...")
            last_printed = countdown
        if remaining > 1.0:
            time.sleep(min(remaining - 0.5, 0.5))
        elif remaining > 0.05:
            time.sleep(0.005)
        # 50ms 이내: busy-wait

    t_start = time.perf_counter()
    print(f"[3] 클릭 시작!")

    for item in BUTTON_SEQUENCE:
        # 숫자 → 단순 대기
        if isinstance(item, (int, float)):
            print(f"    [대기] {item}초")
            time.sleep(item)
            continue

        # ("vqa", 초) → 해당 시간 내 /vqa URL 감지 시 VQA 진행
        if isinstance(item, tuple) and str(item[0]).lower() == VQA_KEYWORD:
            wait_sec = item[1]
            print(f"    [VQA 대기] 최대 {wait_sec}초")
            try:
                WebDriverWait(driver, wait_sec, poll_frequency=0.05).until(
                    lambda d: d.current_url.endswith(VQA_URL_SUFFIX)
                )
                if check_and_solve_vqa(driver, mover, solver):
                    print(f"    [VQA] 해결 완료 — 루프 종료")
                    break
            except TimeoutException:
                print(f"    [VQA] 시간 내 미감지 — 다음으로 진행")
            continue

        name = item[0] if isinstance(item, tuple) else item
        t_btn = time.perf_counter()

        if name == CHECKBOX_KEYWORD:
            click_checkboxes(driver, mover)
            print(f"    [체크박스] 완료 ({(time.perf_counter() - t_btn)*1000:.0f}ms)")
            continue

        try:
            click_by_text(driver, mover, name, timeout=1)
            elapsed = (time.perf_counter() - t_btn) * 1000
            print(f"    [{name}] 클릭 완료 ({elapsed:.0f}ms)")
        except TimeoutException:
            print(f"    [{name}] 타임아웃 — 버튼 미발견, 건너뜀")
        except Exception as e:
            print(f"    [{name}] 오류: {e}")

    total = (time.perf_counter() - t_start) * 1000
    print(f"\n[완료] 총 소요: {total:.0f}ms")

    # alert 처리 후 URL 출력
    try:
        alert = driver.switch_to.alert
        print(f"       [Alert] {alert.text}")
        alert.accept()
    except Exception:
        pass
    try:
        print(f"       현재 URL: {driver.current_url}")
    except Exception:
        pass

    input("\n엔터를 누르면 브라우저를 닫습니다...")
    driver.quit()
    if getattr(driver, "_proxy_ext_dir", None):
        shutil.rmtree(driver._proxy_ext_dir, ignore_errors=True)


if __name__ == "__main__":
    main()
