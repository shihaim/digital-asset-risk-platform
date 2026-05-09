# 멱등성 설계

Kafka Consumer는 같은 메시지를 두 번 이상 처리할 수 있습니다.
따라서 본 프로젝트는 `eventId`와 unique key를 기반으로 중복 처리를 방지합니다.

## 1. Producer 멱등성

### outbox_event

| 제약 | 설명 |
| --- | --- |
| `event_id unique` | 같은 이벤트가 중복 저장되는 것을 방지 |

```sql
UNIQUE KEY uk_outbox_event_id (event_id)
```

## 2. Consumer 멱등성

### consumer_processed_event

Consumer별 처리 이력을 저장합니다.

```sql
UNIQUE KEY uk_consumer_event (consumer_name, event_id)
```

이를 통해 같은 Consumer가 같은 `eventId`를 중복 처리하지 않도록 합니다.

## 3. 도메인 중복 방지

### RiskEvaluation

출금 1건에 대해 FDS 평가는 1번만 생성되어야 합니다.

```sql
UNIQUE KEY uk_risk_eval_ref (ref_type, ref_id)
```

예:

| refType | refId |
| --- | --- |
| `WITHDRAWAL` | `withdrawalId` |

## 4. Consumer별 멱등성 정책

| Consumer | 멱등성 기준 | 처리 |
| --- | --- | --- |
| `FdsWithdrawalConsumer` | `consumerName + eventId`, `refType + refId` | 중복 평가 방지 |
| `AuditEventConsumer` | `eventId` | 중복 감사 로그 방지 |
| `AdminNotificationConsumer` | `eventId` | 중복 알림 방지 |
| `RiskRuleStatisticsConsumer` | `consumerName + eventId` | 중복 통계 증가 방지 |

## 5. ack 정책

- 처리 성공 시 `acknowledgment.acknowledge()`를 호출한다.
- 이미 처리한 이벤트도 중복 처리를 건너뛴 뒤 ack한다.
- 처리 실패 시 ack하지 않으면 Kafka가 해당 메시지를 재처리할 수 있다.
- 재처리 시 멱등성 키로 중복 처리를 방지한다.

## 6. 중복 이벤트 처리 예시

```text
1. risk.evaluation.completed 이벤트 수신
2. consumer_processed_event에서 consumerName + eventId 조회
3. 이미 처리됨 -> skip + ack
4. 미처리 -> Rule 통계 증가
5. consumer_processed_event 저장
6. ack
```

