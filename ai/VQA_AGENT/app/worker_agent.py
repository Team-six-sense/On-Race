from __future__ import annotations
import json
import os
from typing import Any, Dict, List, Optional, TypedDict

from langgraph.graph import END, START, StateGraph

from app.utils import generate_secure_id
from app.db_manager import exists_same_result_signature, save_payload
from app.question_generator import generate_questions
from app.script_generator import generate_broadcast_script
from app.storage_manager import (
    build_question_records,
    build_s3_keys,
    build_video_record,
    upload_file_to_s3,
)
from app.tts_generator import generate_tts_audio
from app.video_generator import generate_marathon_params, generate_marathon_video_logic
from app.video_validator import build_result_signature, delete_file_if_exists, validate_results


class WorkerState(TypedDict):
    job_id: str
    secure_id: str
    temp_dir: str

    params: Optional[Dict[str, Any]]
    local_video_path: Optional[str]
    local_audio_path: Optional[str]

    results: Optional[List[Dict[str, Any]]]
    validation_gap: Optional[float]
    valid: bool

    result_signature: Optional[str]
    is_duplicate: bool

    questions: Optional[List[Dict[str, Any]]]
    commentary_script: Optional[str]

    video_key: Optional[str]
    audio_key: Optional[str]

    status: str
    error: Optional[str]


def node_generate_video(state: WorkerState) -> WorkerState:
    params = generate_marathon_params()
    local_video_path = os.path.join(state["temp_dir"], f"{state['secure_id']}.mp4")
    results = generate_marathon_video_logic(params, local_video_path)

    return {
        **state,
        "params": params,
        "local_video_path": local_video_path,
        "results": results,
        "status": "video_generated",
        "error": None,
    }


def node_validate_video(state: WorkerState) -> WorkerState:
    assert state["results"] is not None

    gap, valid = validate_results(state["results"])
    signature = build_result_signature(state["results"])
    is_duplicate = exists_same_result_signature(signature)

    return {
        **state,
        "validation_gap": gap,
        "valid": valid,
        "result_signature": signature,
        "is_duplicate": is_duplicate,
        "status": "validated",
    }


def route_after_validate(state: WorkerState) -> str:
    if state["valid"] and not state["is_duplicate"]:
        return "generate_questions"

    if state["local_video_path"]:
        delete_file_if_exists(state["local_video_path"])

    return "failed"


def node_failed(state: WorkerState) -> WorkerState:
    return {
        **state,
        "status": "failed",
        "error": state.get("error") or "validation_or_duplicate_failed",
    }


def node_generate_questions(state: WorkerState) -> WorkerState:
    assert state["results"] is not None
    questions = generate_questions(state["results"], state["job_id"])

    return {
        **state,
        "questions": questions,
        "status": "questions_generated",
    }


def node_generate_script(state: WorkerState) -> WorkerState:
    assert state["results"] is not None
    script = generate_broadcast_script(state["results"])

    return {
        **state,
        "commentary_script": script,
        "status": "script_generated",
    }


def node_generate_tts(state: WorkerState) -> WorkerState:
    assert state["commentary_script"] is not None

    local_audio_path = os.path.join(state["temp_dir"], f"{state['secure_id']}.mp3")
    generate_tts_audio(state["commentary_script"], local_audio_path)

    return {
        **state,
        "local_audio_path": local_audio_path,
        "status": "tts_generated",
    }


def node_upload_and_save(state: WorkerState) -> WorkerState:
    assert state["local_video_path"] is not None
    assert state["local_audio_path"] is not None
    assert state["commentary_script"] is not None
    assert state["results"] is not None
    assert state["questions"] is not None
    assert state["validation_gap"] is not None
    assert state["result_signature"] is not None
    
    # S3 키 및 레코드 객체 생성
    video_key, audio_key = build_s3_keys(state["secure_id"])

    video_record = build_video_record(
        video_id=state["job_id"],
        video_key=video_key,
        audio_key=audio_key,
        commentary_script=state["commentary_script"],
        results=state["results"],
        validation_gap=state["validation_gap"],
        result_signature=state["result_signature"],
    )

    question_records = build_question_records(state["questions"], state["job_id"])

    # 로컬에 JSON 결과물 저장 (직접 확인용)
    result_path = os.path.join(state["temp_dir"], "debug_result.json")
    try:
        with open(result_path, "w", encoding="utf-8") as f:
            debug_data = {
                "video": video_record,
                "questions": question_records
            }
            json.dump(debug_data, f, indent=4, ensure_ascii=False)
        print(f"📝 [로컬 테스트] JSON 저장 완료: {result_path}")
    except Exception as e:
        print(f"⚠️ JSON 저장 중 오류 발생: {e}")

    

    # upload_file_to_s3(state["local_video_path"], video_key)
    # upload_file_to_s3(state["local_audio_path"], audio_key)

    
    save_payload(video_record, question_records)

    # delete_file_if_exists(state["local_video_path"])
    # delete_file_if_exists(state["local_audio_path"])

    # return {
    #     **state,
    #     "video_key": video_key,
    #     "audio_key": audio_key,
    #     "status": "completed",
    # }
    return {"status": "completed"}


def build_worker_graph():
    graph = StateGraph(WorkerState)

    graph.add_node("generate_video", node_generate_video)
    graph.add_node("validate_video", node_validate_video)
    graph.add_node("failed", node_failed)
    graph.add_node("generate_questions", node_generate_questions)
    graph.add_node("generate_script", node_generate_script)
    graph.add_node("generate_tts", node_generate_tts)
    graph.add_node("upload_and_save", node_upload_and_save)

    graph.add_edge(START, "generate_video")
    graph.add_edge("generate_video", "validate_video")

    graph.add_conditional_edges(
        "validate_video",
        route_after_validate,
        {
            "generate_questions": "generate_questions",
            "failed": "failed",
        },
    )

    graph.add_edge("generate_questions", "generate_script")
    graph.add_edge("generate_script", "generate_tts")
    graph.add_edge("generate_tts", "upload_and_save")
    graph.add_edge("upload_and_save", END)
    graph.add_edge("failed", END)

    return graph.compile()


graph = build_worker_graph()


def create_initial_worker_state(job_id: str, temp_dir: str) -> WorkerState:
    return {
        "job_id": job_id,
        "secure_id": generate_secure_id(),
        "temp_dir": temp_dir,
        "params": None,
        "local_video_path": None,
        "local_audio_path": None,
        "results": None,
        "validation_gap": None,
        "valid": False,
        "result_signature": None,
        "is_duplicate": False,
        "questions": None,
        "commentary_script": None,
        "video_key": None,
        "audio_key": None,
        "status": "planned",
        "error": None,
    }