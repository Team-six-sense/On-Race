from __future__ import annotations
import os
from dotenv import load_dotenv  

load_dotenv()

# 1. 인프라 및 실행 환경 (Infrastructure)
TEMP_ROOT = os.getenv("VQA_TEMP_ROOT", "temp_batches")
# 병렬 처리를 위한 컨커런시 및 배치 사이즈
WORKER_CONCURRENCY = int(os.getenv("VQA_WORKER_CONCURRENCY", "4"))
BATCH_SIZE = int(os.getenv("VQA_BATCH_SIZE", "8"))

# 2. AWS S3 & CDN 설정 (Storage)
AWS_REGION = os.getenv("AWS_REGION", "ap-northeast-2")

S3_BUCKET_NAME = os.getenv("VQA_S3_BUCKET", "t6-on-race-ai-vqa-data-prod")
S3_VIDEO_PREFIX = os.getenv("VQA_S3_VIDEO_PREFIX", "vqa/temp/video")
S3_AUDIO_PREFIX = os.getenv("VQA_S3_AUDIO_PREFIX", "vqa/temp/audio")

# CloudFront 도메인 (나중에 Signed URL 생성 시 필요할 수 있음)
CLOUDFRONT_DOMAIN = os.getenv("VQA_CLOUDFRONT_DOMAIN", "cdn.example.com")

# 3. 데이터베이스 설정 (Database - MySQL)
DB_ENDPOINT = os.getenv("DB_ENDPOINT")
DB_USER = os.getenv("DB_USERNAME")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_NAME = os.getenv("DB_NAME")
DB_PORT = int(os.getenv("DB_PORT", 3306))

# 4. AI 모델 및 콘텐츠 설정
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")

# 5.텍스트 및 스크립트 생성 모델
SCRIPT_MODEL = os.getenv("VQA_SCRIPT_MODEL", "gpt-5.4-mini") 
QUESTIONS_PER_VIDEO = int(os.getenv("VQA_QUESTIONS_PER_VIDEO", "3"))

# 6.영상 내 이벤트 정합성 체크 기준
MIN_RESULT_GAP_SECONDS = float(os.getenv("VQA_MIN_RESULT_GAP_SECONDS", "0.05"))

# 7.TTS 설정
TTS_MODEL = os.getenv("VQA_TTS_MODEL", "gpt-4o-mini-tts")
TTS_VOICE = os.getenv("VQA_TTS_VOICE", "alloy")
TTS_SPEED = float(os.getenv("VQA_TTS_SPEED", "1.4"))