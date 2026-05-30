from __future__ import annotations

import os
from typing import Any, Dict, List, Tuple

from app.config import MIN_RESULT_GAP_SECONDS
from app.utils import min_adjacent_gap, stable_hash


def validate_results(results: List[Dict[str, Any]]) -> Tuple[float, bool]:
    gap = min_adjacent_gap(results)
    return gap, gap >= MIN_RESULT_GAP_SECONDS


def build_result_signature(results: List[Dict[str, Any]]) -> str:
    """
    동일 기록/동일 순위 조합 중복 방지용 signature
    """
    core = [
        {
            "rank": r["rank"],
            "bib": r["bib"],
            "time": r["time"],
        }
        for r in sorted(results, key=lambda x: x["rank"])
    ]
    return stable_hash({"results": core})


def delete_file_if_exists(path: str | None) -> None:
    if path and os.path.exists(path):
        os.remove(path)