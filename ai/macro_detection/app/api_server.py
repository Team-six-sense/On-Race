import os
import json
from datetime import datetime
from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from typing import List
import uvicorn
from contextlib import asynccontextmanager

from config import MODEL_FILE, SERVER_LISTEN_HOST, SERVER_PORT, SERVER_LOG_DIR
from app.macro_detector import MouseMacroDetector 

detector = None

stats = {
    "total_requests": 0,
    "macro_detected": 0,
    "human_detected": 0
}

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 서버 구동 준비
    global detector
    print(f"[{datetime.now()}] 서버 구동 준비")
    
    try:
        detector = MouseMacroDetector(MODEL_FILE)
        print("AI 모델 로드 성공")
    except Exception as e:
        raise RuntimeError(f"AI 모델 로드 실패: {e}") 
        
    print("서버 구동 준비 완료")
    yield # 서버 실행

    print("서버 종료")
    detector = None

app = FastAPI(
    title="Mouse Macro Detection API", 
    version="1.0",
    lifespan=lifespan
)

class MouseEvent(BaseModel):
    x: float
    y: float
    timestamp: float
    eventType: str

class MacroDetectionRequest(BaseModel):
    user_id: str
    events: List[MouseEvent]

def write_detect_log_to_file(user_id: str, is_macro: bool, result: dict, total_events: int, events_data: list):
    os.makedirs(SERVER_LOG_DIR, exist_ok=True)
    # 이벤트 수와 매크로 여부를 하루 단위 로그 파일에 기록 (예: 20260331.log)
    timestamp = datetime.now().strftime("%Y%m%d")
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    log_filename = f"{timestamp}.log"
    
    log_line = f"[{now}] 탐지 완료 - 사용자 ID: {user_id}, 이벤트 수: {total_events}, 매크로 여부: {is_macro}\n"
    with open(os.path.join(SERVER_LOG_DIR, log_filename), "a", encoding="utf-8") as f:
        if is_macro:
            events_str = json.dumps(events_data, ensure_ascii=False)
            log_line = f"[{now}] 매크로 사용자 - 사용자 ID: {user_id}, 탐지 결과: {result}, 이벤트 수: {total_events}\n이벤트 데이터: {events_str}\n"
        else:
            log_line = f"[{now}] 일반 사용자 - 사용자 ID: {user_id}, 탐지 결과: {result}, 이벤트 수: {total_events}\n"
        f.write(log_line)

@app.post("/api/v1/detect-macro")
async def detect_macro(payload: MacroDetectionRequest, background_tasks: BackgroundTasks):
    print("마우스 매크로 감지 요청 수신")
    stats["total_requests"] += 1

    user_id = payload.user_id
    events = payload.events

    raw_data_list = [event.model_dump() for event in events]
    
    result = detector.predict_macro(raw_data_list)
    
    if result.get("success"):
        is_macro = result.get("is_macro")
        if is_macro:
            stats["macro_detected"] += 1
        else:
            stats["human_detected"] += 1

        # 감지 결과와 이벤트 수를 로그 파일에 비동기로 기록
        background_tasks.add_task(
            write_detect_log_to_file, 
            user_id = user_id, 
            is_macro = is_macro, 
            result = result,
            total_events = len(events),
            events_data = raw_data_list
        )

        return result
    else:
        raise HTTPException(status_code=400, detail=result.get("error_message"))

# 헬스 체크 엔드포인트
@app.get("/health")
def health_check():
    return {"status": "alive", "model_ready": True}

if __name__ == "__main__":
    uvicorn.run("app.api_server:app", host=SERVER_LISTEN_HOST, port=SERVER_PORT, reload=True, access_log=False)