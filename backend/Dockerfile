# 1. 빌드 스테이지
FROM amazoncorretto:17-alpine AS builder
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 권한 부여 및 의존성 다운로드
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드 (테스트 제외)
COPY src src
RUN ./gradlew clean build -x test --no-daemon

# 2. 실행 스테이지
FROM amazoncorretto:17-alpine
WORKDIR /app

# 빌드된 jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# EKS app-api.tf 설정에 맞춘 8080 포트 [cite: 592]
EXPOSE 8080

# JVM 옵션 및 실행 (환경변수는 EKS에서 주입됨 [cite: 596])
ENTRYPOINT ["sh", "-c", "java ${JAVA_TOOL_OPTIONS} -jar app.jar"]