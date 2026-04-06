: <<'BATCH'
@echo off
title AI Macro Detection Server

echo ==================================================
echo  start AI Macro Detection Server (Windows)
echo ==================================================
echo.

:: 현재 스크립트 위치로 이동
cd /d "%~dp0"

:: PYTHONPATH 설정
set PYTHONPATH=%cd%

:: 서버 실행
python app\api_server.py

pause
exit /b
BATCH

# --- 여기서부터는 리눅스/macOS 전용 셸 스크립트 영역입니다 ---
echo "=================================================="
echo " start AI Macro Detection Server (Linux/macOS)"
echo "=================================================="
echo ""

# 현재 스크립트의 절대 경로 계산
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

# PYTHONPATH 설정
export PYTHONPATH=$DIR

# python3 명령어가 있는지 확인 후 실행 (리눅스는 보통 python3 사용)
if command -v python3 >/dev/null 2>&1; then
    python3 app/api_server.py
else
    python app/api_server.py
fi