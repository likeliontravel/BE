# Single Stage -> Multi Stage로 변경
#
# - EC2에 jar파일 전송 불필요
# - 빌드를 먼저 수행해서 빌드 단계에만 필요한 Gradle, JDK, 빌드 캐시, 소스코드 버리고 최종 이미지에 Jar 파일만 건네주고 JDK가 아닌 가벼운 JRE를 실행에 사용
# - JDK 에만 있는 javac(컴파일러), jshell(REPL), jmap(메모리 덤프 도구)를 포함하지 않게 되어 공격받을 표면적이 작아짐.
#
# ========== Stage 1: Build ==========
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# 의존성 다운로드 감소를 위한 Docker 레이어 캐시 활용 : 빌드 설정 파일 먼저 복사 (의존성 레이어 캐시 활용) -> 의존성 다운로드 -> 소스(src) 복사를 마지막에 해서 빌드
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .

RUN chmod +x gradlew

# 의존성 다운로드 (소스 변경 시에도 이 레이어까지는 캐시 재사용)
RUN ./gradlew dependencies --no-daemon

# 소스 복사 후 빌드
COPY src src

RUN ./gradlew bootJar --no-daemon -x test

# ========== Stage 2: Run ==========
FROM eclipse-temurin:17-jre-jammy

LABEL authors="yeboong99"

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/build/libs/toleave-be-0.0.1.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD curl -sf http://localhost:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
