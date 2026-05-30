from __future__ import annotations
import os
from typing import Any, Dict, List, Tuple
import boto3
from botocore.exceptions import ClientError
from app.config import (
AWS_REGION,
S3_BUCKET_NAME, S3_VIDEO_PREFIX, S3_AUDIO_PREFIX
)
from app.utils import today_path_parts, utc_now_iso

def build_s3_keys(video_id: str) -> Tuple[str, str]:
	"""오늘 날짜 기반의 S3 Key 생성 (vqa/video/2026/03/24/uuid.mp4)"""
	yyyy, mm, dd = today_path_parts()
	video_key = f"{S3_VIDEO_PREFIX}/{yyyy}/{mm}/{dd}/{video_id}.mp4"
	audio_key = f"{S3_AUDIO_PREFIX}/{yyyy}/{mm}/{dd}/{video_id}.mp3"
	return video_key, audio_key
	
def upload_file_to_s3(local_path: str, s3_key: str) -> bool:
    """로컬 파일을 S3 버킷으로 업로드"""
    # 1. 💡 s3_client 생성 
    s3_client = boto3.client("s3", region_name=AWS_REGION)

    # 2. 💡 Content-Type 설정 준비
    extra_args = {}
    if s3_key.endswith(".mp4"):
        extra_args['ContentType'] = 'video/mp4'
    elif s3_key.endswith(".mp3"):
        extra_args['ContentType'] = 'audio/mpeg'

    try:
        s3_client.upload_file(
            local_path, 
            S3_BUCKET_NAME, 
            s3_key,
            ExtraArgs=extra_args  
        )
        print(f"✅ S3 Upload Success: {s3_key}")
        return True
    except ClientError as e:
        print(f"❌ S3 Upload Failed: {e}")
        return False
	# print(f" [로컬 테스트] S3 업로드 건너뜀: {local_path} -> {s3_key}")
    # return f"https://s3-mock-url/{s3_key}"
		
		
def build_video_record(
    video_id: str,
    video_key: str,
    audio_key: str,
    commentary_script: str,
    results: List[Dict[str, Any]],
    validation_gap: float,
    result_signature: str,
) -> Dict[str, Any]:
    """DB의 'videos' 테이블 포맷에 맞게 딕셔너리 생성"""
    return {
        "id": video_id,
        "video_key": video_key,
        "audio_key": audio_key,
        "commentary_script": commentary_script,
        "results": results,     # DB 저장 시 JSON 문자열로 변환됨
        "validation_gap": validation_gap,
        "result_signature": result_signature,
        "status": "usable",
        "created_at": utc_now_iso(),
    }
	
	
def build_question_records(questions: List[Dict[str, Any]], video_id: str) -> List[Dict[str, Any]]:
    """DB의 'questions' 테이블 포맷에 맞게 리스트 생성"""
    records = []
    for q in questions:
        records.append({
            "id": q["id"],
            "video_id": video_id,
            "question_text": q["question_text"],
            "answer": q["answer"],
            "options": q["options"], # DB 저장 시 JSON 문자열로 변환됨
            "q_type": q.get("q_type", "multiple_choice"),
            "difficulty": q.get("difficulty", "medium"),
            "created_at": utc_now_iso(),
        })
    return records
