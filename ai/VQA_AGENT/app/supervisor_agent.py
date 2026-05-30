from __future__ import annotations

import os
import shutil
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Any, Dict, List

from app.config import BATCH_SIZE, TEMP_ROOT, WORKER_CONCURRENCY
from app.db_manager import init_db
from app.worker_agent import graph as WORKER_GRAPH, create_initial_worker_state
from app.utils import generate_secure_id


def run_single_job(job_state: Dict[str, Any]) -> Dict[str, Any]:
    """단일 워커 실행 및 결과 요약"""
    result = WORKER_GRAPH.invoke(job_state)
    return {
        "job_id": result["job_id"],
        "status": result["status"],
        "error": result.get("error"),
    }


def run_multi_agent_pipeline(
    target_count: int = 3000,
    batch_size: int = BATCH_SIZE,
    temp_root: str = TEMP_ROOT,
) -> Dict[str, Any]:
    # 1. DB 초기화
    init_db()

    # 2. 임시 작업 공간 정리
    if os.path.exists(temp_root):
        shutil.rmtree(temp_root)
    os.makedirs(temp_root, exist_ok=True)

    all_results: List[Dict[str, Any]] = []
    planned_jobs: List[Dict[str, Any]] = []

    # 3. 작업 계획 수립
    for _ in range(target_count):
        secure_id = generate_secure_id()
        job_id = f"marathon_{secure_id}"

        batch_dir = os.path.join(temp_root, job_id)
        os.makedirs(batch_dir, exist_ok=True)
        planned_jobs.append(create_initial_worker_state(job_id, batch_dir))

    # 4. 병렬 실행
    for i in range(0, len(planned_jobs), batch_size):
        batch_jobs = planned_jobs[i:i + batch_size]

        with ThreadPoolExecutor(max_workers=WORKER_CONCURRENCY) as executor:
            # 워커들에게 일 시키기
            futures = [executor.submit(run_single_job, job) for job in batch_jobs]

            for future in as_completed(futures):
                all_results.append(future.result())

    success_count = len([r for r in all_results if r["status"] == "completed"])
    fail_count = len([r for r in all_results if r["status"] != "completed"])

    print(f"\n✅ 빌드 완료!")
    print(f" - 총 목표: {target_count}")
    print(f" - 성공: {success_count}")
    print(f" - 실패: {fail_count}")

    return {
        "target_count": target_count,
        "success_count": success_count,
        "fail_count": fail_count,
        "results": all_results,
    }