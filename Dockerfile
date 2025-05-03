FROM ubuntu:24.04

ARG VERSION

RUN mkdir /app
WORKDIR /app

RUN apt update && apt upgrade -y
RUN apt install -y openjdk-17-jdk

COPY target/D2RaidSchedulerDiscordBot-${VERSION}.jar /app/D2RaidSchedulerDiscordBot.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","D2RaidSchedulerDiscordBot.jar"]