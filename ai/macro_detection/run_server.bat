@echo off
title AI Macro Detection Server

echo ==================================================
echo  start AI Macro Detection Server
echo ==================================================
echo.

cd /d "%~dp0"

set PYTHONPATH=%cd%

python app\api_server.py

pause