# syntax=docker/dockerfile:1
FROM gradle:7.4.2-jdk11-alpine AS TEMP_BUILD_IMAGE
# COPY build.gradle.kts settings.gradle.kts gradle.properties compass-download-kotlin/build.gradle.kts gecco-easy/build.gradle.kts gecco-questionnaire/build.gradle.kts /home/gradle/src

#COPY gradle $APP_HOME/gradle
COPY --chown=gradle:gradle . /home/gradle/src/
WORKDIR /home/gradle/src
# USER root
#RUN chown -R gradle /home/gradle/src

#RUN gradle build || return 0
#COPY . .
RUN gradle clean shadowJar --no-daemon

# actual container
FROM adoptopenjdk/openjdk11:alpine-jre
ENV ARTIFACT_NAME=compass-interface-codex-cli.jar
ENV APP_HOME=/home/gradle/src

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

#ENV COMPASS_BACKEND_URL=${BACKEND_URL}
#ENV COMPASS_API_ID=${API_ID}
#ENV COMPASS_API_KEY=${API_
#ENV PRIVATE_KEY
#ENV PUBLIC_KEY
#ENV CERTIFICATE
#ENV FHIRSERVER
ENV TZ="GMT+01:00"

ENTRYPOINT exec java -cp ${ARTIFACT_NAME} Server_pollingKt