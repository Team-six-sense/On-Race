import os
import json
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

app = FastAPI()

# 💡 최상위 경로 설정
BASE_DIR = "/Users/techup/VQA_AGENT/temp"
app.mount("/static", StaticFiles(directory=BASE_DIR), name="static")

templates = Jinja2Templates(directory="templates")

@app.get("/", response_class=HTMLResponse)
async def list_folders(request: Request):
    """temp 폴더 내의 각 작업 폴더(marathon_...) 리스트 출력"""
    # 폴더명만 추출 (예: marathon_02Pbhns...)
    folders = [d for d in os.listdir(BASE_DIR) if os.path.isdir(os.path.join(BASE_DIR, d))]
    return templates.TemplateResponse(
    request=request, 
    name="index.html", 
    context={"folders": folders}
)

@app.get("/verify/{folder_name}", response_class=HTMLResponse)
async def verify_task(request: Request, folder_name: str):
    folder_path = os.path.join(BASE_DIR, folder_name)
    
    # 1. 💡 방어 코드: 요청받은 이름이 실제 폴더가 아니면 무시하거나 에러 처리
    if not os.path.isdir(folder_path):
        # 만약 브라우저가 파비콘이나 엉뚱한 파일을 요청한 경우 404를 반환
        raise HTTPException(status_code=404, detail="폴더를 찾을 수 없습니다.")

    # 2. 영상 파일 찾기 (.mp4)
    video_files = [f for f in os.listdir(folder_path) if f.endswith(".mp4")]
    video_file = video_files[0] if video_files else None
    
    # 3. 음성 파일 찾기 (.mp3)
    audio_files = [f for f in os.listdir(folder_path) if f.endswith(".mp3")]
    audio_file = audio_files[0] if audio_files else None

    video_url = f"/static/{folder_name}/{video_file}" if video_file else None
    audio_url = f"/static/{folder_name}/{audio_file}" if audio_file else None

    # 3. 결과 JSON 읽기 (debug_result.json)
    json_path = os.path.join(folder_path, "debug_result.json")
    
    questions = []
    commentary = "스크립트 정보 없음"
    
    if os.path.exists(json_path):
        try:
            with open(json_path, "r", encoding="utf-8") as f:
                data = json.load(f)
                
                # 1. 💡 'video' 딕셔너리를 먼저 가져옵니다.
                video_info = data.get("video", {})
                
                # 2. 💡 video 안에서 스크립트를 찾고, 없으면 최상위에서도 한 번 더 찾습니다.
                commentary = video_info.get("commentary_script") or \
                             data.get("commentary_script") or \
                             video_info.get("script") or \
                             "스크립트 정보를 찾을 수 없습니다."
                
                # 3. 💡 퀴즈 정보 가져오기 (제공해주신 JSON 상 최상위에 있음)
                questions = data.get("questions", [])

        except Exception as e:
            commentary = f"JSON 읽기 오류: {str(e)}"
            
    return templates.TemplateResponse(
    request=request, 
    name="verify.html", 
    context={
        "video_url": video_url,
        "audio_url": audio_url,
        "commentary": commentary,
        "questions": questions,
        "folder_name": folder_name
    }
)