# API 명세

## 1. 출금 요청

### `POST /api/withdrawals`

출금 요청을 생성합니다.

sync 모드에서는 FDS 평가 결과를 즉시 반환합니다. async 모드에서는 `EVALUATING` 상태를 반환하고, FDS 평가는 Kafka Consumer에서 처리됩니다.

### Request

```json
{
  "userId": 10001,
  "assetSymbol": "USDT",
  "chainType": "TRON",
  "toAddress": "THACKED000001",
  "amount": "10000.000000000000000000"
}
```

### Response - async mode

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

### Response - sync mode

```json
{
  "withdrawalId": 1,
  "status": "BLOCKED",
  "riskLevel": "CRITICAL",
  "decision": "BLOCK_WITHDRAWAL",
  "totalScore": 190,
  "caseId": 1
}
```

## 2. 출금 상세 조회

### `GET /api/withdrawals/{withdrawalId}`

### Response

```json
{
  "withdrawalId": 1,
  "userId": 10001,
  "assetSymbol": "USDT",
  "chainType": "TRON",
  "toAddress": "THACKED000001",
  "amount": "10000.000000000000000000",
  "status": "BLOCKED",
  "riskLevel": "CRITICAL",
  "decision": "BLOCK_WITHDRAWAL",
  "totalScore": 190,
  "requestedAt": "2026-05-09T10:00:00"
}
```

## 3. 관리자 RiskCase 목록 조회

### `GET /api/admin/risk-cases?status=REVIEW_REQUIRED`

`status`는 선택 파라미터입니다.

### Response

```json
[
  {
    "caseId": 1,
    "evaluationId": 1,
    "userId": 10001,
    "caseType": "AML_REVIEW",
    "status": "REVIEW_REQUIRED",
    "riskLevel": "CRITICAL",
    "assignedTo": null,
    "createdAt": "2026-05-09T10:01:00",
    "closedAt": null
  }
]
```

## 4. 관리자 RiskCase 상세 조회

### `GET /api/admin/risk-cases/{caseId}`

### Response

```json
{
  "caseId": 1,
  "caseType": "AML_REVIEW",
  "status": "REVIEW_REQUIRED",
  "riskLevel": "CRITICAL",
  "assignedTo": null,
  "reviewResult": null,
  "reviewComment": null,
  "createdAt": "2026-05-09T10:01:00",
  "closedAt": null,
  "withdrawal": {
    "withdrawalId": 1,
    "userId": 10001,
    "assetSymbol": "USDT",
    "chainType": "TRON",
    "toAddress": "THACKED000001",
    "amount": "10000.000000000000000000",
    "status": "BLOCKED",
    "requestedAt": "2026-05-09T10:00:00"
  },
  "evaluation": {
    "evaluationId": 1,
    "refType": "WITHDRAWAL",
    "refId": 1,
    "userId": 10001,
    "totalScore": 190,
    "riskLevel": "CRITICAL",
    "decision": "BLOCK_WITHDRAWAL",
    "evaluatedAt": "2026-05-09T10:00:05"
  },
  "ruleHits": [
    {
      "ruleCode": "HIGH_RISK_WALLET",
      "ruleName": "고위험 지갑 주소 출금",
      "score": 100,
      "reason": "출금 주소가 고위험 지갑 주소로 분류됨: CRITICAL",
      "blocking": true
    }
  ],
  "timeline": []
}
```

## 5. 관리자 심사 처리

### Request

아래 심사 API는 동일한 요청 본문을 사용합니다.

```json
{
  "reviewer": "admin01",
  "comment": "고위험 지갑 및 계정 탈취 정황 확인"
}
```

### API 목록

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/admin/risk-cases/{caseId}/start-review` | 심사 시작 |
| `POST` | `/api/admin/risk-cases/{caseId}/approve` | 승인 |
| `POST` | `/api/admin/risk-cases/{caseId}/reject` | 거절 |
| `POST` | `/api/admin/risk-cases/{caseId}/mark-false-positive` | 오탐 처리 |
| `POST` | `/api/admin/risk-cases/{caseId}/mark-true-positive` | 정탐 처리 |

## 6. 관리자 알림 API

관리자 알림은 `risk.case.created` 이벤트를 `AdminNotificationConsumer`가 소비하면서 생성됩니다.

```text
risk.case.created 이벤트 발생
  -> AdminNotificationConsumer 소비
  -> admin_notification 저장
  -> 관리자 알림 API에서 조회/읽음 처리
```

### 6.1 관리자 알림 목록 조회

### `GET /api/admin/notifications`

관리자 알림 목록을 최신순으로 조회합니다. `readYn`을 전달하면 읽음 여부로 필터링할 수 있습니다.

### Query Parameters

| Name | Required | Description |
| --- | --- | --- |
| `readYn` | false | 읽음 여부. `N` 또는 `Y` |
| `page` | false | page number |
| `size` | false | page size |

### Request Example

```http
GET /api/admin/notifications?readYn=N&page=0&size=20
```

### Response

```json
{
  "content": [
    {
      "notificationId": 1,
      "eventId": "event-case-001",
      "caseId": 10,
      "userId": 10001,
      "notificationType": "RISK_CASE_CREATED",
      "title": "위험 Case가 생성되었습니다.",
      "message": "Case ID 10 / 사용자 10001 / 위험도 CRITICAL / 유형 AML_REVIEW",
      "readYn": "N",
      "createdAt": "2026-05-12T10:00:00",
      "readAt": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 6.2 읽지 않은 알림 개수 조회

### `GET /api/admin/notifications/unread-count`

`readYn = N`인 관리자 알림 개수를 조회합니다.

### Response

```json
{
  "unreadCount": 3
}
```

### 6.3 관리자 알림 읽음 처리

### `POST /api/admin/notifications/{notificationId}/read`

관리자 알림을 읽음 처리합니다. 이미 읽은 알림에 다시 요청해도 읽음 상태를 유지합니다.

### Response

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

### Error

존재하지 않는 `notificationId`이면 `ADMIN_NOTIFICATION_NOT_FOUND` 예외가 발생합니다.

## 7. Supporting APIs

아래 API는 FDS 평가와 관리자 심사를 보조하는 운영/테스트용 API입니다. 핵심 흐름은 출금 요청과 RiskCase API가 담당하고, Supporting APIs는 위험 신호 데이터 생성, 위험 지갑 등록, 대시보드 요약, 사용자 타임라인 조회를 담당합니다.

### 7.1 계정 로그인 이벤트 생성

### `POST /api/account-events/logins`

신규 기기 로그인 여부 판단에 사용할 로그인 이벤트를 생성합니다.

### Request

```json
{
  "userId": 10001,
  "deviceId": "device-new-001",
  "ipAddress": "203.0.113.10",
  "countryCode": "KR",
  "userAgent": "Mozilla/5.0",
  "loginAt": "2026-05-09T09:30:00"
}
```

### Response

```json
1
```

### 7.2 계정 보안 이벤트 생성

### `POST /api/account-events/security`

OTP 재설정, 비밀번호 변경 등 출금 위험 평가에 사용할 보안 이벤트를 생성합니다.

사용 가능한 `eventType` 값은 `PASSWORD_CHANGED`, `OTP_RESET`, `PHONE_CHANGED`, `EMAIL_CHANGED`, `WITHDRAWAL_ADDRESS_ADDED`입니다.

### Request

```json
{
  "userId": 10001,
  "eventType": "OTP_RESET",
  "deviceId": "device-new-001",
  "ipAddress": "203.0.113.10",
  "eventAt": "2026-05-09T09:40:00"
}
```

### Response

```json
1
```

### 7.3 위험 지갑 등록

### `POST /api/wallet-risks`

고위험 지갑 주소 정보를 등록합니다. 등록된 주소는 `HIGH_RISK_WALLET` Rule 평가에 사용됩니다.

### Request

```json
{
  "chainType": "TRON",
  "address": "THACKED000001",
  "riskLevel": "CRITICAL",
  "riskScore": 100,
  "riskCategory": "SANCTIONED_WALLET",
  "provider": "INTERNAL"
}
```

### Response

```json
1
```

### 7.4 관리자 리스크 대시보드 요약

### `GET /api/admin/risk-dashboard/summary`

관리자 화면에서 사용할 Case 및 출금 상태 요약 수치를 조회합니다.

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

### 7.5 사용자 리스크 타임라인 조회

### `GET /api/admin/users/{userId}/risk-timeline`

사용자의 로그인 이벤트, 보안 이벤트, 출금 요청을 시간순 타임라인으로 조회합니다.

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
