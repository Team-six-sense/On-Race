: <<'BATCH'
@echo off
title Data Pipeline
color 0B

echo ==================================================
echo  Data Pipeline (Windows)
echo ==================================================

cd /d "%~dp0"
set PYTHONPATH=%cd%

echo --------------------------------------------------
echo 데이터 병합 진행
echo --------------------------------------------------
python pipeline\merge_dataset.py
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] 데이터 병합 중 에러 발생
    pause
    exit /b %errorlevel%
)
echo 데이터 병합 완료
echo.

echo --------------------------------------------------
echo 모델 학습 진행
echo --------------------------------------------------
python pipeline\macro_learning.py
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] 모델 학습 중 에러 발생
    pause
    exit /b %errorlevel%
)
echo 모델 학습 완료
echo.

echo ==================================================
echo 파이프라인 완료
echo ==================================================
pause
exit /b
BATCH

# --- 여기서부터는 리눅스/macOS 영역입니다 ---
echo "=================================================="
echo " Data Pipeline (Linux/macOS)"
echo "=================================================="

# 현재 스크립트 위치로 이동 및 PYTHONPATH 설정
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"
export PYTHONPATH=$DIR

# 파이썬 명령어 설정 (python3 우선)
PYTHON_CMD="python3"
if ! command -v python3 >/dev/null 2>&1; then
    PYTHON_CMD="python"
fi

echo "--------------------------------------------------"
echo "데이터 병합 진행"
echo "--------------------------------------------------"
$PYTHON_CMD pipeline/merge_dataset.py
if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] 데이터 병합 중 에러 발생"
    read -p "Press Enter to continue..."
    exit 1
fi
echo "데이터 병합 완료"
echo ""

echo "--------------------------------------------------"
echo "모델 학습 진행"
echo "--------------------------------------------------"
$PYTHON_CMD pipeline/macro_learning.py
if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] 모델 학습 중 에러 발생"
    read -p "Press Enter to continue..."
    exit 1
fi
echo "모델 학습 완료"
echo ""

echo "=================================================="
echo "파이프라인 완료"
echo "=================================================="
read -p "Press Enter to close..."