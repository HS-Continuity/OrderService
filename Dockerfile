# Use Eclipse Temurin JDK for the base image
FROM eclipse-temurin:17-jdk-alpine

# Set environment variables
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

# Update and install necessary packages
USER root
RUN apk update && apk add --no-cache curl jq

# Copy the JAR file into the container
ARG JAR_FILE=/build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} /yeonieum_orderservice.jar

# Copy the entrypoint script into the container
COPY entrypoint.sh /entrypoint.sh

# Expose the port
EXPOSE 8070

# Set the entrypoint
ENTRYPOINT ["sh", "/entrypoint.sh"]