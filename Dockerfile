# syntax=docker/dockerfile:1
FROM gradle:7.4.2-jdk11-alpine AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle.kts settings.gradle.kts $APP_HOME

COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src
USER root
RUN chown -R gradle /home/gradle/src

RUN gradle build || return 0
COPY . .
RUN gradle clean shadowJar

# actual container
FROM adoptopenjdk/openjdk11:alpine-jre
ENV ARTIFACT_NAME=compass-interface-codex-cli.jar
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

ARG BACKEND_URL=http://localhost:8080
ENV COMPASS_BACKEND_URL=${BACKEND_URL}
ARG API_ID
ENV COMPASS_API_ID=${API_ID}
ARG API_KEY
ENV COMPASS_API_KEY=${API_KEY}
ARG PRIVATE_KEY
ENV

EXPOSE 8080
ENTRYPOINT exec java -cp ${ARTIFACT_NAME} Server_pollingKt