# Test Strategy

이 프로젝트의 테스트는 단순 CRUD보다 FDS 비즈니스 정합성, 이벤트 발행 안정성, Consumer 멱등성, 외부 의존성 대체 구조 검증에 초점을 둡니다.

---

## 1. 테스트 목표

- Rule 조건 적중 여부 검증
- `DecisionEngine` 판단 결과 검증
- 출금 상태 전이 검증
- `RiskEvaluation` / `RiskRuleHit` 저장 검증
- 위험 출금 `RiskCase` 생성 검증
- 관리자 심사 결과 반영 검증
- Kafka 이벤트 발행 검증
- Outbox 상태 전이 및 수동 재처리 검증
- Consumer 멱등성 검증
- sync / async 평가 모드 차이 검증
- Redis cache 및 KYT Provider Mock fallback 검증
- Rule 설정 변경 이력 저장 및 최신순 조회 검증
- Rule 시뮬레이션 결과 반환 및 운영 데이터 미저장 검증
- 사용자 누적 위험 프로필 갱신 및 멱등성 검증

---

## 2. 테스트 계층

| Layer | Scope |
| --- | --- |
| Unit Test | Rule, DecisionEngine, Domain Entity, Consumer 단위 로직 |
| Integration Test | WithdrawalService, RiskCaseService, OutboxEventPublisher, WalletRiskService |
| Admin Service Test | Rule 설정 변경 이력, Rule 시뮬레이션 Service 동작 |
| Controller Test | 관리자 API 요청/응답 검증 |
| Kafka E2E Test | Testcontainers Kafka 기반 Producer/Consumer 흐름 |
| Redis Integration Test | Testcontainers Redis 기반 캐시 동작 |
| Business E2E Test | 출금 요청부터 관리자 심사까지 전체 흐름 |

---

## 3. 주요 테스트 시나리오

### Rule

- 신규 기기 로그인 직후 출금 Rule
- OTP 재설정 직후 출금 Rule
- 비밀번호 변경 직후 출금 Rule
- 신규 지갑 주소 Rule
- 고액 출금 Rule
- 24시간 내 반복 출금 Rule
- 고위험 지갑 주소 Rule
- DB Rule 설정 반영 여부
- Rule 설정 수정 시 변경 전/후 이력 저장
- Rule 변경 이력 최신 변경 시각순 조회

### Rule Simulation

- 시뮬레이션 요청 성공 시 `totalScore`, `riskLevel`, `decision`, `ruleHits` 반환
- 기존 Rule 목록과 `DecisionEngine` 재사용
- 고액 출금 조건에서 `HIGH_AMOUNT_WITHDRAWAL` RuleHit 발생
- 고위험 지갑 주소에서 `HIGH_RISK_WALLET` RuleHit 발생
- 시뮬레이션 후 `WithdrawalRequest` 미저장
- 운영 데이터인 `RiskEvaluation`, `RiskRuleHit`, `RiskCase`를 저장하지 않는 흐름 검증

### DecisionEngine

- 점수 구간별 `RiskLevel` / `RiskDecisionType` 검증
- blocking Rule 우선순위 검증
- 출금 상태 매핑 검증

### Withdrawal FDS

- 정상 출금 `APPROVED`
- 주의 출금 `HELD`
- 고위험 지갑 출금 `BLOCKED`
- `RiskEvaluation` / `RiskRuleHit` 저장
- 위험 출금 `RiskCase` 생성
- sync mode 즉시 평가
- async mode `EVALUATING` 응답 후 Consumer 평가

### Kafka / Outbox

- OutboxEvent 저장
- Kafka 발행 성공 시 `SENT`
- Kafka 발행 실패 시 `FAILED`
- 최대 재시도 초과 시 `DEAD`
- `FAILED` / `DEAD` 이벤트 수동 재처리
- `withdrawal.requested` 기반 FDS Consumer 처리
- `risk.case.created` 기반 관리자 알림 생성
- `risk.evaluation.completed` 기반 Rule 통계 증가
- `risk.evaluation.completed` 기반 사용자 위험 점수 및 차단 횟수 증가
- `risk.case.created` 기반 사용자 Case count 증가
- 중복 eventId 처리 방지

### User Risk Profile

- 평가 점수를 프로필 점수로 변환하는 경계값 검증
- 누적 점수에 따른 `NORMAL`, `WATCH`, `HIGH`, `CRITICAL` 등급 변경 검증
- `BLOCK_WITHDRAWAL` 평가 이벤트 처리 시 차단 출금 횟수 증가
- `RiskCaseCreatedEvent` 처리 시 실제 Case count 증가
- 동일 `eventId` 재수신 시 점수와 카운트 중복 반영 방지
- 프로필 없는 사용자 조회 시 DB row 생성 없이 기본 응답 반환
- 관리자 위험 프로필 조회 API 응답 필드 검증

### Redis / KYT

- Redis cache hit/miss 동작
- DB miss 시 KYT Provider Mock fallback
- KYT 위험 주소의 `wallet_address_risk` 저장
- KYT 결과가 `HIGH_RISK_WALLET` Rule 평가로 연결되는지 검증

---

## 4. E2E 흐름

```text
출금 요청
  -> Outbox에 withdrawal.requested 저장
  -> Kafka 발행
  -> FdsWithdrawalConsumer 소비
  -> FDS 평가 / RiskCase 생성
  -> Outbox에 risk.evaluation.completed, risk.case.created 저장
  -> Kafka 발행
  -> Audit / Notification / RuleStatistics / UserRiskProfile Consumer 처리
```

---

## 5. 실행 명령어

전체 테스트:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew test
```

Kafka 전체 FDS E2E:

```bash
./gradlew test --tests "*KafkaFullWithdrawalFdsE2ETest"
```

Outbox + 관리자 알림 E2E:

```bash
./gradlew test --tests "*KafkaOutboxAdminNotificationE2ETest"
```

Redis/KYT 캐시 통합 테스트:

```bash
./gradlew test --tests "*FdsWalletRiskCacheIntegrationTest"
```

Rule 변경 이력 Service 테스트:

```bash
./gradlew test --tests "*RiskRuleConfigAdminServiceTest"
```

Rule 시뮬레이션 Controller/Service 테스트:

```bash
./gradlew test --tests "*AdminRiskRuleSimulationControllerTest" --tests "*RiskRuleSimulationServiceTest"
```

테스트는 Testcontainers 기반으로 Kafka, Redis, DB를 띄우므로 Docker Desktop 실행 환경이 필요합니다.
