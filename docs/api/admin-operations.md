# Admin Operations API

운영자가 Outbox, 관리자 알림, 대시보드 요약, 사용자 리스크 타임라인, 사용자 누적 위험 프로필을 확인하는 API입니다.

---

## 1. Outbox Summary

```http
GET /api/admin/outbox-events/summary
```

### Response

```json
{
  "pendingCount": 2,
  "processingCount": 0,
  "sentCount": 120,
  "failedCount": 1,
  "deadCount": 3
}
```

---

## 2. Outbox Event List

```http
GET /api/admin/outbox-events?status=DEAD&page=0&size=20
```

`status`는 필수 파라미터입니다.

### Response

```json
{
  "content": [
    {
      "id": 10,
      "eventId": "event-001",
      "eventType": "RiskCaseCreatedEvent",
      "topicName": "risk.case.created",
      "messageKey": "100",
      "status": "DEAD",
      "retryCount": 5,
      "lastErrorMessage": "Kafka publish failed",
      "occurredAt": "2026-05-12T10:00:00",
      "createdAt": "2026-05-12T10:00:01",
      "updatedAt": "2026-05-12T10:05:00",
      "sentAt": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 3. Outbox Event Detail

```http
GET /api/admin/outbox-events/{eventId}
```

상세 응답은 `payloadJson`을 포함합니다.

```json
{
  "id": 10,
  "eventId": "event-001",
  "eventType": "RiskCaseCreatedEvent",
  "topicName": "risk.case.created",
  "messageKey": "100",
  "payloadJson": "{...}",
  "status": "DEAD",
  "retryCount": 5,
  "lastErrorMessage": "Kafka publish failed",
  "occurredAt": "2026-05-12T10:00:00",
  "createdAt": "2026-05-12T10:00:01",
  "updatedAt": "2026-05-12T10:05:00",
  "sentAt": null
}
```

---

## 4. Retry Outbox Event

```http
POST /api/admin/outbox-events/{eventId}/retry
```

`FAILED` 또는 `DEAD` 상태의 이벤트를 `PENDING`으로 되돌립니다. 즉시 Kafka로 발행하지 않고, 이후 Outbox Publisher가 다시 발행합니다.

---

## 5. Admin Notifications

### List

```http
GET /api/admin/notifications?readYn=N&page=0&size=20
```

### Unread Count

```http
GET /api/admin/notifications/unread-count
```

```json
{
  "unreadCount": 3
}
```

### Mark As Read

```http
POST /api/admin/notifications/{notificationId}/read
```

```json
{
  "notificationId": 1,
  "eventId": "event-case-001",
  "caseId": 10,
  "userId": 10001,
  "notificationType": "RISK_CASE_CREATED",
  "title": "위험 Case가 생성되었습니다.",
  "message": "Case ID 10 / 사용자 10001 / 위험도 CRITICAL / 유형 AML_REVIEW",
  "readYn": "Y",
  "createdAt": "2026-05-12T10:00:00",
  "readAt": "2026-05-12T10:05:00"
}
```

---

## 6. Risk Dashboard Summary

```http
GET /api/admin/risk-dashboard/summary
```

### Response

```json
{
  "reviewRequiredCaseCount": 3,
  "inReviewCaseCount": 1,
  "approvedCaseCount": 5,
  "rejectedCaseCount": 2,
  "falsePositiveCaseCount": 4,
  "truePositiveCaseCount": 1,
  "heldWithdrawalCount": 2,
  "blockedWithdrawalCount": 1
}
```

---

## 7. User Risk Timeline

```http
GET /api/admin/users/{userId}/risk-timeline
```

### Response

```json
{
  "userId": 10001,
  "events": [
    {
      "eventType": "LOGIN",
      "description": "신규 기기 로그인 - deviceId=device-new-001",
      "eventAt": "2026-05-09T09:30:00"
    },
    {
      "eventType": "OTP_RESET",
      "description": "보안 이벤트 발생: OTP_RESET",
      "eventAt": "2026-05-09T09:40:00"
    },
    {
      "eventType": "WITHDRAWAL_REQUESTED",
      "description": "USDT 10000.000000000000000000 출금 요청 - status=BLOCKED",
      "eventAt": "2026-05-09T10:00:00"
    }
  ]
}
```

---

## 8. User Risk Profile

```http
GET /api/admin/users/{userId}/risk-profile
```

사용자의 누적 위험 점수, 위험 등급, 실제 생성된 Case 수, 차단 출금 횟수를 조회합니다.

### Response

```json
{
  "userId": 10001,
  "currentRiskScore": 80,
  "riskLevel": "HIGH",
  "totalCaseCount": 3,
  "totalBlockedWithdrawalCount": 1,
  "lastEvaluatedAt": "2026-06-04T19:30:00"
}
```

평가 이력이 없는 사용자는 `NORMAL`, score `0`, count `0`으로 응답합니다. 조회만으로 `user_risk_profile` row를 생성하지 않습니다.

---

## 9. Outbox Status

| Status | Description |
| --- | --- |
| `PENDING` | 발행 대기 |
| `PROCESSING` | 발행 중 |
| `SENT` | 발행 성공 |
| `FAILED` | 발행 실패, 자동 재시도 대상 |
| `DEAD` | 최대 재시도 초과, 수동 확인 대상 |
