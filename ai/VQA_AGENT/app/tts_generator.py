from __future__ import annotations

from openai import OpenAI

from app.config import OPENAI_API_KEY, TTS_MODEL, TTS_SPEED, TTS_VOICE

def generate_tts_audio(script: str, output_path: str) -> str:
    """
    LLM이 생성한 대본을 받아 TTS 음성 파일로 변환합니다.
    """
    client = OpenAI(api_key=OPENAI_API_KEY)

    try:
        print(f"음성 합성 시작: {script[:30]}...")
        
        # [Step 1] OpenAI TTS API 호출
        response = client.audio.speech.create(
            model=TTS_MODEL,
            voice=TTS_VOICE,
            input=script,
            speed=TTS_SPEED,
        )

        # [Step 2] 파일 저장
        response.write_to_file(output_path)
        
        return output_path

    except Exception as e:
        return None