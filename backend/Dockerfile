FROM amazoncorretto:21 AS builder

WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY common ./common
COPY gateway/build.gradle ./gateway/build.gradle
COPY auth/build.gradle ./auth/build.gradle
COPY main/build.gradle ./main/build.gradle
COPY queue/build.gradle ./queue/build.gradle

RUN chmod +x ./gradlew
RUN sed -i 's/\r$//' ./gradlew

ARG SERVICE

RUN ./gradlew :${SERVICE}:dependencies --no-daemon

COPY ${SERVICE} ${SERVICE}
RUN ./gradlew :${SERVICE}:bootJar --no-daemon \
    && cp /workspace/${SERVICE}/build/libs/*.jar /workspace/app.jar

FROM amazoncorretto:21-alpine-jdk AS runtime

WORKDIR /app

ENV AWS_EC2_METADATA_DISABLED=true \
    AWS_METADATA_SERVICE_TIMEOUT=1

COPY --from=builder /workspace/app.jar /app/app.jar

ENTRYPOINT ["java", "-Daws.disableEc2Metadata=true", "-Daws.disableEc2MetadataV1=true", "-Daws.ec2MetadataServiceTimeout=1", "-Dcom.amazonaws.sdk.disableEc2Metadata=true", "-jar", "/app/app.jar"]
