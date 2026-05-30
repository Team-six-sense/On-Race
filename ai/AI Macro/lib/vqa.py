import threading
import time
from datetime import datetime
from pathlib import Path
from typing import Any

import cv2
import numpy as np


class VQASolver:
    """
    브라우저 화면 녹화 + Gemini VQA 파이프라인.

    driver(Selenium) 또는 page(Playwright) 중 하나를 전달.
    브라우저 내부에서 직접 캡처하므로 창이 가려져 있거나
    최소화되어도 정상 동작합니다.

    Usage (Selenium):
        solver = VQASolver(driver=driver, api_key="...", duration=10)
        answer = solver.record_and_ask("수집 버튼이 눌렸나요?")

    Usage (manual control):
        solver.start_recording()
        macro.run()
        path = solver.stop_recording()
        answer = solver.ask(path, "오류가 발생했나요?")
    """

    def __init__(
        self,
        driver: Any = None,
        page: Any = None,
        duration: float = 10.0,
        fps: int = 10,
        output_dir: str = "recordings",
        api_key: str | None = None,
        model: str = "gemini-2.5-flash",
    ):
        if driver is None and page is None:
            raise ValueError("driver(Selenium) 또는 page(Playwright) 중 하나를 전달하세요.")

        self.driver = driver
        self.page = page
        self.duration = duration
        self.fps = fps
        self.output_dir = Path(output_dir)
        self.api_key = api_key
        self.model_name = model

        self._stop_event = threading.Event()
        self._thread: threading.Thread | None = None
        self._last_path: Path | None = None

    # ── 프레임 캡처 ─────────────────────────────────────────────────────────

    def _capture_frame(self) -> np.ndarray:
        """브라우저에서 직접 PNG를 가져와 BGR ndarray로 반환."""
        if self.driver is not None:
            png: bytes = self.driver.get_screenshot_as_png()
        else:
            png = self.page.screenshot()  # type: ignore[union-attr]
        arr = np.frombuffer(png, dtype=np.uint8)
        frame = cv2.imdecode(arr, cv2.IMREAD_COLOR)
        assert frame is not None, "imdecode 실패: PNG 데이터가 올바르지 않습니다."
        return frame

    # ── 녹화 ───────────────────────────────────────────────────────────────

    def start_recording(self, crop: tuple[int, int, int, int] | None = None) -> "VQASolver":
        """백그라운드 스레드로 브라우저 녹화 시작.

        crop: (x, y, width, height) 브라우저 픽셀 좌표 — 지정 시 해당 영역만 녹화.
        """
        if self._thread and self._thread.is_alive():
            raise RuntimeError("이미 녹화 중입니다. stop_recording() 먼저 호출하세요.")

        self.output_dir.mkdir(parents=True, exist_ok=True)
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        path = self.output_dir / f"capture_{timestamp}.mp4"
        self._last_path = path
        self._crop = crop

        self._stop_event.clear()
        self._thread = threading.Thread(
            target=self._record_loop, args=(path,), daemon=True
        )
        self._thread.start()
        return self

    def _record_loop(self, path: Path) -> None:
        interval = 1.0 / self.fps
        out: cv2.VideoWriter | None = None
        crop = getattr(self, "_crop", None)
        try:
            while not self._stop_event.is_set():
                t0 = time.perf_counter()
                frame = self._capture_frame()

                if crop is not None:
                    x, y, w, h = crop
                    fh, fw = frame.shape[:2]
                    x1, y1 = max(0, x), max(0, y)
                    x2, y2 = min(fw, x + w), min(fh, y + h)
                    frame = frame[y1:y2, x1:x2]

                if out is None:
                    h_out, w_out = frame.shape[:2]
                    fourcc = cv2.VideoWriter.fourcc(*"mp4v")  # type: ignore[attr-defined]
                    out = cv2.VideoWriter(str(path), fourcc, self.fps, (w_out, h_out))

                out.write(frame)
                wait = interval - (time.perf_counter() - t0)
                if wait > 0:
                    time.sleep(wait)
        finally:
            if out is not None:
                out.release()

    def stop_recording(self) -> Path:
        """녹화 중지 후 저장된 파일 경로 반환."""
        self._stop_event.set()
        if self._thread:
            self._thread.join(timeout=10)
            self._thread = None
        assert self._last_path is not None
        return self._last_path

    def record(self) -> Path:
        """duration초 동안 녹화 후 파일 경로 반환 (블로킹)."""
        self.start_recording()
        time.sleep(self.duration)
        return self.stop_recording()

    # ── Gemini VQA ─────────────────────────────────────────────────────────

    def ask(self, video_path: Path, prompt: str, max_retries: int = 2) -> str:
        """녹화된 영상 + 프롬프트를 Gemini에 전송하고 응답 반환 (인라인 데이터).
        503/429 등 일시적 오류는 최대 max_retries회 재시도.
        """
        if not self.api_key:
            raise ValueError("api_key가 설정되지 않았습니다.")

        from google import genai  # type: ignore[import]
        from google.genai import types  # type: ignore[import]
        from google.genai import errors as genai_errors  # type: ignore[import]

        client = genai.Client(api_key=self.api_key)

        t_read = time.perf_counter()
        video_bytes = Path(video_path).read_bytes()
        t_read_end = time.perf_counter()
        print(f"[VQA] 파일 읽기: {(t_read_end - t_read)*1000:.0f}ms  ({len(video_bytes)/1024:.1f} KB)")
        print(f"[VQA] 영상 인라인 전송 중: {video_path}")

        for attempt in range(1, max_retries + 1):
            t_attempt = time.perf_counter()
            try:
                response = client.models.generate_content(
                    model=self.model_name,
                    contents=[
                        types.Part.from_bytes(data=video_bytes, mime_type="video/mp4"),
                        prompt,
                    ],
                )
                elapsed = (time.perf_counter() - t_attempt) * 1000
                print(f"[VQA] API 시도 {attempt}: 성공  ({elapsed:.0f}ms)")
                return response.text  # type: ignore[no-any-return]
            except (genai_errors.ServerError, genai_errors.ClientError) as e:
                elapsed = (time.perf_counter() - t_attempt) * 1000
                if attempt == max_retries:
                    print(f"[VQA] API 시도 {attempt}: 실패  ({elapsed:.0f}ms) — {e.__class__.__name__}")
                    raise
                wait = 2 ** attempt
                print(f"[VQA] API 시도 {attempt}: 실패  ({elapsed:.0f}ms) — {e.__class__.__name__}, {wait}초 후 재시도...")
                time.sleep(wait)

    def record_and_ask(self, prompt: str) -> str:
        """전체 파이프라인: duration초 녹화 → Gemini 전송 → 응답 반환."""
        path = self.record()
        return self.ask(path, prompt)
