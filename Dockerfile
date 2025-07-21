FROM openjdk:17-jdk-slim

LABEL authors="yeboong99"

WORKDIR /app

COPY build/libs/toleave-be-0.0.1.jar app.jar
COPY toleave-b9a7b3a17267.json /app/toleave-b9a7b3a17267.json

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
