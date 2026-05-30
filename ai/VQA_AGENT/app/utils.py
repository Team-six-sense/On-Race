from __future__ import annotations

import hashlib
import json
from datetime import datetime
from typing import Any, Dict, List
import secrets

def generate_secure_id() -> str:
    """보안적으로 안전한 32바이트 고유ID 생성"""
    return secrets.token_urlsafe(32)

def utc_now_iso() -> str:
    """서버에 파일 저장 시 영상 생성 시간 설정"""
    return datetime.utcnow().isoformat(timespec="seconds") + "Z"


def today_path_parts() -> tuple[str, str, str]:
    """표준 형식에서 날짜만 반환"""
    now = datetime.utcnow()
    return f"{now.year:04d}", f"{now.month:02d}", f"{now.day:02d}"


def time_to_seconds(t: str) -> float:
    """계산을 위해 기록을 초 단위 숫자로 변경"""
    h, m, s = t.split(":")
    return int(h) * 3600 + int(m) * 60 + float(s)


def min_adjacent_gap(results: List[Dict[str, Any]]) -> float:
    """선수들 사이 간격 계산 """
    times = sorted(time_to_seconds(r["time"]) for r in results)
    gaps = [times[i + 1] - times[i] for i in range(len(times) - 1)]
    return min(gaps)


def stable_hash(payload: Dict[str, Any]) -> str:
    """해시 값 부여"""
    normalized = json.dumps(payload, ensure_ascii=False, sort_keys=True, separators=(",", ":"))
    return hashlib.sha256(normalized.encode("utf-8")).hexdigest()


_DIGIT_MAP = {
    "0": "공", "1": "일", "2": "이", "3": "삼", "4": "사",
    "5": "오", "6": "육", "7": "칠", "8": "팔", "9": "구",
}

def normalize_digits_to_korean(text: str) -> str:
    """발음 교정"""
    return "".join(_DIGIT_MAP.get(ch, ch) for ch in text)