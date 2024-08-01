FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=/build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} yeonieum_orderservice.jar
ENTRYPOINT ["java","-jar","/yeonieum_orderservice.jar"]

FROM docker.io/jhipster/jhipster-registry:v7.1.0
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS
USER root
RUN apt update -y
RUN apt install curl jq -y
COPY entrypoint.sh /entrypoint.sh
EXPOSE 8070
ENTRYPOINT ["sh", "/entrypoint.sh"]