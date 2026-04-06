# 1. 빌드 스테이지 (개발팀 검증 원본 이미지 사용)
FROM amazoncorretto:21 AS builder
WORKDIR /workspace

# Gradle 래퍼 및 설정 파일 복사
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# [핵심 수정] 모든 모듈의 build.gradle 복사 (의존성 캐싱용)
COPY common/build.gradle ./common/build.gradle
COPY gateway/build.gradle ./gateway/build.gradle
COPY auth/build.gradle ./auth/build.gradle
COPY main/build.gradle ./main/build.gradle
COPY queue/build.gradle ./queue/build.gradle

RUN chmod +x ./gradlew

# 빌드할 서비스 지정 (예: main)
ARG SERVICE

# 의존성 구조 파악 및 라이브러리 다운로드 (캐싱 레이어)
RUN ./gradlew :${SERVICE}:dependencies --no-daemon

# [핵심 수정] 모든 모듈의 소스 코드 복사
# 개발팀의 모듈 간 참조(project(':auth') 등)가 있을 경우 빌드 실패를 방지하기 위함
COPY common ./common
COPY gateway ./gateway
COPY auth ./auth
COPY main ./main
COPY queue ./queue

# 해당 서비스의 executable jar 생성
RUN ./gradlew :${SERVICE}:bootJar --no-daemon \
    && cp /workspace/${SERVICE}/build/libs/*.jar /workspace/app.jar

# 2. 실행 스테이지 (인프라 경량화 및 EKS 최적화)
FROM amazoncorretto:21-alpine-jdk AS runtime
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /workspace/app.jar app.jar

EXPOSE 8080

# EKS의 JAVA_TOOL_OPTIONS(메모리 설정 등)를 인식하기 위한 실행 방식
ENTRYPOINT ["sh", "-c", "java ${JAVA_TOOL_OPTIONS} -jar app.jar"]