import math
import random
import time


EASE_POWER   = 3   # 이징 강도: 높을수록 초반/후반 가감속이 더 급격해짐
MIDDLE_RIPPLE = 0.06  # 중간 구간 속도 미세 변동 크기 (0이면 완전 일정)


def _ease_in_out(t, power=EASE_POWER):
    """초반/후반 강한 가감속, 중간 약한 가감속 변동.

    power=2: 부드러운 곡선 / power=4: 뚜렷한 가감속 / power=6: 매우 급격한 가감속
    MIDDLE_RIPPLE: 중간 구간에 sin 파형으로 미세한 속도 변동 추가
                   (t=0, t=1에서 0이 되도록 envelope 적용 → 시작/끝에 영향 없음)
    """
    if t < 0.5:
        base = 0.5 * (2 * t) ** power
    else:
        base = 1.0 - 0.5 * (2 * (1 - t)) ** power

    # 중간 구간 미세 변동: sin(π*t)로 양 끝은 0, 중앙에서 최대
    ripple = MIDDLE_RIPPLE * math.sin(math.pi * t) * math.sin(3 * math.pi * t)
    return base + ripple


def _bezier_curve(p0, p2, deviation, steps):
    mx = (p0[0] + p2[0]) / 2
    my = (p0[1] + p2[1]) / 2
    dx, dy = p2[0] - p0[0], p2[1] - p0[1]
    dist = math.hypot(dx, dy)
    if dist > 0:
        perp_x, perp_y = -dy / dist, dx / dist
        offset = random.uniform(-deviation, deviation)
        p1 = (mx + perp_x * offset, my + perp_y * offset)
    else:
        p1 = (mx, my)
    points = []
    for i in range(1, steps + 1):
        t = _ease_in_out(i / steps)
        x = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t ** 2 * p2[0]
        y = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t ** 2 * p2[1]
        points.append((int(round(x)), int(round(y))))
    return points


class BezierMoverPyautogui:
    """pyautogui 기반 Bezier 마우스 이동.

    포인트는 균등하게 생성하고, 방문 시각을 ease-in-out 스케줄로 제어.
    → 초반/후반 느린 이동, 중간 빠른 이동 (끊김 없는 슬라이딩).

    OS 커서를 실제로 움직이므로 isTrusted=true.
    창이 보여야 하며 멀티 브라우저 동시 제어 불가.
    """

    STEPS_PER_PX   = 0.03   # px당 포인트 수 (moveTo 호출 최소화)
    MIN_DURATION   = 0.015  # 이동 최소 시간 (초) — 기존 대비 2배 빠름
    MAX_DURATION   = 0.030  # 이동 최대 시간 (초)

    def __init__(self):
        import pyautogui as _pag
        self._pag = _pag
        self._pag.PAUSE = 0  # 호출 사이 기본 딜레이 제거

    def _gen_curve(self, p0, p2, deviation):
        """제어점 생성 + 균등 t 간격 포인트 반환 (위치 easing 없음).

        중간 포인트에 Gaussian 노이즈 추가 → angle_change_std 인간 범위 확보.
        """
        mx, my = (p0[0] + p2[0]) / 2, (p0[1] + p2[1]) / 2
        dx, dy = p2[0] - p0[0], p2[1] - p0[1]
        dist = math.hypot(dx, dy)
        if dist > 0:
            perp_x, perp_y = -dy / dist, dx / dist
            p1 = (mx + perp_x * random.uniform(-deviation, deviation),
                  my + perp_y * random.uniform(-deviation, deviation))
        else:
            p1 = (mx, my)

        steps = max(10, int(dist * self.STEPS_PER_PX))
        noise_sigma = 6.0  # px — 적은 스텝 보상, 큰 노이즈로 angle/jitter 확보
        points = []
        for i in range(steps + 1):
            t = i / steps  # 균등 간격 — 타이밍은 _execute_move 에서 제어
            x = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t ** 2 * p2[0]
            y = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t ** 2 * p2[1]
            if 0 < i < steps:  # 시작/끝 제외한 중간 포인트에만 노이즈
                x += random.gauss(0, noise_sigma)
                y += random.gauss(0, noise_sigma)
            points.append((int(round(x)), int(round(y))))
        return points

    def _execute_move(self, points, duration):
        """ease-in-out 시간표에 따라 포인트를 방문 (duration=0 + sleep 제어).

        방문 시각 = ease_in_out(i/N) * duration + 누적 Gaussian 지터
        → 초반/후반 느림, 중간 빠름 + 근육 떨림 수준의 타이밍 불규칙성 (jerk_std 확보).
        """
        n = len(points)
        if n == 0:
            return
        t0 = time.perf_counter()

        # 제로섬 Gaussian 지터: 각 스텝 타이밍에 불규칙성 추가, 누적 드리프트 방지
        jitter_sigma = 0.003  # 3ms std — 타이밍 불규칙성 증가
        raw_jitter = [random.gauss(0, jitter_sigma) for _ in range(n)]
        mean_j = sum(raw_jitter) / n
        jitter = [j - mean_j for j in raw_jitter]

        cumulative_jitter = 0.0
        for i, (px, py) in enumerate(points):
            cumulative_jitter += jitter[i]
            t_target = _ease_in_out(i / max(n - 1, 1)) * duration + cumulative_jitter
            wait = t_target - (time.perf_counter() - t0)
            if wait > 0:
                time.sleep(wait)
            self._pag.moveTo(px, py, duration=0, _pause=False)

    def move_to(self, tx, ty, duration=None):
        """현재 OS 커서 위치에서 (tx, ty)까지 Bezier 이동."""
        cur_x, cur_y = self._pag.position()
        dist = math.hypot(tx - cur_x, ty - cur_y)
        if duration is None:
            duration = random.uniform(self.MIN_DURATION, self.MAX_DURATION)
        points = self._gen_curve((cur_x, cur_y), (tx, ty), dist * random.uniform(0.15, 0.35))
        self._execute_move(points, duration)

    def click(self, tx, ty, duration=None):
        """(tx, ty)까지 2단계 Bezier 이동 후 클릭."""
        cur_x, cur_y = self._pag.position()

        # Phase 1: 거친 접근
        rough_x = tx + random.uniform(-30, 30)
        rough_y = ty + random.uniform(-60, 60)
        dist1 = math.hypot(rough_x - cur_x, rough_y - cur_y)
        dur1 = random.uniform(self.MIN_DURATION, self.MAX_DURATION) if duration is None else duration * 0.7
        phase1 = self._gen_curve((cur_x, cur_y), (rough_x, rough_y),
                                 dist1 * random.uniform(0.02, 0.08))
        self._execute_move(phase1, dur1)

        # Phase 2: 정밀 조정
        dist2 = math.hypot(tx - rough_x, ty - rough_y)
        dur2 = random.uniform(self.MIN_DURATION * 0.4, self.MAX_DURATION * 0.4)
        phase2 = self._gen_curve((rough_x, rough_y), (tx, ty),
                                 dist2 * random.uniform(0.05, 0.15))
        self._execute_move(phase2, dur2)

        self._pag.click(_pause=False)


class BezierMover:

    def __init__(self, driver):
        self.driver = driver
        self.cur = [0, 0]

    def _cdp_move(self, x, y):
        self.driver.execute_cdp_cmd("Input.dispatchMouseEvent", {
            "type": "mouseMoved", "x": x, "y": y,
        })

    def _get_box(self, element):
        return self.driver.execute_script(
            "var r=arguments[0].getBoundingClientRect();"
            "return {x:r.left,y:r.top,width:r.width,height:r.height};",
            element,
        )

    def click(self, element):
        box = self._get_box(element)
        cx = box["x"] + box["width"] / 2
        cy = box["y"] + box["height"] / 2

        # Phase 1: rough approach (may overshoot)
        rough_x = cx + random.uniform(-box["width"], box["width"])
        rough_y = cy + random.uniform(-box["height"] * 2, box["height"] * 2)
        dist1 = math.hypot(rough_x - self.cur[0], rough_y - self.cur[1])
        steps1 = min(16, max(8, int(dist1 / 40)))
        dev1 = dist1 * random.uniform(0.2, 0.4)
        for px, py in _bezier_curve((self.cur[0], self.cur[1]), (rough_x, rough_y), dev1, steps1):
            self._cdp_move(px, py)

        # Phase 2: fine adjustment to random point inside button
        margin = max(2, min(box["width"], box["height"]) * 0.12)
        click_x = int(round(random.uniform(box["x"] + margin, box["x"] + box["width"] - margin)))
        click_y = int(round(random.uniform(box["y"] + margin, box["y"] + box["height"] - margin)))
        dist2 = math.hypot(click_x - rough_x, click_y - rough_y)
        steps2 = min(10, max(5, int(dist2 / 15)))
        dev2 = dist2 * random.uniform(0.05, 0.15)
        for px, py in _bezier_curve((rough_x, rough_y), (click_x, click_y), dev2, steps2):
            self._cdp_move(px, py)

        element.click()
        self.cur[0], self.cur[1] = click_x, click_y
