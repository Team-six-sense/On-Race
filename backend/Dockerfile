# 1. 빌드 스테이지 (Java 21)
FROM amazoncorretto:21-alpine AS builder
WORKDIR /workspace

# Gradle 설정 파일 복사
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY common ./common

# 빌드할 서비스의 build.gradle 및 소스 복사 (build-arg SERVICE 사용)
ARG SERVICE
COPY ${SERVICE}/build.gradle ./${SERVICE}/build.gradle
RUN chmod +x ./gradlew

# 의존성 다운로드 및 빌드
RUN ./gradlew :${SERVICE}:dependencies --no-daemon
COPY ${SERVICE} ${SERVICE}
RUN ./gradlew :${SERVICE}:bootJar --no-daemon \
    && cp /workspace/${SERVICE}/build/libs/*.jar /workspace/app.jar

# 2. 실행 스테이지 (Java 21)
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=builder /workspace/app.jar app.jar

EXPOSE 8080

# JVM 옵션은 EKS app-api.tf에서 주입됨
ENTRYPOINT ["sh", "-c", "java ${JAVA_TOOL_OPTIONS} -jar app.jar"]