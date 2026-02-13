FROM eclipse-temurin:17-jdk-jammy

LABEL authors="yeboong99"

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY toleave-be-0.0.1.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD curl -sf http://localhost:8080/error || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
