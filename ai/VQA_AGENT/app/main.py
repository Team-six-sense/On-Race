from __future__ import annotations
import time
import os
import sys
from fastapi import FastAPI, BackgroundTasks
import uvicorn
from app.supervisor_agent import run_multi_agent_pipeline

app = FastAPI()

# [설정] 환경변수나 상수로 관리
TARGET_COUNT = 20  # 나중에 3000으로 수정
BATCH_SIZE = 5

def run_vqa_pipeline():
    """기존의 대규모 생성 로직을 담은 함수"""
    print(f"\n{'='*50}")
    print(f"🚀 VQA 대규모 데이터 생성 파이프라인 시작 (Background)")
    print(f"🎯 목표 수량: {TARGET_COUNT}개")
    print(f"📦 배치 크기: {BATCH_SIZE}개")
    print(f"{'='*50}\n")

    start_time = time.time()

    try:
        # 파이프라인 실행
        result = run_multi_agent_pipeline(
            target_count=TARGET_COUNT,
            batch_size=BATCH_SIZE,
        )

        end_time = time.time()
        duration = end_time - start_time

        # 리포트 출력
        print("\n" + "="*50)
        print("✅ 파이프라인 전체 완료")
        print(f"📈 최종 결과: {result['success_count']} / {TARGET_COUNT} 성공")
        print(f"❌ 실패 수: {result['fail_count']}")
        
        if TARGET_COUNT > 0:
            avg_time = duration / TARGET_COUNT
            print(f"⏳ 총 소요 시간: {duration/3600:.2f}시간 ({duration:.2f}초)")
            print(f"⏱️ 평균 생성 속도: {avg_time:.2f}초/개")
        print("="*50 + "\n")

        if result["fail_count"] > 0:
            print("⚠️ 실패 항목 상세:")
            for i, item in enumerate(result["results"]):
                if item["status"] != "completed":
                    print(f"   - [Job ID: {item['job_id']}] Error: {item.get('error')}")
                    if i > 20: break

    except Exception as e:
        print(f"\n 백그라운드 실행 중 에러 발생: {e}")

@app.get("/health")
def health_check():
    """인프라 팀의 Liveness/Readiness Probe를 위한 헬스체크 엔드포인트"""
    return {"status": "healthy", "target": TARGET_COUNT, "batch": BATCH_SIZE}

@app.post("/start-production")
def start_production(background_tasks: BackgroundTasks):
    """API 호출 시 생성을 시작"""
    background_tasks.add_task(run_vqa_pipeline)
    return {"message": f"VQA Production started in background. Target: {TARGET_COUNT}"}

if __name__ == "__main__":
    # 서버 실행 (프로세스 유지)
    uvicorn.run(app, host="0.0.0.0", port=8000)