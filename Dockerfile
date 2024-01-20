FROM openjdk:17

ARG BOT_VERSION

RUN mkdir /app
WORKDIR /app

COPY target/D2RaidSchedulerDiscordBot-${BOT_VERSION}.jar /app/D2RaidSchedulerDiscordBot.jar
COPY ../.env /app

CMD java -jar D2RaidSchedulerDiscordBot.jar