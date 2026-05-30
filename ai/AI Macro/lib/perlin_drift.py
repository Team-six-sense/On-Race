"""Perlin Drift Mouse Mover — 속도 프로파일 직접 제어 방식.

Catmull-Rom 스플라인 경로 + Ornstein-Uhlenbeck 속도 스케줄 + 미세 섭동.
XGBoost 매크로 탐지기 우회를 위해 각 feature를 인간 범위 내로 제어.
"""
import math
import random
import time
import bisect


# ── 튜닝 상수 ──────────────────────────────────────────────
V_TARGET_MIN = 1.40         # px/ms — 목표 평균 속도 하한 (인간 상한 근접)
V_TARGET_MAX = 1.55         # px/ms — 목표 평균 속도 상한 (인간 max 1.611 직전)
RAMP_UP_FRAC = 0.12         # 가속 구간 비율 (짧게 — 빠른 출발)
RAMP_DOWN_FRAC = 0.15       # 감속 구간 비율 (짧게 — 빠른 도착)
OU_THETA = 3.0              # O-U 평균 회귀 강도 (약하게 — velocity_std 증가)
OU_SIGMA_FRAC = 0.60        # O-U 노이즈 크기 (크게 — velocity_std 0.83+ 목표)
DT_NOMINAL = 14.0           # ms — 이벤트 간격 (약간 빠르게)
DT_JITTER_STD = 4.0         # ms — 타이밍 노이즈 (크게 → jerk 증폭)
PERP_JITTER_STD = 1.5       # px — 수직 방향 위치 노이즈 (줄여서 jitter_rate 낮춤)
WAYPOINT_SPREAD = 0.06      # 웨이포인트 수직 편향 (줄여서 path_efficiency 낮춤)
MICRO_PAUSE_PROB = 0.015    # 미세 정지 확률 (절반으로 — 시간 절약)
MICRO_PAUSE_MIN = 20        # ms
MICRO_PAUSE_MAX = 50        # ms
N_WAYPOINTS_MIN = 3
N_WAYPOINTS_MAX = 4


# ── Catmull-Rom 스플라인 ──────────────────────────────────
def _catmull_rom_point(p0, p1, p2, p3, t, alpha=0.5):
    """Centripetal Catmull-Rom 보간 (alpha=0.5)."""
    x = 0.5 * (
        (2 * p1[0])
        + (-p0[0] + p2[0]) * t
        + (2 * p0[0] - 5 * p1[0] + 4 * p2[0] - p3[0]) * t * t
        + (-p0[0] + 3 * p1[0] - 3 * p2[0] + p3[0]) * t * t * t
    )
    y = 0.5 * (
        (2 * p1[1])
        + (-p0[1] + p2[1]) * t
        + (2 * p0[1] - 5 * p1[1] + 4 * p2[1] - p3[1]) * t * t
        + (-p0[1] + 3 * p1[1] - 3 * p2[1] + p3[1]) * t * t * t
    )
    return (x, y)


def _catmull_rom_chain(control_points, samples_per_segment=80):
    """제어점 목록 → 호 길이 파라미터화된 (arc_lengths[], positions[]) 반환."""
    n = len(control_points)
    if n < 2:
        return [0.0], [control_points[0]]

    # 팬텀 포인트 추가 (시작/끝 연장)
    pts = list(control_points)
    p_start = (2 * pts[0][0] - pts[1][0], 2 * pts[0][1] - pts[1][1])
    p_end = (2 * pts[-1][0] - pts[-2][0], 2 * pts[-1][1] - pts[-2][1])
    pts = [p_start] + pts + [p_end]

    positions = []
    arc_lengths = [0.0]
    total_arc = 0.0

    for seg in range(1, len(pts) - 2):
        p0, p1, p2, p3 = pts[seg - 1], pts[seg], pts[seg + 1], pts[seg + 2]
        for j in range(samples_per_segment):
            t = j / samples_per_segment
            pos = _catmull_rom_point(p0, p1, p2, p3, t)
            if positions:
                d = math.hypot(pos[0] - positions[-1][0], pos[1] - positions[-1][1])
                total_arc += d
            positions.append(pos)
            arc_lengths.append(total_arc)

    # 마지막 점 추가
    last_ctrl = control_points[-1]
    if positions:
        d = math.hypot(last_ctrl[0] - positions[-1][0], last_ctrl[1] - positions[-1][1])
        total_arc += d
    positions.append(last_ctrl)
    arc_lengths.append(total_arc)

    return arc_lengths, positions


def _lookup_position(arc_lengths, positions, s):
    """호 길이 s에 해당하는 위치를 선형 보간으로 반환."""
    if s <= 0:
        return positions[0]
    if s >= arc_lengths[-1]:
        return positions[-1]
    idx = bisect.bisect_right(arc_lengths, s) - 1
    idx = max(0, min(idx, len(arc_lengths) - 2))
    s0, s1 = arc_lengths[idx], arc_lengths[idx + 1]
    if s1 - s0 < 1e-9:
        return positions[idx]
    frac = (s - s0) / (s1 - s0)
    px = positions[idx][0] + frac * (positions[idx + 1][0] - positions[idx][0])
    py = positions[idx][1] + frac * (positions[idx + 1][1] - positions[idx][1])
    return (px, py)


# ── 경로 생성 ──────────────────────────────────────────────
def _generate_waypoints(sx, sy, ex, ey):
    """시작→끝 사이에 랜덤 웨이포인트 생성."""
    dx, dy = ex - sx, ey - sy
    dist = math.hypot(dx, dy)
    if dist < 1:
        return [(sx, sy), (ex, ey)]

    # 수직 벡터
    perp_x, perp_y = -dy / dist, dx / dist
    spread = dist * WAYPOINT_SPREAD
    # 짧은 거리에선 spread 축소
    if dist < 200:
        spread *= 0.5

    n_wp = random.randint(N_WAYPOINTS_MIN, N_WAYPOINTS_MAX)
    # 웨이포인트 t 위치: 균등 + 약간의 랜덤
    t_positions = sorted([random.uniform(0.12, 0.88) for _ in range(n_wp)])

    waypoints = [(sx, sy)]
    for t in t_positions:
        base_x = sx + dx * t
        base_y = sy + dy * t
        offset = random.gauss(0, spread)
        waypoints.append((base_x + perp_x * offset, base_y + perp_y * offset))
    waypoints.append((ex, ey))
    return waypoints


# ── 속도 스케줄 (O-U 프로세스) ─────────────────────────────
def _generate_velocity_schedule(total_arc):
    """사다리꼴 + O-U 노이즈 속도 프로파일 생성.

    Returns: list of (time_ms, velocity_px_per_ms, arc_distance)
    """
    v_target = random.uniform(V_TARGET_MIN, V_TARGET_MAX)
    total_time = total_arc / v_target  # ms

    dt = DT_NOMINAL  # ms per step
    n_steps = max(5, int(total_time / dt))

    # O-U 파라미터
    sigma = OU_SIGMA_FRAC * v_target
    theta = OU_THETA

    schedule = []
    v = v_target * 0.3  # 시작 속도 (느리게)
    arc_accum = 0.0

    for i in range(n_steps):
        frac = i / max(n_steps - 1, 1)

        # 사다리꼴 엔벨로프
        if frac < RAMP_UP_FRAC:
            # 가속: 0.3 → 1.0
            ramp = 0.3 + 0.7 * (frac / RAMP_UP_FRAC)
        elif frac > (1.0 - RAMP_DOWN_FRAC):
            # 감속: 1.0 → 0.2
            ramp = 0.2 + 0.8 * ((1.0 - frac) / RAMP_DOWN_FRAC)
        else:
            ramp = 1.0

        v_envelope = v_target * ramp

        # O-U 업데이트 (평균 회귀)
        dt_sec = dt / 1000.0
        dv = theta * (v_envelope - v) * dt_sec + sigma * random.gauss(0, math.sqrt(dt_sec))
        v += dv

        # 속도 미세 스파이크: 8% 확률로 순간적 속도 변동 → jerk 증폭
        if random.random() < 0.08:
            v *= random.uniform(0.4, 1.8)

        v = max(0.05, v)  # 최소 속도 보장

        # 미세 정지 삽입
        actual_dt = dt + random.gauss(0, DT_JITTER_STD)
        actual_dt = max(3.0, actual_dt)

        if MICRO_PAUSE_PROB > 0 and random.random() < MICRO_PAUSE_PROB and 0.1 < frac < 0.9:
            actual_dt += random.uniform(MICRO_PAUSE_MIN, MICRO_PAUSE_MAX)
            v *= 0.1  # 정지 중 거의 안 움직임

        ds = v * actual_dt
        arc_accum += ds

        schedule.append((actual_dt, v, arc_accum))

    # 호 길이 정규화: schedule의 arc가 total_arc에 맞도록 스케일
    if arc_accum > 0:
        scale = total_arc / arc_accum
        schedule = [(dt_val, v_val, s * scale) for dt_val, v_val, s in schedule]

    return schedule


# ── 메인 클래스 ────────────────────────────────────────────
class PerlinDriftMover:
    """Perlin Drift 기반 pyautogui 마우스 이동.

    속도 프로파일을 직접 제어하여 매크로 탐지기의 인간 범위 feature를 달성.
    BezierMoverPyautogui와 동일한 인터페이스 (move_to, click).
    """

    def __init__(self):
        import pyautogui as _pag
        self._pag = _pag
        self._pag.PAUSE = 0

    def _build_path(self, sx, sy, ex, ey):
        """경로 + 속도 → 시간별 위치 시퀀스 생성.

        Returns: list of (px, py, wait_ms)
        """
        dist = math.hypot(ex - sx, ey - sy)
        if dist < 2:
            return [(int(ex), int(ey), DT_NOMINAL)]

        # 1. 웨이포인트 → 스플라인 → 호 길이 파라미터화
        waypoints = _generate_waypoints(sx, sy, ex, ey)
        arc_lengths, positions = _catmull_rom_chain(waypoints)
        total_arc = arc_lengths[-1]

        if total_arc < 1:
            return [(int(ex), int(ey), DT_NOMINAL)]

        # 2. 속도 스케줄 생성
        schedule = _generate_velocity_schedule(total_arc)

        # 3. 호 길이 → 위치 매핑 + 수직 지터
        result = []
        prev_x, prev_y = sx, sy
        for dt_val, v_val, arc_s in schedule:
            pos = _lookup_position(arc_lengths, positions, arc_s)
            px, py = pos

            # 수직 지터: 이동 방향에 수직으로 노이즈 추가
            move_dx = px - prev_x
            move_dy = py - prev_y
            move_dist = math.hypot(move_dx, move_dy)
            if move_dist > 0.5:
                perp_nx = -move_dy / move_dist
                perp_ny = move_dx / move_dist
                jitter = random.gauss(0, PERP_JITTER_STD)
                px += perp_nx * jitter
                py += perp_ny * jitter

            ix, iy = int(round(px)), int(round(py))
            # 중복 위치 스킵 (동일 픽셀이면 움직임 없음)
            if result and ix == result[-1][0] and iy == result[-1][1]:
                result[-1] = (ix, iy, result[-1][2] + dt_val)
            else:
                result.append((ix, iy, dt_val))

            prev_x, prev_y = px, py

        # 마지막 점이 정확히 목표에 도달하도록
        if result:
            result[-1] = (int(round(ex)), int(round(ey)), result[-1][2])

        return result

    def _execute(self, path_points):
        """시간 스케줄에 따라 moveTo 호출."""
        if not path_points:
            return
        t0 = time.perf_counter()
        elapsed_target = 0.0

        for px, py, wait_ms in path_points:
            elapsed_target += wait_ms / 1000.0
            now = time.perf_counter() - t0
            wait = elapsed_target - now
            if wait > 0:
                time.sleep(wait)
            self._pag.moveTo(px, py, duration=0, _pause=False)

    def move_to(self, tx, ty, duration=None):
        """현재 OS 커서 위치에서 (tx, ty)까지 이동."""
        cur_x, cur_y = self._pag.position()
        path = self._build_path(cur_x, cur_y, tx, ty)
        self._execute(path)

    def click(self, tx, ty, duration=None):
        """(tx, ty)까지 2단계 이동 후 클릭.

        Phase 1: 대략적 접근 (overshoot 포함)
        Phase 2: 정밀 보정
        """
        cur_x, cur_y = self._pag.position()

        # Phase 1: 거친 접근 — 작은 overshoot (시간 절약)
        rough_x = tx + random.uniform(-15, 15)
        rough_y = ty + random.uniform(-30, 30)
        path1 = self._build_path(cur_x, cur_y, rough_x, rough_y)
        self._execute(path1)

        # Phase 2: 정밀 보정 — 짧은 거리, 느린 속도
        path2 = self._build_path(rough_x, rough_y, tx, ty)
        self._execute(path2)

        self._pag.click(_pause=False)
