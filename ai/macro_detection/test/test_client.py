import os
import glob
import json
import time
import requests
import sys

current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from config import SERVER_REQUEST_HOST, SERVER_PORT, TEST_DIR

API_URL = f"http://{SERVER_REQUEST_HOST}:{SERVER_PORT}/api/v1/detect-macro"

def run_automated_tests():
    if not os.path.exists(TEST_DIR):
        print(f"❌ '{TEST_DIR}' 폴더가 없습니다. 폴더를 만들고 JSON 파일들을 넣어주세요.")
        return

    test_files = glob.glob(os.path.join(TEST_DIR, "*.json"))
    
    if not test_files:
        print(f"❌ '{TEST_DIR}' 폴더에 JSON 파일이 하나도 없습니다.")
        return

    print(f"🚀 총 {len(test_files)}개의 파일에 대해 순차적 AI 판별 테스트를 시작합니다...\n")
    print("=" * 50)

    # 1. 통계 집계용 변수 초기화
    total_macros = 0
    total_humans = 0
    error_count = 0
    
    user_id = "TEST_USER" # 테스트용 고정 사용자 ID (실제 API에서는 요청마다 달라질 수 있음)
    
    # 2. 전체 소요 시간 측정을 위한 타이머 시작
    batch_start_time = time.perf_counter()

    for filepath in test_files:
        filename = os.path.basename(filepath)
        print(f"📂 파일명: {filename}")

        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                test_payload = json.load(f)

            # 개별 판별 시간 측정
            start_time = time.perf_counter()
            response = requests.post(API_URL, json={"user_id": user_id, "events": test_payload})
            end_time = time.perf_counter()

            if response.status_code == 200:
                result = response.json()
                is_macro = result.get("is_macro")
                prob = result.get("probability", 0) * 100
                
                # 결과에 따라 카운트 1씩 증가
                if is_macro:
                    print(f"   🚨 [결과] 🤖 매크로 (Macro)  | 확률: {prob:.2f}%")
                    total_macros += 1
                else:
                    print(f"   ✅ [결과] 👤 정상 유저 (Human)| 확률: {prob:.2f}%")
                    total_humans += 1
                    
                print(f"   ⏱️ [속도] {end_time - start_time:.4f}초 소요")
                
            else:
                print(f"   ❌ [서버 에러] 상태 코드 {response.status_code}")
                print(f"   상세: {response.text}")
                error_count += 1

        except json.JSONDecodeError:
            print(f"   ⚠️ [에러] 파일이 올바른 JSON 형식이 아닙니다.")
            error_count += 1
        except requests.exceptions.ConnectionError:
            print("\n❌ 서버에 연결할 수 없습니다. 'api_server.py'가 실행 중인지 확인해 주세요!")
            return # 치명적 에러이므로 반복문을 즉시 멈춤
        except Exception as e:
            print(f"   ⚠️ [알 수 없는 에러] {e}")
            error_count += 1

        print("-" * 50)

    # 3. 전체 타이머 종료 및 계산
    batch_end_time = time.perf_counter()
    total_elapsed_time = batch_end_time - batch_start_time

    # ==========================================
    # 📊 4. 최종 결과 요약 리포트 출력
    # ==========================================
    print("\n" + "=" * 50)
    print(" 📊 [최종 테스트 결과 요약 리포트] 📊")
    print("=" * 50)
    print(f"  ▶ 총 테스트 건수 : {len(test_files)}건")
    print(f"  👤 정상 (Human) : {total_humans}건")
    print(f"  🚨 매크로 (Macro) : {total_macros}건")
    if error_count > 0:
        print(f"  ⚠️ 에러 발생     : {error_count}건")
    print("-" * 50)
    print(f"  ⏱️ 전체 소요 시간 : {total_elapsed_time:.4f}초")
    # 1건당 평균 소요 시간 (파일이 있을 경우에만 계산)
    if len(test_files) > 0:
        avg_time = total_elapsed_time / len(test_files)
        print(f"  ⚡ 1건당 평균 속도: {avg_time:.4f}초")
    print("=" * 50 + "\n")

if __name__ == "__main__":
    run_automated_tests()