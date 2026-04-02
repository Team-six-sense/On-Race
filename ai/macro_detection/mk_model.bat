@echo off
title Data Pipeline
color 0B

cd /d "%~dp0"

:: 현재 폴더를 파이썬 기본 경로로 지정합니다.
set PYTHONPATH=%cd%

echo --------------------------------------------------
echo 데이터 병합 진행
echo --------------------------------------------------
python pipeline\merge_dataset.py

:: 데이터 병합 에러 체크
if %errorlevel% neq 0 (
    echo.
    echo 데이터 병합 중 에러 발생
    pause
    exit /b %errorlevel%
)

echo 데이터 병합 완료
echo.

echo --------------------------------------------------
echo 모델 학습 진행
echo --------------------------------------------------
python pipeline\macro_learning.py

:: 모델 학습 에러 체크
if %errorlevel% neq 0 (
    echo.
    echo 모델 학습 중 에러 발생
    pause
    exit /b %errorlevel%
)

echo 모델 학습 완료
echo.

echo ==================================================
echo 파이프라인 완료
echo ==================================================

pause