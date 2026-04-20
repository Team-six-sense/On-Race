# 서버 구축 방법 (Linux)

# 초기값은 권한 설정이 필요한 폴더여서 바탕 화면으로 이동
cd ~

# 진행에 필요한 프로그램 설치 및 설치 확인
sudo yum install git -y
git --version
sudo yum install python3-pip -y
pip3 --version
sudo yum install wget -y

# git clone
git clone https://github.com/URL
cd 매크로 탐지 폴더

# 모델 다운로드
mkdir models
cd models
wget --no-check-certificate 'https://drive.google.com/uc?export=download&id=파일ID' -O mouse_macro_model.json

####################################################
# 서버 실행 방법 (Linux)

# bat 파일 권한 부여 (내부적으로 Linux에서도 동작하도록 설계)
cd ..
sed -i 's/\r$$//' run_server.bat
chmod +x run_server.bat

# 가상환경 생성
python3 -m venv venv
source venv/bin/activate
pip3 install -r requirements.txt

# 서버 실행
bash run_server.bat

####################################################
# 서버 구축 방법 (Windows)

# git clone
클론할 폴더로 이동
git clone https://github.com/URL
모델 생성(생성 방법은 하단에 기술) 혹은 다운로드

####################################################
# 서버 실행 방법 (Windows)

# 라이브러리 설치
pip install -r requirements.txt

# 서버 실행
run_server.bat

####################################################
# 테스트 방법(Windows)

data\dataset\test 폴더에 테스트 진행할 마우스 데이터(.json) 추가
test\test_client.py VSCode에서 실행

####################################################
# 모델 생성 방법 (Windows)

(코드가 clone이 되어있다는 가정 하에 진행)

# 데이터셋 추가
data\dataset 폴더에 폴더(사람일 경우 human, 매크로일 경우 macro) 생성 후 해당 폴더에 마우스 데이터(.json) 추가
mk_model.bat 실행
models 폴더 내에 mouse_macro_model.json이 추가되었는지 확인

####################################################
# 설정 파일(config.py) 설명

파일 경로는 고정이므로 수정 시 오류
MACRO_THRESHOLD : 모델에서 나온 값이 해당값 이상일 경우, 매크로로 판단
SERVER_LISTEN_HOST : 서버 연결을 허용할 호스트
SERVER_PORT : 수신 연결을 허용할 포트
SERVER_REQUEST_HOST : 테스트 진행 시 서버와 연결할 호스트