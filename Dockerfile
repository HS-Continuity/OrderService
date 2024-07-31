FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=/build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} yeonieum_orderservice.jar
ENTRYPOINT ["java","-jar","/yeonieum_orderservice.jar"]