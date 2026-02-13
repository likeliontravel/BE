# Emergency Deployment Record (2026-02-13)

긴급 배포 시 변경한 내용을 기록한 문서. 추후 CI/CD 파이프라인 구축 시 참고용.

---

## 배포 환경

- **EC2**: `ubuntu@43.202.152.36` (`~/toleave_yechan/`)
- **도메인**: `api.toleave.cloud` (백엔드), `toleave.cloud` (프론트엔드)
- **컨테이너 구성**: MySQL 8.0 + Redis 7 + Spring Boot App + Next.js Frontend + Nginx

---

## 1. application.yml (기본 프로필 변경)

**변경 내용**: `spring.profiles.active` 값을 `local` → `prod`로 변경 (1줄)

```yaml
# 변경 전
  profiles:
    active: local

# 변경 후
  profiles:
    active: prod
```

나머지 내용은 동일 (변경 없음).

---

## 2. application-prod.yml (Redis 설정 추가)

### 변경 전

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  cloud:
    gcp:
      project-id: ${GCP_PROJECT_ID}
      credentials:
        location: ${GCP_CREDENTIALS_PATH}
```

### 변경 후

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

  cloud:
    gcp:
      project-id: ${GCP_PROJECT_ID}
      credentials:
        location: ${GCP_CREDENTIALS_PATH}

logging:
  level:
    org.springframework.security: WARN
    org.example.be: INFO
```

**변경점**: `spring.data.redis` 섹션 추가, production용 logging 레벨 추가 (security DEBUG → WARN)

---

## 3. Dockerfile

### 변경 전

```dockerfile
FROM openjdk:17-jdk-slim

LABEL authors="yeboong99"

WORKDIR /app

COPY toleave-be-0.0.1.jar app.jar
COPY toleave-b9a7b3a17267.json /app/toleave-b9a7b3a17267.json

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 변경 후

```dockerfile
FROM eclipse-temurin:17-jdk-jammy

LABEL authors="yeboong99"

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY toleave-be-0.0.1.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD curl -sf http://localhost:8080/error || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**변경점**:
- 베이스 이미지 `openjdk:17-jdk-slim` → `eclipse-temurin:17-jdk-jammy` (openjdk 이미지 Docker Hub에서 삭제됨)
- GCP 인증파일 `COPY` 제거 (docker-compose에서 볼륨 마운트로 대체)
- `curl` 설치 + `HEALTHCHECK` 추가

---

## 4. docker-compose.yml

### 변경 전 (EC2 기존 상태)

```yaml
version: "3.8"

services:
  db:
    image: mysql:8.0
    container_name: mysql-db
    environment:
      MYSQL_DATABASE: toleave
      MYSQL_ROOT_PASSWORD: 19990524
      TZ: Asia/Seoul
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - toleave-network

  app:
    image: toleave_yechan-app
    container_name: toleave-app
    ports:
      - "8080:8080"
    depends_on:
      - db
    networks:
      - toleave-network

  frontend:
    image: jwj9127/toleave-frontend:latest
    container_name: toleave-frontend
    restart: always
    networks:
      - toleave-network

  nginx:
    image: nginx:latest
    container_name: toleave-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
      - /etc/letsencrypt:/etc/letsencrypt
    depends_on:
      - app
      - frontend
    networks:
      - toleave-network

volumes:
  mysql-data:

networks:
  toleave-network:
    driver: bridge
```

### 변경 후

```yaml
version: '3.8'

services:
  db:
    image: mysql:8.0
    container_name: mysql-db
    environment:
      MYSQL_DATABASE: toleave
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      TZ: Asia/Seoul
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - toleave-network
    restart: unless-stopped
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost" ]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - toleave-network
    restart: unless-stopped
    healthcheck:
      test: [ "CMD", "redis-cli", "ping" ]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: toleave-app
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - toleave-network
    restart: unless-stopped
    env_file:
      - .env
    environment:
      SPRING_PROFILES_ACTIVE: prod
    volumes:
      - ${GCP_CREDENTIALS_HOST_PATH}:/app/credentials/toleave-b9a7b3a17267.json:ro

  frontend:
    image: jwj9127/toleave-frontend:latest
    container_name: toleave-frontend
    restart: always
    networks:
      - toleave-network

  nginx:
    image: nginx:latest
    container_name: toleave-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
      - /etc/letsencrypt:/etc/letsencrypt
      - ./nginx/certbot/www:/var/www/certbot
    depends_on:
      - app
      - frontend
    networks:
      - toleave-network
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:

networks:
  toleave-network:
```

**변경점**:
- **Redis 서비스 추가** (`redis:7-alpine`, 데이터 볼륨, 헬스체크)
- **MySQL 비밀번호 환경변수화**: 하드코딩 `19990524` → `${MYSQL_ROOT_PASSWORD}`
- **App 서비스 변경**: `image:` → `build:` 방식, `env_file: .env`로 전체 환경변수 주입, `SPRING_PROFILES_ACTIVE: prod`, GCP 인증파일 볼륨 마운트 (`:ro`), `depends_on` 헬스체크 조건 추가
- **모든 서비스에 `restart: unless-stopped`** 추가
- **DB/Redis 헬스체크** 추가 (app이 DB/Redis healthy 확인 후 시작)
- **certbot 웹루트 볼륨** 추가 (`./nginx/certbot/www:/var/www/certbot`)
- `redis-data` 볼륨 추가

---

## 5. nginx/default.conf

### 변경 전 (EC2 기존 상태)

```nginx
server {
    listen 80;
    server_name toleave.cloud www.toleave.cloud;

       location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name toleave.cloud www.toleave.cloud;

    ssl_certificate     /etc/letsencrypt/live/toleave.cloud/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/toleave.cloud/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://toleave-frontend:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}

server {
    listen 443 ssl http2;
    server_name api.toleave.cloud;

    ssl_certificate     /etc/letsencrypt/live/api.toleave.cloud/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.toleave.cloud/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://toleave-app:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

### 변경 후

```nginx
server {
    listen 80;
    server_name toleave.cloud www.toleave.cloud api.toleave.cloud;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name toleave.cloud www.toleave.cloud;

    ssl_certificate     /etc/letsencrypt/live/toleave.cloud/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/toleave.cloud/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://toleave-frontend:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}

server {
    listen 443 ssl http2;
    server_name api.toleave.cloud;

    ssl_certificate     /etc/letsencrypt/live/api.toleave.cloud/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.toleave.cloud/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;

    location /ws {
        proxy_pass http://toleave-app:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;

        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }

    location / {
        proxy_pass http://toleave-app:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }
}
```

**변경점**:
- HTTP 블록에 `api.toleave.cloud` 추가 (HTTP→HTTPS 리다이렉트)
- `return 301`을 `location /` 안으로 이동 (certbot 인증서 갱신 정상 작동 위해)
- **WebSocket `/ws` location 블록 추가** (STOMP 채팅 지원: Upgrade 헤더, 3600s 타임아웃)
- 프론트엔드 블록은 변경 없음

---

## 6. Java 소스코드 도메인 변경

도메인 만료로 인해 `toleave.shop` → `toleave.cloud`로 전체 변경.

| 파일 | 라인 | 변경 내용 |
|------|------|----------|
| `chat/config/WebSocketConfig.java` | :29 | `.setAllowedOrigins("...", "https://toleave.shop")` → `"https://toleave.cloud"` |
| `security/config/WebConfig.java` | :36 | `.allowedOrigins("...", "https://toleave.shop")` → `"https://toleave.cloud"` |
| `security/config/WebConfig.java` | :42 | `.allowedOrigins("...", "https://toleave.shop")` → `"https://toleave.cloud"` |
| `security/config/SecurityConfig.java` | :123 | `List.of("...", "https://toleave.shop")` → `"https://toleave.cloud"` |
| `group/invitation/controller/InvitationJoinController.java` | :61 | `"https://api.toleave.shop/login?..."` → `"https://api.toleave.cloud/login?..."` |
| `group/invitation/controller/InvitationJoinController.java` | :66 | `"https://toleave.shop/login"` → `"https://toleave.cloud/login"` |

---

## 7. EC2용 .env 파일 (신규 생성)

EC2 `~/toleave_yechan/.env`에 배치. 주요 환경별 차이:

| 변수 | 로컬 (.env) | EC2 (.env) |
|------|-------------|------------|
| `DB_URL` | `jdbc:mysql://127.0.0.1:3306/...` | `jdbc:mysql://mysql-db:3306/...` |
| `REDIS_HOST` | `localhost` | `redis` |
| `GCP_CREDENTIALS_PATH` | 로컬 파일 절대경로 | `file:/app/credentials/toleave-b9a7b3a17267.json` |
| OAuth REDIRECT_URI들 | `http://localhost:8080/...` | `https://api.toleave.cloud/...` |

EC2 전용 신규 변수:
- `MYSQL_ROOT_PASSWORD` — MySQL 컨테이너 비밀번호
- `REDIS_HOST`, `REDIS_PORT` — Docker Redis 컨테이너 연결
- `GCP_CREDENTIALS_HOST_PATH` — EC2 호스트의 GCP JSON 파일 절대 경로 (볼륨 마운트 소스)

---

## 8. 배포 명령어 (재배포 시 참고)

```bash
# 로컬에서 빌드
cd backend
./gradlew clean bootJar

# EC2로 전송
scp -i ~/Desktop/sshkey/toleaveKey.pem build/libs/toleave-be-0.0.1.jar ubuntu@43.202.152.36:~/toleave_yechan/
scp -i ~/Desktop/sshkey/toleaveKey.pem Dockerfile ubuntu@43.202.152.36:~/toleave_yechan/
scp -i ~/Desktop/sshkey/toleaveKey.pem docker-compose.yml ubuntu@43.202.152.36:~/toleave_yechan/

# EC2에서 실행
cd ~/toleave_yechan
docker compose down
docker compose up --build -d
docker compose logs -f app

# JAR만 변경 시 (앱 컨테이너만 재빌드)
docker compose up --build -d app
```

---

## TODO (추후 개선사항)

- [ ] CI/CD 파이프라인 구축 (GitHub Actions 등)
- [ ] SSL 인증서 자동 갱신 cron 설정
- [ ] Java 코드의 하드코딩된 도메인 URL을 환경변수로 분리 (`InvitationJoinController`)
- [ ] CORS 설정을 환경변수 기반으로 변경 (도메인 변경 시 코드 수정 불필요하도록)
- [ ] EC2 인스턴스 메모리 모니터링 (MySQL + Redis + App + Nginx 동시 구동)
- [ ] Docker image 태그 버전 관리 (현재 JAR 버전 고정: 0.0.1)
