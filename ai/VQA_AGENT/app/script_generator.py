from __future__ import annotations

from typing import Any, Dict, List

from openai import OpenAI

from app.config import OPENAI_API_KEY, SCRIPT_MODEL
from app.utils import normalize_digits_to_korean


def _build_summary(results: List[Dict[str, Any]]) -> str:
    lines = []
    for item in sorted(results, key=lambda x: x["rank"]):
        lines.append(
            f"- {item['rank']}등: 배번 {item['bib']}"
        )
    return "\n".join(lines)


def generate_broadcast_script(results: List[Dict[str, Any]]) -> str:
    if not OPENAI_API_KEY:
        return "❌ 오류: OPENAI_API_KEY가 설정되지 않았습니다. .env 파일을 확인하세요."
    """
    실제 중계 대본 생성.
    """
    client = OpenAI(api_key=OPENAI_API_KEY)
    data_summary = _build_summary(results)

    prompt = f"""
당신은 스포츠 캐스터입니다. 아래 경주 결과를 바탕으로 '12초' 이내에 읽을 수 있는 중계 대본을 작성하세요.

[데이터]
{data_summary}

[가이드라인]
1. 반드시 "선수들 결승선을 향해 달려옵니다! 과연 1등은 누구일까요! 결과 발표합니다!"로 시작하세요.
2. 모든 선수의 등수와 배번만 언급하세요. 레인 번호는 언급하지 마세요.
3. 시각 장애인이 듣고 문제를 풀 수 있도록 숫자는 명확하게 표현하세요.
4. 12초라는 짧은 시간 제한이 있으므로 불필요한 미사여구는 빼고 정보 전달에 집중하세요.
5. 배번 다음에는 '번 선수', 등수 다음에는 조사 '은'을 붙여주세요.
6. 가장 마지막에는 "선수입니다!"로 끝내주세요.
7. 오직 대본 내용만 출력하세요.
8. 모든 숫자는 반드시 한글 발음(일, 이, 삼, 사, 오, 육, 칠, 팔, 구, 공)으로 변환하여 출력하세요.
예: 7087 → 칠공팔칠, 2등 → 이등, 3번 → 삼번

[예시]
"선수들 결승선을 향해 달려옵니다! 과연 1등은 누구일까요! 결과 발표합니다! 1등은 칠일공삼번 선수!  2등은 사삼육이번 선수! 3등은 삼삼칠사번 선수! 4등은 일사이공번 선수입니다!."
"""

    # LLM 호출 
    try:
        response = client.chat.completions.create(
            model=SCRIPT_MODEL,
            messages=[
                {"role": "system", "content": "당신은 빠르고 정확하게 정보를 전달하는 스포츠 캐스터입니다."},
                {"role": "user", "content": prompt}
            ],
            temperature=0.5  
        )
        
        script = response.choices[0].message.content.strip()
        return normalize_digits_to_korean(script)

    except Exception as e:
        print(f"🚨 OpenAI API 상세 에러: {str(e)}")
        return f"LLM 호출 중 오류 발생: {e}"
