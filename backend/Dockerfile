FROM amazoncorretto:21 AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY gateway/build.gradle ./gateway/build.gradle
COPY main/build.gradle ./main/build.gradle

RUN chmod +x ./gradlew

ARG SERVICE

RUN ./gradlew :${SERVICE}:dependencies --no-daemon

COPY ${SERVICE} ${SERVICE}
RUN ./gradlew :${SERVICE}:bootJar --no-daemon \
    && cp /workspace/${SERVICE}/build/libs/*.jar /workspace/app.jar

FROM amazoncorretto:21-alpine-jdk AS runtime

WORKDIR /app

COPY --from=builder /workspace/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
