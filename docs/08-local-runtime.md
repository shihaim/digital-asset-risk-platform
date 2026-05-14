# Docker Compose 및 로컬 실행 환경

이 문서는 Bash 기준으로 로컬 개발 환경을 실행하고, API/Kafka/DB/Redis/Kafka UI 동작을 확인하는 절차를 정리합니다.

```text
git clone
  |
cp .env.example .env
  |
docker compose -f docker-compose.yaml up -d
  |
SPRING_PROFILES_ACTIVE=local, FDS_EVALUATION_MODE=async
  |
./gradlew bootRun
  |
API / Kafka / DB / Redis / Kafka UI 확인
```

---

# 1. 로컬 구성

로컬 개발 환경은 아래 구성을 기준으로 합니다.

```text
Spring Boot App
  |- MariaDB
  |- Kafka
  |- Kafka UI
  `- Redis
```

| 구성 | 기본 포트 | 용도 |
| --- | ---: | --- |
| Spring Boot | 8080 | API |
| MariaDB | 3306 | DB |
| Kafka | 9092 | Kafka broker |
| Kafka UI | 8085 | Kafka topic/message 확인 |
| Redis | 6379 | 캐시 |
| Actuator | 8080 | Health check |

Health check endpoint:

```text
http://localhost:8080/actuator/health
```

---

# 2. 파일 구조

현재 프로젝트는 `.yaml` 확장자를 기준으로 구성합니다.

```text
digital-asset-risk-platform/
  |- docker-compose.yaml
  |- .env.example
  |- README.md
  |- docs/
  |- src/
  |   |- main/
  |   |   `- resources/
  |   |       |- application.yaml
  |   |       |- application-local.yaml
  |   |       |- application-dev.yaml
  |   |       |- application-stage.yaml
  |   |       `- application-prod.yaml
  |   `- test/
  |       `- resources/
  |           `- application-test.yaml
  `- build.gradle
```

---

# 3. Profile 및 환경 변수 전략

중요한 기준은 다음과 같습니다.

- `.env`는 Docker Compose가 읽는 환경 변수 파일입니다.
- `application-local.yaml`은 `spring.config.import: optional:file:.env[.properties]`로 로컬 실행 시 `.env`를 읽습니다.
- Spring Boot 실행 profile은 `SPRING_PROFILES_ACTIVE`로 선택합니다.
- dev, stage, prod 환경은 `.env` 파일보다 배포 환경변수나 Secret Manager 주입을 기준으로 합니다.

| Profile | 용도 | 설정 파일 |
| --- | --- | --- |
| local | 개인 로컬 개발 | `application-local.yaml` |
| dev | 개발 서버 | `application-dev.yaml` |
| stage | 운영 전 검증 | `application-stage.yaml` |
| prod | 운영 | `application-prod.yaml` |
| test | 자동 테스트 | `src/test/resources/application-test.yaml` |

`application.yaml`은 profile 기본값을 `local`로 둡니다.

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

Bash에서 async 모드로 실행하는 예:

```bash
SPRING_PROFILES_ACTIVE=local FDS_EVALUATION_MODE=async ./gradlew bootRun
```

---

# 4. `.env.example`

`.env.example`은 Docker Compose 실행에 필요한 기본값을 문서화하는 파일입니다.

```env
# MariaDB
MYSQL_HOST=localhost
MYSQL_DATABASE=risk_platform
MYSQL_USER=risk_user
MYSQL_PASSWORD=risk_password
MYSQL_ROOT_PASSWORD=root_password
MYSQL_PORT=3306

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP_ID=risk-platform-local
KAFKA_PORT=9092
KAFKA_UI_PORT=8085

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# Spring
SPRING_PROFILES_ACTIVE=local
FDS_EVALUATION_MODE=async
```

로컬에서는 복사해서 사용합니다.

```bash
cp .env.example .env
```

주의할 점:

- `.env`는 git에 커밋하지 않습니다.
- local profile은 `application-local.yaml`의 config import를 통해 `.env` 값을 읽습니다.
- dev, stage, prod는 `.env` 파일에 의존하지 않고 실행 환경의 환경변수로 값을 주입합니다.

---

# 5. Docker Compose 구성

루트의 `docker-compose.yaml`은 로컬 인프라 실행을 담당합니다.

구성 요소:

- MariaDB
- Redis
- Kafka
- Kafka UI

실행:

```bash
docker compose -f docker-compose.yaml up -d
```

상태 확인:

```bash
docker compose -f docker-compose.yaml ps
```

Kafka listener 기준:

```text
로컬 PC의 Spring Boot -> localhost:9092
Docker 내부 Kafka UI -> kafka:29092
```

Kafka는 외부 접속용 listener와 Docker network 내부 접속용 listener를 함께 사용합니다.

---

# 6. Spring Boot 설정 구조

## 6.1 `application.yaml`

공통 설정은 `application.yaml`에 둡니다. 환경별 DB 계정, Kafka 주소, Redis 주소는 공통 파일에 직접 고정하지 않습니다.

핵심 설정:

```yaml
spring:
  application:
    name: digital-asset-risk-platform

  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

server:
  port: ${SERVER_PORT:8080}

fds:
  evaluation:
    mode: ${FDS_EVALUATION_MODE:sync}

outbox:
  publisher:
    fixed-delay-ms: ${OUTBOX_PUBLISHER_FIXED_DELAY_MS:3000}
```

## 6.2 `application-local.yaml`

`application-local.yaml`은 로컬 Docker Compose 인프라와 연결되는 설정입니다.

주요 기준:

- DB host: `localhost`
- Kafka bootstrap server: `localhost:9092`
- Redis host: `localhost`
- JPA ddl-auto: 로컬에서는 `update`
- Kafka listener: local profile에서는 자동 시작

Kafka 직렬화/역직렬화 구조는 두 흐름으로 나뉩니다.

| 흐름 | Producer | Consumer |
| --- | --- | --- |
| 일반 이벤트 직접 발행 | `KafkaTemplate<String, Object>` + `JsonSerializer` | Listener factory에서 타입별 `JsonDeserializer<T>` 주입 |
| Outbox 재발행 | `KafkaTemplate<String, String>` + `StringSerializer` | Listener factory에서 payload JSON을 타입별 DTO로 역직렬화 |

`application-local.yaml`의 `value-deserializer` 기본값은 `StringDeserializer`이지만, 실제 `@KafkaListener`는 `KafkaConsumerConfig`의 타입별 container factory가 `JsonDeserializer<T>`를 명시적으로 주입합니다.

## 6.3 `application-dev.yaml`, `application-stage.yaml`, `application-prod.yaml`

dev, stage, prod는 로컬 기본값보다 환경변수 주입을 우선합니다.

운영 계열 환경 원칙:

- `ddl-auto: update` 사용 금지
- `ddl-auto: validate` 또는 migration 도구 사용
- DB password, Kafka 주소, Redis 주소는 환경변수 또는 Secret Manager로 주입
- 운영에서는 DEBUG SQL logging 비활성화

## 6.4 `application-test.yaml`

테스트 설정은 로컬 Docker Compose와 분리합니다.

현재 테스트는 Testcontainers 기반이므로 `.\gradlew.bat test`를 실행하기 전에 `docker compose up`을 할 필요는 없습니다. 다만 Testcontainers가 컨테이너를 띄우기 때문에 Docker Desktop 실행 환경은 필요합니다.

테스트에서는 Kafka listener를 기본적으로 꺼두고, Kafka E2E 테스트에서만 override하는 방식이 안정적입니다.

```yaml
spring:
  kafka:
    listener:
      auto-startup: false

fds:
  evaluation:
    mode: sync

outbox:
  publisher:
    fixed-delay-ms: 600000
```

Kafka E2E 테스트에서는 필요한 경우 테스트 클래스에서 override합니다.

```java
@SpringBootTest(properties = {
    "spring.kafka.listener.auto-startup=true",
    "fds.evaluation.mode=async"
})
```

---

# 7. 로컬 실행 순서

## 7.1 환경 변수 파일 생성

```bash
cp .env.example .env
```

## 7.2 인프라 실행

```bash
docker compose -f docker-compose.yaml up -d
```

## 7.3 컨테이너 상태 확인

```bash
docker compose -f docker-compose.yaml ps
```

DB, Kafka, Redis가 준비되기 전에 Spring Boot를 바로 실행하면 연결 오류가 날 수 있습니다. 필요하면 로그를 확인합니다.

```bash
docker compose -f docker-compose.yaml logs -f mariadb
docker compose -f docker-compose.yaml logs -f kafka
docker compose -f docker-compose.yaml logs -f redis
```

## 7.4 Spring Boot 실행

sync 모드:

```bash
SPRING_PROFILES_ACTIVE=local FDS_EVALUATION_MODE=sync ./gradlew bootRun
```

async 모드:

```bash
SPRING_PROFILES_ACTIVE=local FDS_EVALUATION_MODE=async ./gradlew bootRun
```

## 7.5 Health Check

```bash
curl http://localhost:8080/actuator/health
```

## 7.6 Kafka UI

브라우저에서 접속합니다.

```text
http://localhost:8085
```

확인할 topic:

```text
withdrawal.requested
risk.evaluation.completed
risk.case.created
```

Topic은 Spring Boot 애플리케이션이 실행된 뒤 생성되거나, 이벤트 발행 후 확인될 수 있습니다.

---

# 8. 로컬 실행 확인용 API 순서

async 모드에서는 출금 요청 직후 `EVALUATING` 응답이 오고, Kafka Consumer가 이벤트를 처리한 뒤 상태가 변경됩니다. 따라서 출금 요청 후 바로 한 번만 조회하지 말고 잠시 뒤 재조회합니다.

계정 이벤트 기반 Rule은 시간 조건을 사용합니다. 아래 예시는 실행 시점 기준으로 최근 로그인/보안 이벤트 시간을 동적으로 생성합니다.

```bash
BASE_URL="http://localhost:8080"
LOGIN_AT="$(date -d '30 minutes ago' '+%Y-%m-%dT%H:%M:%S')"
SECURITY_AT="$(date -d '20 minutes ago' '+%Y-%m-%dT%H:%M:%S')"
```

## 8.1 고위험 지갑 등록

```bash
curl -X POST "$BASE_URL/api/wallet-risks" \
  -H "Content-Type: application/json" \
  -d '{
    "chainType": "TRON",
    "address": "THACKED000001",
    "riskLevel": "CRITICAL",
    "riskScore": 100,
    "riskCategory": "SANCTIONED_WALLET",
    "provider": "INTERNAL"
  }'
```

## 8.2 계정 로그인 이벤트 생성

```bash
curl -X POST "$BASE_URL/api/account-events/logins" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": 10001,
    \"deviceId\": \"device-new-001\",
    \"ipAddress\": \"203.0.113.10\",
    \"countryCode\": \"KR\",
    \"userAgent\": \"Mozilla/5.0\",
    \"loginAt\": \"$LOGIN_AT\"
  }"
```

## 8.3 계정 보안 이벤트 생성

```bash
curl -X POST "$BASE_URL/api/account-events/security" \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": 10001,
    \"eventType\": \"OTP_RESET\",
    \"deviceId\": \"device-new-001\",
    \"ipAddress\": \"203.0.113.10\",
    \"eventAt\": \"$SECURITY_AT\"
  }"
```

## 8.4 출금 요청

```bash
curl -X POST "$BASE_URL/api/withdrawals" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 10001,
    "assetSymbol": "USDT",
    "chainType": "TRON",
    "toAddress": "THACKED000001",
    "amount": "10000.000000000000000000"
  }'
```

async 모드 응답 예:

```json
{
  "withdrawalId": 1,
  "status": "EVALUATING",
  "riskLevel": null,
  "decision": null,
  "totalScore": null,
  "caseId": null
}
```

## 8.5 출금 상세 재조회

```bash
sleep 3
curl "$BASE_URL/api/withdrawals/1"
```

async 모드에서는 Consumer 처리 시간이 필요할 수 있으므로 상태가 아직 `EVALUATING`이면 잠시 후 다시 조회합니다.

## 8.6 관리자 Case 목록 조회

```bash
curl "$BASE_URL/api/admin/risk-cases"
```

## 8.7 관리자 대시보드 요약 조회

```bash
curl "$BASE_URL/api/admin/risk-dashboard/summary"
```

## 8.8 사용자 리스크 타임라인 조회

```bash
curl "$BASE_URL/api/admin/users/10001/risk-timeline"
```

---

# 9. Docker Compose 운영 명령어

## 전체 실행

```bash
docker compose -f docker-compose.yaml up -d
```

## 로그 확인

```bash
docker compose -f docker-compose.yaml logs -f kafka
docker compose -f docker-compose.yaml logs -f mariadb
docker compose -f docker-compose.yaml logs -f redis
```

## 중지

```bash
docker compose -f docker-compose.yaml down
```

## 볼륨까지 삭제

```bash
docker compose -f docker-compose.yaml down -v
```

주의:

```text
down -v는 MariaDB, Kafka, Redis의 로컬 데이터를 함께 삭제합니다.
로컬 데이터를 초기화해야 할 때만 사용합니다.
```

## 특정 서비스만 재시작

```bash
docker compose -f docker-compose.yaml restart kafka
```

---

# 10. Gradle 테스트 실행

## 전체 테스트

```bash
./gradlew test
```

## Kafka E2E 테스트만 실행

```bash
./gradlew test --tests "*KafkaFullWithdrawalFdsE2ETest"
```

## 특정 테스트 실행

```bash
./gradlew test --tests "com.example.digital_asset_risk_platform.e2e.KafkaOutboxAdminNotificationE2ETest"
```

테스트는 Testcontainers 기반으로 실행되므로 로컬 Docker Compose 인프라에 의존하지 않습니다.

---

# 11. Troubleshooting

## Spring Boot가 DB에 연결하지 못하는 경우

확인할 것:

- MariaDB 컨테이너가 정상 실행 중인지
- DB port가 충돌하지 않는지
- `application-local.yaml`의 datasource 설정과 compose 환경변수가 맞는지
- 컨테이너 초기화가 끝나기 전에 Spring Boot를 실행하지 않았는지

명령어:

```bash
docker compose -f docker-compose.yaml ps
docker compose -f docker-compose.yaml logs -f mariadb
```

## Kafka UI는 뜨지만 topic이 보이지 않는 경우

확인할 것:

- Spring Boot 애플리케이션이 실행되었는지
- 이벤트 발행 API를 호출했는지
- Kafka broker가 healthy 상태인지
- Kafka UI bootstrap server가 Docker network 내부 주소를 바라보는지

명령어:

```bash
docker compose -f docker-compose.yaml logs -f kafka
docker compose -f docker-compose.yaml logs -f kafka-ui
```

## async 모드에서 출금 상태가 계속 EVALUATING인 경우

확인할 것:

- `FDS_EVALUATION_MODE=async`로 실행했는지
- Kafka listener가 auto-startup 상태인지
- Outbox publisher가 동작 중인지
- Kafka Consumer 처리 로그가 있는지
- `withdrawal.requested` topic에 메시지가 발행되었는지

## `.env`를 수정했는데 Spring Boot 설정이 바뀌지 않는 경우

local profile은 `application-local.yaml`의 `spring.config.import` 설정으로 `.env`를 읽을 수 있습니다.

다만 `.env` 수정 후 이미 실행 중인 Spring Boot 애플리케이션에는 자동 반영되지 않습니다. 애플리케이션을 재시작해야 합니다.

```bash
SPRING_PROFILES_ACTIVE=local FDS_EVALUATION_MODE=async ./gradlew bootRun
```
