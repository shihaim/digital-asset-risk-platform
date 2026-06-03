# Operations

이 문서는 운영자가 장애, 위험 케이스, Outbox 발행 실패, 관리자 알림, Rule 통계와 설정을 확인할 때 필요한 관점을 정리합니다.

---

## 1. 운영 확인 대상

| Area | 확인 대상 |
| --- | --- |
| Outbox | `FAILED`, `DEAD` 이벤트 여부 |
| RiskCase | 심사 대기/심사 중 케이스 |
| Notification | 읽지 않은 관리자 알림 |
| Rule Statistics | 자주 적중하는 Rule |
| Rule Config | Rule 활성화 여부, 점수, blocking 여부, 임계값, 변경 이력 |
| Rule Simulation | Rule 변경 전후 예상 평가 결과 |
| Timeline | 특정 사용자의 로그인, 보안 이벤트, 출금 이력 |

---

## 2. Outbox 운영

Outbox 이벤트는 Kafka 발행 안정성을 위해 사용합니다. 운영자는 발행 실패 이벤트를 조회하고 필요하면 수동 재처리할 수 있습니다.

| API | Purpose |
| --- | --- |
| `GET /api/admin/outbox-events/summary` | 상태별 Outbox 이벤트 수 조회 |
| `GET /api/admin/outbox-events?status=FAILED&page=0&size=20` | 특정 상태 이벤트 목록 조회 |
| `GET /api/admin/outbox-events/{eventId}` | 이벤트 상세 조회 |
| `POST /api/admin/outbox-events/{eventId}/retry` | `FAILED` / `DEAD` 이벤트 수동 재처리 |

`DEAD` 이벤트는 자동 재시도 대상이 아닙니다. payload, topic, Kafka 상태, 직렬화 오류를 확인한 뒤 수동 재처리해야 합니다.

---

## 3. 관리자 알림

`risk.case.created` 이벤트가 발행되면 `AdminNotificationConsumer`가 관리자 알림을 생성합니다.

```text
risk.case.created
  -> AdminNotificationConsumer
  -> admin_notification 저장
  -> 관리자 알림 API에서 조회/읽음 처리
```

운영자는 읽지 않은 알림 수와 알림 목록을 통해 새로 생성된 위험 케이스를 확인할 수 있습니다.

| API | Purpose |
| --- | --- |
| `GET /api/admin/notifications` | 관리자 알림 목록 조회 |
| `GET /api/admin/notifications/unread-count` | 읽지 않은 알림 수 조회 |
| `POST /api/admin/notifications/{notificationId}/read` | 알림 읽음 처리 |

---

## 4. Rule 통계

`risk.evaluation.completed` 이벤트가 발행되면 `RiskRuleStatisticsConsumer`가 Rule 적중 통계를 집계합니다.

```text
risk.evaluation.completed
  -> RiskRuleStatisticsConsumer
  -> risk_rule_statistics 증가
  -> Rule 통계 API에서 조회
```

| API | Purpose |
| --- | --- |
| `GET /api/admin/risk-rule-statistics` | Rule 통계 목록 조회 |
| `GET /api/admin/risk-rule-statistics/top?limit=5` | 적중 횟수 기준 상위 Rule 조회 |

자주 적중하는 Rule은 실제 위험 증가일 수도 있고, 임계값이 지나치게 낮다는 신호일 수도 있습니다. 운영자는 통계와 RiskCase 심사 결과를 함께 봐야 합니다.

---

## 5. Rule 설정 운영

Rule 설정은 DB에서 관리합니다.

| API | Purpose |
| --- | --- |
| `GET /api/admin/risk-rules` | Rule 설정 목록 조회 |
| `GET /api/admin/risk-rules/{ruleCode}` | Rule 설정 상세 조회 |
| `PATCH /api/admin/risk-rules/{ruleCode}` | Rule 설정 수정 및 변경 이력 저장 |
| `GET /api/admin/risk-rules/{ruleCode}/histories` | Rule 설정 변경 이력 최신순 조회 |
| `POST /api/admin/risk-rules/simulate` | 운영 데이터 저장 없는 Rule 평가 시뮬레이션 |

운영 시 주의점:

- `enabled=false`는 해당 Rule을 평가에서 제외합니다.
- `score` 변경은 총점 기반 `RiskLevel` 판단에 영향을 줍니다.
- `blocking=true` Rule은 적중 시 즉시 `CRITICAL`로 판단될 수 있습니다.
- `thresholdValue`는 Rule별 임계값이므로 변경 전 테스트 시나리오로 검증해야 합니다.
- Rule 설정 변경 요청에는 `changedBy`, `changeReason`을 함께 남겨야 합니다.
- 변경 이력은 변경 전/후 값과 변경자, 변경 사유, 변경 시각을 포함합니다.
- 시뮬레이션 API는 `WithdrawalRequest`, `RiskEvaluation`, `RiskRuleHit`, `RiskCase`를 저장하지 않고 평가 결과만 반환합니다.

---

## 6. Redis/KYT 운영 관점

지갑 위험도 조회는 Redis cache, DB, KYT Provider Mock 순서로 진행됩니다.

```text
Redis hit
  -> 캐시된 위험도 사용

Redis miss
  -> DB 조회
  -> DB miss 시 KYT Provider Mock 조회
  -> 위험 주소면 DB 저장 후 캐시
```

실제 운영 환경에서는 Mock Provider 대신 외부 KYT Provider를 연결할 수 있습니다. 이때 타임아웃, Provider 오류, 응답 신뢰도, 캐시 TTL 정책을 별도로 관리해야 합니다.

---

## 7. 장애 확인 순서

async mode에서 출금 상태가 계속 `EVALUATING`이면 다음 순서로 확인합니다.

1. `outbox_event`에 `withdrawal.requested` 이벤트가 저장되었는지 확인
2. Outbox 상태가 `PENDING`, `FAILED`, `DEAD` 중 어디에 머무는지 확인
3. Kafka topic에 메시지가 발행되었는지 확인
4. `FdsWithdrawalConsumer`가 실행 중인지 확인
5. Consumer 멱등성 테이블에 동일 eventId가 이미 처리되어 있는지 확인
6. FDS 평가 중 예외가 발생했는지 애플리케이션 로그 확인

---

## 8. 운영 API 문서

상세 요청/응답 예시는 [운영 API 문서](api/admin-operations.md)를 참고합니다.
