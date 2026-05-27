# Risk Cases API

관리자 RiskCase 조회 및 심사 처리 API입니다.

---

## 1. Search Risk Cases

```http
GET /api/admin/risk-cases?status=REVIEW_REQUIRED
```

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

---

## 2. Get Risk Case Detail

```http
GET /api/admin/risk-cases/{caseId}
```

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
      "reason": "출금 주소가 고위험 지갑 주소로 분류됨",
      "blocking": true
    }
  ],
  "timeline": []
}
```

---

## 3. Review Request Body

아래 심사 API는 동일한 요청 본문을 사용합니다.

```json
{
  "reviewer": "admin01",
  "comment": "고위험 지갑 주소 및 계정 보안 이벤트 확인"
}
```

---

## 4. Review APIs

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/admin/risk-cases/{caseId}/start-review` | 심사 시작 |
| `POST` | `/api/admin/risk-cases/{caseId}/approve` | 승인 |
| `POST` | `/api/admin/risk-cases/{caseId}/reject` | 거절 |
| `POST` | `/api/admin/risk-cases/{caseId}/mark-false-positive` | 오탐 처리 |
| `POST` | `/api/admin/risk-cases/{caseId}/mark-true-positive` | 정탐 처리 |

### Response

각 API는 변경된 RiskCase 상세 응답을 반환합니다.

---

## 5. RiskCase Status

| Status | Description |
| --- | --- |
| `REVIEW_REQUIRED` | 심사 필요 |
| `IN_REVIEW` | 심사 중 |
| `APPROVED` | 관리자 승인 |
| `REJECTED` | 관리자 거절 |
| `FALSE_POSITIVE` | 오탐 |
| `TRUE_POSITIVE` | 정탐 |
