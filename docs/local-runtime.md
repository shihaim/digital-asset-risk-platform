# Local Runtime

이 문서는 로컬에서 애플리케이션, MariaDB, Kafka, Redis, Kafka UI를 실행하고 동작을 확인하는 방법을 정리합니다.

---

## 1. 로컬 구성

| Component | Default Port | Purpose |
| --- | ---: | --- |
| Spring Boot | 8080 | API |
| MariaDB | 3306 | Database |
| Kafka | 9092 | Broker |
| Kafka UI | 8085 | Topic/message 확인 |
| Redis | 6379 | Cache |
| Actuator | 8080 | Health check |

Health check:

```text
http://localhost:8080/actuator/health
```

Kafka UI:

```text
http://localhost:8085
```

---

## 2. 환경 변수 파일

로컬 실행 전 `.env.example`을 복사합니다.

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

`.env`는 git에 커밋하지 않습니다.

---

## 3. Docker Compose 실행

```bash
docker compose -f docker-compose.yaml up -d
```

상태 확인:

```bash
docker compose -f docker-compose.yaml ps
```

로그 확인:

```bash
docker compose -f docker-compose.yaml logs -f kafka
docker compose -f docker-compose.yaml logs -f mariadb
docker compose -f docker-compose.yaml logs -f redis
```

중지:

```bash
docker compose -f docker-compose.yaml down
```

볼륨까지 삭제:

```bash
docker compose -f docker-compose.yaml down -v
```

`down -v`는 MariaDB, Kafka, Redis 로컬 데이터를 함께 삭제합니다. 초기화가 필요할 때만 사용합니다.

---

## 4. Spring Boot 실행

sync mode:

```bash
SPRING_PROFILES_ACTIVE=local FDS_EVALUATION_MODE=sync ./gradlew bootRun
```

async mode:

```bash
SPRING_PROFILES_ACTIVE=local FDS_EVALUATION_MODE=async ./gradlew bootRun
```

Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:FDS_EVALUATION_MODE="async"
.\gradlew bootRun
```

---

## 5. Kafka Topic 확인

Kafka UI에서 다음 topic을 확인합니다.

```text
withdrawal.requested
risk.evaluation.completed
risk.case.created
```

topic은 애플리케이션 실행 또는 이벤트 발행 후 확인할 수 있습니다.

---

## 6. 로컬 동작 확인 순서

기본 URL:

```bash
BASE_URL="http://localhost:8080"
```

고위험 지갑 등록:

```bash
curl -X POST "$BASE_URL/api/wallet-risks" \
  -H "Content-Type: application/json" \
  -d '{
    "chainType": "TRON",
    "address": "THACKED000001",
    "riskLevel": "CRITICAL",
    "riskScore": 100,
    "riskCategory": "SANCTIONED_ADDRESS",
    "provider": "INTERNAL"
  }'
```

출금 요청:

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

출금 상세 조회:

```bash
curl "$BASE_URL/api/withdrawals/1"
```

관리자 RiskCase 목록 조회:

```bash
curl "$BASE_URL/api/admin/risk-cases"
```

관리자 대시보드 요약:

```bash
curl "$BASE_URL/api/admin/risk-dashboard/summary"
```

---

## 7. 테스트 실행

전체 테스트:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew test
```

테스트는 Testcontainers 기반입니다. 로컬 Docker Compose 인프라에는 의존하지 않지만 Docker Desktop은 실행 중이어야 합니다.
