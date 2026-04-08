# 1. 빌드 스테이지
FROM amazoncorretto:21 AS builder
WORKDIR /workspace

# 빌드할 서비스 이름을 인자로 받음 (기본값 설정 권장)
ARG SERVICE=main

# Gradle 래퍼 및 설정 파일 복사
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# 윈도우 줄바꿈 문자(CRLF) 변환
RUN sed -i 's/\r$//' gradlew

# 모든 모듈의 build.gradle 복사 (캐싱 최적화)
# 모듈이 추가될 때마다 여기에 명시해야 합니다.
COPY common/build.gradle ./common/build.gradle
COPY gateway/build.gradle ./gateway/build.gradle
COPY auth/build.gradle ./auth/build.gradle
COPY main/build.gradle ./main/build.gradle
COPY queue/build.gradle ./queue/build.gradle


# 의존성 먼저 다운로드 (코드 변경 시에도 캐시 활용)
RUN ./gradlew :${SERVICE}:dependencies --no-daemon || true

# 모든 소스 코드 복사
COPY . .

# [핵심 수정] 복사된 윈도우용 gradlew를 다시 리눅스용으로 변환
RUN sed -i 's/\r$//' gradlew && chmod +x ./gradlew

# 실행 가능한 jar 생성 및 위치 고정
# -x test를 추가하여 빌드 속도를 높일 수 있습니다.
RUN ./gradlew :${SERVICE}:bootJar --no-daemon -x test \
    && cp /workspace/${SERVICE}/build/libs/*.jar /workspace/app.jar

# 2. 실행 스테이지 (알파인 이미지를 사용하여 용량 다이어트)
# jdk 대신 jre 이미지를 사용하면 보안과 용량 면에서 더 유리합니다.
FROM amazoncorretto:21-alpine AS runtime
WORKDIR /app

# 빌드 결과물만 복사
COPY --from=builder /workspace/app.jar app.jar

# 타임존 설정 (로그 시간 일치를 위함)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

EXPOSE 8080

# ENTRYPOINT 최적화
# sh -c를 쓰지 않아도 JAVA_TOOL_OPTIONS는 JVM이 자동으로 읽어들입니다.
ENTRYPOINT ["java", "-jar", "app.jar"]

# # 1. 빌드 스테이지 (개발팀 검증 원본 이미지 사용)
# FROM amazoncorretto:21 AS builder
# WORKDIR /workspace

# # Gradle 래퍼 및 설정 파일 복사
# COPY gradlew gradlew.bat settings.gradle build.gradle ./
# COPY gradle ./gradle

# # [추가] 윈도우 줄바꿈 문자(CRLF)를 리눅스용(LF)으로 변환
# RUN sed -i 's/\r$//' gradlew

# # [핵심 수정] 모든 모듈의 build.gradle 복사 (의존성 캐싱용)
# COPY common/build.gradle ./common/build.gradle
# COPY gateway/build.gradle ./gateway/build.gradle
# COPY auth/build.gradle ./auth/build.gradle
# COPY main/build.gradle ./main/build.gradle
# COPY queue/build.gradle ./queue/build.gradle

# RUN chmod +x ./gradlew

# # 빌드할 서비스 지정 (예: main)
# ARG SERVICE

# # 의존성 구조 파악 및 라이브러리 다운로드 (캐싱 레이어)
# RUN ./gradlew :${SERVICE}:dependencies --no-daemon

# # [핵심 수정] 모든 모듈의 소스 코드 복사
# # 개발팀의 모듈 간 참조(project(':auth') 등)가 있을 경우 빌드 실패를 방지하기 위함
# COPY common ./common
# COPY gateway ./gateway
# COPY auth ./auth
# COPY main ./main
# COPY queue ./queue

# # 해당 서비스의 executable jar 생성
# RUN ./gradlew :${SERVICE}:bootJar --no-daemon \
#     && cp /workspace/${SERVICE}/build/libs/*.jar /workspace/app.jar

# # 2. 실행 스테이지 (인프라 경량화 및 EKS 최적화)
# FROM amazoncorretto:21-alpine-jdk AS runtime
# WORKDIR /app

# # 빌드 결과물 복사
# COPY --from=builder /workspace/app.jar app.jar

# EXPOSE 8080

# # EKS의 JAVA_TOOL_OPTIONS(메모리 설정 등)를 인식하기 위한 실행 방식
# ENTRYPOINT ["sh", "-c", "java ${JAVA_TOOL_OPTIONS} -jar app.jar"]