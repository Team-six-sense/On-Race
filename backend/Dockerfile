# 1. 빌드 스테이지
FROM amazoncorretto:21 AS builder
WORKDIR /workspace

ARG SERVICE
RUN if [ -z "$SERVICE" ]; then echo "SERVICE argument is required"; exit 1; fi

# 설정 파일들 먼저 복사 (캐싱 레이어)
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY common/build.gradle ./common/build.gradle
COPY gateway/build.gradle ./gateway/build.gradle
COPY auth/build.gradle ./auth/build.gradle
COPY main/build.gradle ./main/build.gradle
COPY queue/build.gradle ./queue/build.gradle

# 전체 소스 코드 복사 (이때 윈도우 파일들이 덮어씌워짐)
COPY . .

# [핵심] 모든 파일 복사 후, 다시 한번 줄바꿈 수정 및 실행 권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# 의존성 다운로드 및 빌드
RUN ./gradlew :${SERVICE}:dependencies --no-daemon || true

# 빌드 및 실행 JAR 추출
RUN ./gradlew :${SERVICE}:bootJar --no-daemon -x test \
    && find /workspace/${SERVICE}/build/libs -name "*.jar" -not -name "*plain.jar" -exec cp {} /workspace/app.jar \;

# 2. 실행 스테이지
FROM amazoncorretto:21-alpine AS runtime
WORKDIR /app

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

COPY --from=builder /workspace/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]