# syntax=docker/dockerfile:1
FROM gradle:8.6.0-jdk21-alpine AS TEMP_BUILD_IMAGE

COPY --chown=gradle:gradle . /home/gradle/src/
WORKDIR /home/gradle/src

RUN gradle clean shadowJar --no-daemon

# actual container
FROM eclipse-temurin:21-alpine
ENV ARTIFACT_NAME=compass-interface-codex-cli.jar
ENV APP_HOME=/home/gradle/src

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

# Set the timezone, which is important for the JWT tokens used to connect to compass-numapp-backend
ENV TZ="GMT+01:00"

ENTRYPOINT exec java -cp ${ARTIFACT_NAME} Server_pollingKt