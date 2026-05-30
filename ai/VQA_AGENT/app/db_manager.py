from __future__ import annotations
import os
import json
import pymysql
from contextlib import contextmanager
from typing import Any, Dict, List

from app.config import DB_ENDPOINT, DB_USER, DB_PASSWORD, DB_NAME, DB_PORT

# 실제 DB 연결을 시도하지 않도록 컨텍스트 매니저를 비워둡니다.
from contextlib import contextmanager

# @contextmanager
# def get_db_conn():
#     """DB 연결 없이 가짜 연결 유지"""
#     yield None

# def init_db() -> None:
#     print("⚠️ [DEBUG] 로컬 파일 저장 모드입니다. DB 초기화를 건너뜁니다.")

# def exists_same_result_signature(result_signature: str) -> bool:
#     return False

# def save_payload(video_record: Dict[str, Any], question_records: List[Dict[str, Any]]) -> None:
#     """
#     DB 대신 temp/{video_id}/debug_result.json 경로에 데이터를 저장합니다.
#     """
#     video_id = video_record["id"]
#     # 폴더 구조: temp/marathon_UUID/
#     target_dir = os.path.join("temp", video_id)
    
#     # 만약 폴더가 없으면 생성 (보통 worker_agent에서 생성하지만 안전을 위해)
#     if not os.path.exists(target_dir):
#         os.makedirs(target_dir, exist_ok=True)

#     file_path = os.path.join(target_dir, "debug_result.json")

#     # DB 테이블 구조와 유사하게 데이터 병합
#     payload = {
#         "video": video_record,
#         "questions": question_records
#     }

#     try:
#         with open(file_path, "w", encoding="utf-8") as f:
#             json.dump(payload, f, indent=4, ensure_ascii=False)
        
#         print("\n" + "="*50)
#         print(f"✅ [로컬 저장 완료] ID: {video_id}")
#         print(f"📂 저장 경로: {file_path}")
#         print(f"📹 포함된 영상 파일: {video_id}.mp4")
#         print(f"❓ 질문 개수: {len(question_records)}개")
#         print("="*50 + "\n")
#     except Exception as e:
#         print(f"❌ 파일 저장 중 에러 발생: {e}")

# def check_latest_data(limit: int = 5) -> None:
#     print("🔍 temp 폴더 내의 폴더 목록을 확인하세요.")

# if __name__ == "__main__":
#     print("현재 디버그 모드입니다.")

@contextmanager
def get_db_conn():
    """MySQL 연결을 위한 컨텍스트 매니저"""
    conn = pymysql.connect(
        host=DB_ENDPOINT,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        port=DB_PORT,
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=False,  # 트랜잭션 수동 제어
        ssl={'ca': None},             # SSL 활성화 (인증서 경로는 None이어도 됨)
        ssl_verify_cert=False,       # Proxy의 자체 서명 인증서 검증 건너뛰기
        ssl_verify_identity=False,   # 호스트 이름 검증 건너뛰기
    )
    try:
        yield conn
        conn.commit()
    except Exception as e:
        conn.rollback()
        print(f"❌ DB 트랜잭션 에러: {e}")
        raise e
    finally:
        conn.close()

def init_db() -> None:
    """MySQL 테이블 초기화"""
    with get_db_conn() as conn:
        with conn.cursor() as cursor:
            # Videos 테이블 (JSON 타입 지원하는 MySQL 5.7+ 기준)
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS videos (
                    id VARCHAR(64) PRIMARY KEY,
                    video_key VARCHAR(255) NOT NULL,
                    audio_key VARCHAR(255) NOT NULL,
                    commentary_script TEXT NOT NULL,
                    results_json JSON NOT NULL,
                    validation_gap DOUBLE NOT NULL,
                    result_signature VARCHAR(128) UNIQUE NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    created_at DATETIME NOT NULL,
                    INDEX idx_status (status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """)

            # Questions 테이블
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS questions (
                    id VARCHAR(64) PRIMARY KEY,
                    video_id VARCHAR(64) NOT NULL,
                    question_text TEXT NOT NULL,
                    answer VARCHAR(255) NOT NULL,
                    options_json JSON NOT NULL,
                    q_type VARCHAR(50) NOT NULL,
                    difficulty VARCHAR(20) NOT NULL,
                    created_at DATETIME NOT NULL,
                    FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """)

def exists_same_result_signature(result_signature: str) -> bool:
    """중복 생성 방지를 위한 시그니처 체크"""
    with get_db_conn() as conn:
        with conn.cursor() as cursor:
            cursor.execute("SELECT id FROM videos WHERE result_signature = %s", (result_signature,))
            return cursor.fetchone() is not None

def save_payload(video_record: Dict[str, Any], question_records: List[Dict[str, Any]]) -> None:
    """Video와 Questions를 하나의 트랜잭션으로 묶어 저장"""
    with get_db_conn() as conn:
        with conn.cursor() as cursor:
            # 1. Video 저장
            video_sql = """
                INSERT INTO videos (
                    id, video_key, audio_key, commentary_script,
                    results_json, validation_gap,
                    result_signature, status, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """
            cursor.execute(video_sql, (
                video_record["id"],
                video_record["video_key"],
                video_record["audio_key"],
                video_record["commentary_script"],
                json.dumps(video_record["results"], ensure_ascii=False),
                video_record["validation_gap"],
                video_record["result_signature"],
                video_record["status"],
                video_record["created_at"],
            ))

            # 2. Questions 저장
            question_sql = """
                INSERT INTO questions (
                    id, video_id, question_text, answer,
                    options_json, q_type, difficulty, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """
            for q in question_records:
                cursor.execute(question_sql, (
                    q["id"],
                    q["video_id"],
                    q["question_text"],
                    q["answer"],
                    json.dumps(q["options"], ensure_ascii=False),
                    q["q_type"],
                    q["difficulty"],
                    q["created_at"],
                ))
    print(f"✅ DB 저장 완료 (Video ID: {video_record['id']})")

def check_latest_data(limit: int = 5) -> None:
    """최근 저장된 비디오와 연결된 퀴즈 데이터를 출력하여 확인"""
    print(f"\n🔍 최근 {limit}개의 데이터를 조회합니다...\n" + "="*50)
    
    with get_db_conn() as conn:
        with conn.cursor() as cursor:
            # 1. 최신 비디오 5건 조회
            cursor.execute("""
                SELECT id, video_key, validation_gap, status, created_at 
                FROM videos 
                ORDER BY created_at DESC 
                LIMIT %s
            """, (limit,))
            videos = cursor.fetchall()

            if not videos:
                print("📭 저장된 데이터가 없습니다.")
                return

            for v in videos:
                print(f"📹 [Video] ID: {v['id']} | Status: {v['status']}")
                print(f"   - Key: {v['video_key']}")
                print(f"   - Gap: {v['validation_gap']}s | Created: {v['created_at']}")
                
                # 2. 해당 비디오에 속한 퀴즈 조회
                cursor.execute("""
                    SELECT question_text, answer, options_json 
                    FROM questions 
                    WHERE video_id = %s
                """, (v['id'],))
                questions = cursor.fetchall()
                
                for i, q in enumerate(questions, 1):
                    options = json.loads(q['options_json']) # JSON 문자열을 리스트로 복원
                    print(f"     ❓ Q{i}: {q['question_text']}")
                    print(f"        Ans: {q['answer']} | Options: {options}")
                print("-" * 50)


if __name__ == "__main__":
    # 1. 처음이라면 테이블 생성
    # init_db()

    # 2. 데이터가 잘 들어왔는지 확인하고 싶을 때만 호출
    check_latest_data(3)