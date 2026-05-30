from __future__ import annotations

import random
import uuid
from typing import Any, Dict, List

from app.config import QUESTIONS_PER_VIDEO
from app.utils import time_to_seconds

def generate_obstacle_question(results: List[Dict[str, Any]]) -> Dict[str, Any]:
    """
    장애물을 밟고 느려진 러너의 수를 묻는 질문
    """
    # hit_slow가 True인 러너들만 필터링
    slow_runners = [r for r in results if r.get('hit_slow', False)]
    count = len(slow_runners)
    
    q = "코스 위에서 '감속 아이템'을 밟아 일시적으로 속도가 느려진 러너는 총 몇 명입니까?"
    ans = f"{count}명"
    
    # 보기 생성: 0명부터 최대 인원수(보통 4명)까지 생성 후 셔플
    opts = [f"{i}명" for i in range(len(results) + 1)]
    random.shuffle(opts)

    return {
        "question_text": q,
        "answer": ans,
        "options": opts,
        "q_type": "obstacle_count",
        "difficulty": "medium" # 과정 분석이 필요하므로 medium 이상
    }

def generate_easy_question(results: List[Dict[str, Any]]) -> Dict[str, Any]:
    q_type = random.choice(["rank_of_bib", "bib_of_rank", "time_check"])
    target = random.choice(results)

    if q_type == "rank_of_bib":
        q, ans = f"배번 {target['bib']}번 러너는 몇 등으로 들어왔습니까?", f"{target['rank']}등"
        opts = [f"{i}등" for i in range(1, len(results) + 1)]
    elif q_type == "bib_of_rank":
        q, ans = f"{target['rank']}등으로 들어온 러너의 배번은 무엇입니까?", target['bib']
        opts = [r['bib'] for r in results]
    else: # time_check
        q, ans = f"배번 {target['bib']}번 러너의 기록은 무엇입니까?", target['time']
        opts = [ans]
        while len(opts) < 4:
            fake = f"02:15:{random.uniform(10, 25):05.2f}"
            if fake not in opts: opts.append(fake)

    random.shuffle(opts)
    return {"question_text": q, "answer": ans, "options": opts, "q_type": q_type, "difficulty": "easy",}

def generate_hard_question(results: List[Dict[str, Any]]) -> Dict[str, Any]:
    """
    사용자 요청 기반 고난도 문제 유형 2가지:
    1. 두 러너 간의 시간 차이(초) 계산
    2. 두 러너의 순위 합산 계산
    """
    q_type = random.choice(["time_gap", "rank_sum"])

    # 1. 두 러너의 기록 차이 (초 단위)
    if q_type == "time_gap":
        r1, r2 = random.sample(results, 2)
        # 문자열 시간을 초로 변환하여 차이 계산 (절대값)
        t1 = time_to_seconds(r1["time"])
        t2 = time_to_seconds(r2["time"])
        gap = round(abs(t1 - t2), 2)
        
        q = f"{r1['bib']}번과 {r2['bib']}번 러너의 기록 차이는 몇 초입니까?"
        ans = f"{gap:.2f}초"
        
        # 오답 생성: 실제 차이에 ±0.5~2.0초 랜덤 가감
        opts = [ans]
        while len(opts) < 4:
            fake_val = abs(round(gap + random.uniform(-2.0, 2.0), 2))
            fake = f"{fake_val:.2f}초"
            if fake not in opts:
                opts.append(fake)

    # 2. 두 러너의 순위 합 (새로 추가된 유형)
    else: # rank_sum
        r1, r2 = random.sample(results, 2)
        total_rank_sum = r1['rank'] + r2['rank']
        
        q = f"{r1['bib']}번과 {r2['bib']}번 러너의 순위의 합은 몇 입니까?"
        ans = str(total_rank_sum)
        
        # 오답 생성: 가능한 순위 합의 범위 내에서 랜덤 추출 (예: 1+2=3 ~ 3+4=7)
        opts = [ans]
        possible_sums = list(range(3, (len(results) * 2))) # 4명 기준 3~7 정도
        while len(opts) < 4:
            fake = str(random.choice(possible_sums))
            if fake not in opts:
                opts.append(fake)

    random.shuffle(opts)

    return {"question_text": q, "answer": ans, "options": opts, "q_type": q_type, "difficulty": "hard"}

def generate_questions(results: List[Dict[str, Any]], video_id: str) -> List[Dict[str, Any]]:
    """
    질문 생성 메인 함수:
    - 순서 고정: [Easy, Hard, Easy]
    - 공통 키 구조 적용: id, video_id, q, a, opts, type
    """
    questions: List[Dict[str, Any]] = []
    
    # 순서를 [Easy, Hard, Easy]로 고정
    fixed_difficulty_pattern = ["medium", "hard", "easy"]

    for pattern in fixed_difficulty_pattern:
        if pattern == "medium": 
            base = generate_obstacle_question(results)
        elif pattern == "hard":
            base = generate_hard_question(results)
        else: # easy
            base = generate_easy_question(results)
            
        # 데이터 통합 (UUID와 video_id 추가)
        questions.append({
            "id": str(uuid.uuid4()),
            "video_id": video_id,
            "question_text": base["question_text"],
            "answer": base["answer"],
            "options": base["options"],
            "q_type": base["q_type"],
            "difficulty": base["difficulty"]
        })

    return questions