"""AI 기반 자유 설계 마우스 매크로.

AI가 움직임의 모든 것(경로, 속도, 타이밍)을 JSON config로 설계.
SimpleExecutor는 config를 받아 pyautogui로 실행만 담당.
MovementHistory는 과거 탐지 결과를 기록.
AIMouseDesigner는 이력을 참고하여 AI 프롬프트를 생성.
"""
import json
import math
import random
import re
import time
from datetime import datetime
from pathlib import Path


# ── 인간 feature 범위 (탐지기 학습 데이터 기준) ───────────────
HUMAN_RANGES = {
    "velocity_mean":    {"min": 0.597, "max": 1.611, "mean": 0.972, "importance": "58.5% (최중요)"},
    "velocity_std":     {"min": 0.547, "max": 1.867, "importance": "8.8%"},
    "jerk_mean":        {"min": -43100000, "max": 0.0006, "importance": "5.0%"},
    "jerk_std":         {"min": 0.001, "max": 437800000, "importance": "2.5%"},
    "angle_change_std": {"min": 0.402, "max": 1.099, "importance": "3.1%"},
    "jitter_rate":      {"min": 0.144, "max": 0.500, "importance": "분류에 사용됨"},
    "path_efficiency":  {"min": 1.649, "max": 4.007, "importance": "1.6%"},
    "total_distance":   {"min": 1542, "max": 3568, "importance": "1.9%"},
    "pause_count":      {"min": 0, "max": 12, "importance": "0.5%"},
    "move_to_click_ratio": {"min": 8.7, "max": 38.0, "importance": "미사용"},
    "avg_click_duration":  {"min": 52, "max": 171, "importance": "미사용 (but 인간 구분 핵심)"},
    "std_click_duration":  {"min": 0.8, "max": 113.6, "importance": "미사용"},
}


# ── SimpleExecutor ─────────────────────────────────────────
class SimpleExecutor:
    """AI가 설계한 config를 받아 pyautogui로 실행.

    config 포맷:
    {
        "waypoints": [
            {"t": 0.0, "lateral": 0.0, "delay_ms": 0},
            {"t": 0.5, "lateral": 0.02, "delay_ms": 14},
            {"t": 1.0, "lateral": 0.0, "delay_ms": 18}
        ],
        "jitter": {"position_px": 2.0, "time_ms": 2.0},
        "overshoot": {"enabled": true, "max_px": 30, "correction_delay_ms": 50},
        "click_hold_ms": {"min": 55, "max": 95}
    }

    - waypoints: 정규화된 경로점 (t: 0→1, lateral: 수직 편향 비율, delay_ms: 대기시간)
    - jitter: 위치/타이밍 노이즈
    - overshoot: 클릭 시 2단계 접근
    - click_hold_ms: 클릭 유지 시간
    """

    def __init__(self, config=None):
        import pyautogui as _pag
        self._pag = _pag
        self._pag.PAUSE = 0
        self.config = config or self._default_config()

    def set_config(self, config):
        self.config = config

    def get_config(self):
        return self.config

    @staticmethod
    def _default_config():
        return {
            "waypoints": [
                {"t": 0.0,  "lateral": 0.0,    "delay_ms": 0},
                {"t": 0.08, "lateral": 0.005,   "delay_ms": 20},
                {"t": 0.18, "lateral": 0.015,   "delay_ms": 16},
                {"t": 0.30, "lateral": 0.020,   "delay_ms": 14},
                {"t": 0.42, "lateral": 0.012,   "delay_ms": 13},
                {"t": 0.55, "lateral": -0.005,  "delay_ms": 12},
                {"t": 0.65, "lateral": -0.010,  "delay_ms": 13},
                {"t": 0.75, "lateral": -0.008,  "delay_ms": 14},
                {"t": 0.85, "lateral": -0.003,  "delay_ms": 16},
                {"t": 0.93, "lateral": 0.001,   "delay_ms": 18},
                {"t": 1.0,  "lateral": 0.0,     "delay_ms": 22},
            ],
            "jitter": {"position_px": 2.0, "time_ms": 2.5},
            "overshoot": {"enabled": True, "max_px": 30, "correction_delay_ms": 50},
            "click_hold_ms": {"min": 55, "max": 95},
        }

    def _interpolate(self, sx, sy, ex, ey, waypoints):
        """waypoints → 실제 (x, y, delay_ms) 리스트 생성."""
        dx, dy = ex - sx, ey - sy
        dist = math.hypot(dx, dy)
        if dist < 1:
            return [(int(ex), int(ey), 10)]

        # 수직 벡터
        perp_x, perp_y = -dy / dist, dx / dist
        jitter_pos = self.config.get("jitter", {}).get("position_px", 0)
        jitter_time = self.config.get("jitter", {}).get("time_ms", 0)

        points = []
        for wp in waypoints:
            t = wp["t"]
            lateral = wp["lateral"]
            delay = wp["delay_ms"]

            # 직선 보간 + 수직 오프셋
            px = sx + dx * t + perp_x * lateral * dist
            py = sy + dy * t + perp_y * lateral * dist

            # 랜덤 지터 (시작/끝 제외)
            if 0 < t < 1 and jitter_pos > 0:
                px += random.gauss(0, jitter_pos)
                py += random.gauss(0, jitter_pos)

            # 타이밍 지터
            if jitter_time > 0 and delay > 0:
                delay = max(1, delay + random.gauss(0, jitter_time))

            points.append((int(round(px)), int(round(py)), delay))

        return points

    def _execute(self, points):
        """시간표에 따라 moveTo 호출."""
        t0 = time.perf_counter()
        elapsed_target = 0.0

        for px, py, delay_ms in points:
            elapsed_target += delay_ms / 1000.0
            now = time.perf_counter() - t0
            wait = elapsed_target - now
            if wait > 0:
                time.sleep(wait)
            self._pag.moveTo(px, py, duration=0, _pause=False)

    def move_to(self, tx, ty, duration=None):
        """현재 위치에서 (tx, ty)까지 이동."""
        sx, sy = self._pag.position()
        waypoints = self.config["waypoints"]
        points = self._interpolate(sx, sy, tx, ty, waypoints)
        self._execute(points)

    def click(self, tx, ty, duration=None):
        """(tx, ty)까지 이동 후 클릭."""
        sx, sy = self._pag.position()
        overshoot = self.config.get("overshoot", {})
        hold = self.config.get("click_hold_ms", {"min": 55, "max": 95})

        if overshoot.get("enabled", False):
            # Phase 1: overshoot
            max_px = overshoot.get("max_px", 30)
            rough_x = tx + random.uniform(-max_px, max_px)
            rough_y = ty + random.uniform(-max_px * 1.5, max_px * 1.5)
            points1 = self._interpolate(sx, sy, rough_x, rough_y, self.config["waypoints"])
            self._execute(points1)

            # Phase 2: 정밀 보정 (waypoint 축소)
            correction = overshoot.get("correction_delay_ms", 50)
            correction_wps = [
                {"t": 0.0, "lateral": 0.0, "delay_ms": 0},
                {"t": 0.4, "lateral": 0.002, "delay_ms": correction * 0.3},
                {"t": 0.7, "lateral": -0.001, "delay_ms": correction * 0.3},
                {"t": 1.0, "lateral": 0.0, "delay_ms": correction * 0.4},
            ]
            points2 = self._interpolate(rough_x, rough_y, tx, ty, correction_wps)
            self._execute(points2)
        else:
            points = self._interpolate(sx, sy, tx, ty, self.config["waypoints"])
            self._execute(points)

        # 클릭
        hold_time = random.uniform(hold["min"] / 1000, hold["max"] / 1000)
        self._pag.mouseDown(_pause=False)
        time.sleep(hold_time)
        self._pag.mouseUp(_pause=False)


# ── MovementHistory ────────────────────────────────────────
class MovementHistory:
    """탐지 결과 이력 관리.

    failed: 탐지된 움직임 목록 (최대 10개 보관)
    last_success: 가장 최근 탐지 우회 성공한 움직임
    """

    MAX_FAILED = 10

    def __init__(self, path="movement_history.json"):
        self.path = Path(path)
        self.data = self._load()

    def _load(self):
        if self.path.exists():
            try:
                return json.loads(self.path.read_text())
            except (json.JSONDecodeError, KeyError):
                pass
        return {"failed": [], "last_success": None}

    def _save(self):
        self.path.write_text(json.dumps(self.data, indent=2, ensure_ascii=False))

    def record(self, config, features, probability, is_macro):
        """결과 기록."""
        entry = {
            "config": config,
            "features": features,
            "probability": probability,
            "timestamp": datetime.now().isoformat(),
        }
        if is_macro:
            self.data["failed"].append(entry)
            # 최근 10개만 유지
            if len(self.data["failed"]) > self.MAX_FAILED:
                self.data["failed"] = self.data["failed"][-self.MAX_FAILED:]
        else:
            self.data["last_success"] = entry
        self._save()

    def get_failed(self):
        return self.data["failed"]

    def get_last_success(self):
        return self.data["last_success"]


# ── AIMouseDesigner ────────────────────────────────────────
class AIMouseDesigner:
    """AI에게 움직임 설계를 요청하기 위한 프롬프트 생성 + 응답 파싱."""

    def __init__(self, history=None):
        self.history = history or MovementHistory()

    def build_prompt(self):
        """AI에게 보낼 프롬프트 생성."""
        failed = self.history.get_failed()
        success = self.history.get_last_success()

        prompt = """너는 마우스 매크로 탐지 우회 전문가야.
아래 정보를 참고하여 탐지기를 우회하면서 최대한 빠른 마우스 움직임 config를 설계해줘.

## 목표
1. 매크로 탐지기에 "인간"으로 판정되어야 함 (매크로 확률 < 30%)
2. 최대한 빠르게 (인간 최상위권 속도 — velocity_mean 1.3~1.5 px/ms)
3. 자연스러운 움직임 패턴

## 탐지기 Feature 분석 (인간 범위)
"""
        for name, info in HUMAN_RANGES.items():
            line = f"- {name}: [{info['min']}, {info['max']}]"
            if "mean" in info:
                line += f" (평균 {info['mean']})"
            line += f"  — 모델 중요도: {info['importance']}"
            prompt += line + "\n"

        prompt += """
## Feature와 Config의 관계
- velocity_mean: waypoint 간 delay_ms가 작을수록 빠름 (거리/시간)
- velocity_std: delay_ms의 변동이 클수록 증가
- angle_change_std: lateral 값의 변화가 클수록 증가 (방향 전환)
- jitter_rate: jitter.position_px가 클수록 증가 (방향 반전 비율)
- path_efficiency: lateral 값이 클수록 증가 (우회 경로)
- total_distance: path_efficiency × 직선거리
- jerk: delay_ms 변동 + 위치 변화의 불규칙성에 의해 결정
- avg_click_duration: click_hold_ms 범위
- pause_count: delay_ms > 150 인 구간 수

## Config 포맷
```json
{
  "waypoints": [
    {"t": 0.0, "lateral": 0.0, "delay_ms": 0},
    {"t": ..., "lateral": ..., "delay_ms": ...},
    {"t": 1.0, "lateral": 0.0, "delay_ms": ...}
  ],
  "jitter": {"position_px": float, "time_ms": float},
  "overshoot": {"enabled": bool, "max_px": float, "correction_delay_ms": float},
  "click_hold_ms": {"min": float, "max": float}
}
```

### Config 필드 설명
- waypoints: 정규화 경로점 배열
  - t: 0→1 진행도 (시작→끝). 반드시 0.0으로 시작, 1.0으로 끝나야 함
  - lateral: 이동 방향에 수직인 편향 (거리 대비 비율). 양수=왼쪽, 음수=오른쪽
  - delay_ms: 이 지점에서 다음 지점까지 대기 시간 (ms)
  - waypoint 개수는 자유 (5~30개 권장)
- jitter.position_px: 각 waypoint에 가우시안 위치 노이즈 (px 단위)
- jitter.time_ms: delay_ms에 가우시안 타이밍 노이즈 (ms 단위)
- overshoot.enabled: 클릭 시 overshoot 사용 여부
- overshoot.max_px: overshoot 최대 거리 (px)
- overshoot.correction_delay_ms: 보정 이동 총 시간 (ms)
- click_hold_ms.min/max: 클릭 유지 시간 범위 (ms)
"""

        if failed:
            prompt += "\n## 과거 탐지된 움직임 (실패 기록)\n"
            for i, entry in enumerate(failed[-5:], 1):  # 최근 5개만
                prompt += f"\n### 실패 #{i} (매크로 확률: {entry['probability']:.1%})\n"
                prompt += f"Feature 값:\n"
                for fname, fval in entry["features"].items():
                    human = HUMAN_RANGES.get(fname, {})
                    status = ""
                    if "min" in human and "max" in human:
                        if fval < human["min"]:
                            status = " ⬇ 너무 낮음"
                        elif fval > human["max"]:
                            status = " ⬆ 너무 높음"
                        else:
                            status = " ✓ 정상"
                    prompt += f"  {fname}: {fval}{status}\n"
                prompt += f"Config: {json.dumps(entry['config'], indent=2)}\n"

        if success:
            prompt += f"\n## 가장 최근 우회 성공한 움직임\n"
            prompt += f"매크로 확률: {success['probability']:.1%}\n"
            prompt += f"Feature 값:\n"
            for fname, fval in success["features"].items():
                prompt += f"  {fname}: {fval}\n"
            prompt += f"Config: {json.dumps(success['config'], indent=2)}\n"

        prompt += """
## 응답 형식
반드시 JSON config만 응답해줘. 설명 불필요.
```json
{여기에 config}
```
"""
        return prompt

    @staticmethod
    def parse_response(text):
        """AI 응답에서 JSON config 추출."""
        text = text.strip()

        # 1. 코드 블록에서 추출 시도
        code_match = re.search(r'```(?:json)?\s*\n?(.*?)```', text, re.DOTALL)
        if code_match:
            try:
                return json.loads(code_match.group(1).strip())
            except json.JSONDecodeError:
                pass

        # 2. 전체를 JSON으로 파싱 시도
        try:
            return json.loads(text)
        except json.JSONDecodeError:
            pass

        # 3. 중괄호 블록 추출 (중첩 지원)
        depth = 0
        start = None
        for i, ch in enumerate(text):
            if ch == '{':
                if depth == 0:
                    start = i
                depth += 1
            elif ch == '}':
                depth -= 1
                if depth == 0 and start is not None:
                    try:
                        return json.loads(text[start:i + 1])
                    except json.JSONDecodeError:
                        start = None

        return None

    def record_result(self, config, features, probability, is_macro):
        """탐지 결과를 이력에 기록."""
        self.history.record(config, features, probability, is_macro)
