import math
import random

G_0 = 9
W_0 = 10
M_0 = 14
D_0 = 3


MAX_MOUSE_STEPS = 5  # page.mouse.move 호출 상한 (Playwright 전송 비용 제한)
STEPS_MIN = 2         # page.mouse.move steps 랜덤 범위 하한
STEPS_MAX = 4        # page.mouse.move steps 랜덤 범위 상한


def _subsample(points, n):
    """n개 이하로 균등 추출 (마지막 점은 항상 포함)."""
    if len(points) <= n:
        return points
    step = len(points) / n
    sampled = [points[int(i * step)] for i in range(n - 1)]
    sampled.append(points[-1])
    return sampled


def _wind_path(sx, sy, ex, ey, g=G_0, w=W_0, m=M_0, d=D_0):
    cx, cy = float(sx), float(sy)
    vx = vy = wx = wy = 0.0
    points = []
    while True:
        dist = math.hypot(ex - cx, ey - cy)
        if dist < 1:
            break
        w_mag = min(w, dist)
        if dist >= d:
            wx = wx / math.sqrt(3) + (2 * random.random() - 1) * w_mag / math.sqrt(5)
            wy = wy / math.sqrt(3) + (2 * random.random() - 1) * w_mag / math.sqrt(5)
        else:
            wx /= math.sqrt(3)
            wy /= math.sqrt(3)
            if m < 3:
                m = random.uniform(3, 5)
            else:
                m /= math.sqrt(5)
        vx += wx + g * (ex - cx) / dist
        vy += wy + g * (ey - cy) / dist
        v_mag = math.hypot(vx, vy)
        if v_mag > m:
            v_clip = m / 2 + random.uniform(0, m / 2)
            vx = vx / v_mag * v_clip
            vy = vy / v_mag * v_clip
        cx += vx
        cy += vy
        points.append((int(round(cx)), int(round(cy))))
    points.append((int(ex), int(ey)))
    return points


class WindMover:

    def __init__(self, driver):
        self.driver = driver
        self.cur = [0, 0]

    def _get_center(self, element):
        r = self.driver.execute_script(
            "var r=arguments[0].getBoundingClientRect();"
            "return {x:Math.round(r.left+r.width/2),y:Math.round(r.top+r.height/2)};",
            element,
        )
        return r["x"], r["y"]

    def click(self, element):
        tx, ty = self._get_center(element)
        for px, py in _wind_path(self.cur[0], self.cur[1], tx, ty):
            self.driver.execute_cdp_cmd("Input.dispatchMouseEvent", {
                "type": "mouseMoved", "x": px, "y": py,
            })
        element.click()
        self.cur[0], self.cur[1] = tx, ty


class WindMoverPlaywright:

    def __init__(self, page):
        self.page = page
        self.cur = [0, 0]

    def click(self, locator):
        box = locator.bounding_box()
        tx = int(box["x"] + box["width"] / 2)
        ty = int(box["y"] + box["height"] / 2)
        path = _subsample(_wind_path(self.cur[0], self.cur[1], tx, ty), MAX_MOUSE_STEPS)
        for px, py in path:
            self.page.mouse.move(px, py, steps=random.randint(STEPS_MIN, STEPS_MAX))
        locator.click()
        self.cur[0], self.cur[1] = tx, ty
