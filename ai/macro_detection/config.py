import os

# 데이터 및 모델 디렉토리 설정
BASE_DIR = os.path.dirname(os.path.abspath(__file__))           # 현재 파일의 디렉토리

DATA_DIR = os.path.join(BASE_DIR, 'data')                       # 데이터 디렉토리
MODEL_DIR = os.path.join(BASE_DIR, 'models')                    # 모델 디렉토리
MODEL_FILE = os.path.join(MODEL_DIR, 'mouse_macro_model.json')  # 모델 파일 경로
TEST_DIR = os.path.join(DATA_DIR, 'dataset', 'test')            # 테스트 데이터 디렉토리
HUMAN_DIR = os.path.join(DATA_DIR, 'dataset', 'human')          # 사용자 데이터 디렉토리
MACRO_DIR = os.path.join(DATA_DIR, 'dataset', 'macro')          # 매크로 데이터 디렉토리
TEMP_DIR = os.path.join(DATA_DIR, 'dataset', 'temp')            # 임시 파일 디렉토리

# 로그 파일 경로
SERVER_LOG_DIR = os.path.join(BASE_DIR, 'app','logs')           # 서버 로그 디렉토리

# 모델 파일 경로
MODEL_FILENAME = 'mouse_macro_model.json'                       # 모델 파일명
MODEL_PATH = os.path.join(MODEL_DIR, MODEL_FILENAME)            # 모델 파일 경로

# 학습 데이터 파일 경로
TRAIN_DATA_FILENAME = 'initial_training_data.json'              # 학습 데이터 파일명
TRAIN_DATA_PATH = os.path.join(DATA_DIR, TRAIN_DATA_FILENAME)   # 학습 데이터 파일 경로

COLLECT_MACRO_DATA = False                                      # 매크로 데이터 수집 여부 (True: 수집, False: 수집 안 함(기본값))
COLLECT_HUMAN_DATA = False                                      # 사용자 데이터 수집 여부 (True: 수집, False: 수집 안 함(기본값))
MACRO_THRESHOLD = 0.80                                          # 매크로 판별 임계값 (0.0 ~ 1.0 사이)

# API 서버 설정
SERVER_LISTEN_HOST = "0.0.0.0"                                  # API 실행 호스트
SERVER_REQUEST_HOST = "127.0.0.1"                               # API 요청 호스트
SERVER_PORT = 8000                                              # 서버 포트