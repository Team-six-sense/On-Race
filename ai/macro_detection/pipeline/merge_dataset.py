import os
import json
import glob
from config import HUMAN_DIR, MACRO_DIR, DATA_DIR

def merge_datasets_to_json(output_filename=os.path.join(DATA_DIR, 'initial_training_data.json')):
    print("폴더에서 데이터를 읽어와 병합을 시작합니다..")

    os.makedirs(HUMAN_DIR, exist_ok=True)
    os.makedirs(MACRO_DIR, exist_ok=True)

    all_sessions_data = []
    human_count = 0
    macro_count = 0

    human_files = glob.glob(os.path.join(HUMAN_DIR, "*.json"))
    for filepath in human_files:
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                points = json.load(f)
                
            # 파일명을 session_id로 활용 (예: test_human_01.json -> human_test_human_01)
            filename = os.path.basename(filepath).replace('.json', '')
            
            all_sessions_data.append({
                "session_id": f"human_{filename}",
                "is_macro": 0,  # False를 의미하는 0 배정
                "points": points
            })
            human_count += 1
        except Exception as e:
            print(f"파일 읽기 실패 ({filepath}): {e}")

    # 매크로 데이터도 동일한 방식으로 처리 (session_id에 'macro_' 접두어 추가)
    macro_files = glob.glob(os.path.join(MACRO_DIR, "*.json"))
    for filepath in macro_files:
        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                points = json.load(f)
                
            filename = os.path.basename(filepath).replace('.json', '')
            
            all_sessions_data.append({
                "session_id": f"macro_{filename}",
                "is_macro": 1,  # True를 의미하는 1 배정
                "points": points
            })
            macro_count += 1
        except Exception as e:
            print(f"파일 읽기 실패 ({filepath}): {e}")

    # 최종 병합
    if human_count == 0 and macro_count == 0:
        print("폴더에 JSON 파일이 없습니다.")
        return

    print(f"병합 진행 중: (사람: {human_count}건, 매크로: {macro_count}건, 총 {human_count + macro_count}건)")
    
    with open(output_filename, 'w', encoding='utf-8') as f:
        json.dump(all_sessions_data, f, ensure_ascii=False, indent=2)

    print(f"'{output_filename}' 파일이 성공적으로 생성되었습니다.")

if __name__ == "__main__":
    merge_datasets_to_json()