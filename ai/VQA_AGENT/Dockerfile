# 1. 베이스 이미지 (EKS AMD64 환경용)
FROM --platform=linux/amd64 python:3.11-slim

# 2. 필수 리눅스 패키지 설치 (네트워크 에러 방지 설정 추가)
RUN apt-get update -o Acquire::Retries=3 && \
    apt-get install -y --no-install-recommends \
    libglib2.0-0 \
    libgl1-mesa-dev \
    && rm -rf /var/lib/apt/lists/*

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 의존성 파일 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install --no-cache-dir fastapi uvicorn

# 5. 소스 코드 복사
COPY . .

# 6. 환경 변수 설정
ENV PYTHONUNBUFFERED=1
ENV PYTHONPATH=/app

# 7. 실행 명령어 (FastAPI 서버 모드)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]