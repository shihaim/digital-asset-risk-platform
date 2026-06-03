# Risk Rules API

Rule 설정, Rule 변경 이력, Rule 시뮬레이션, Rule 적중 통계 API입니다.

---

## 1. Rule Config List

```http
GET /api/admin/risk-rules
```

### Response

```json
[
  {
    "ruleCode": "HIGH_RISK_WALLET",
    "enabled": true,
    "score": 100,
    "blocking": true,
    "thresholdValue": null,
    "description": "고위험 지갑 주소 출금"
  }
]
```

---

## 2. Rule Config Detail

```http
GET /api/admin/risk-rules/{ruleCode}
```

---

## 3. Update Rule Config

```http
PATCH /api/admin/risk-rules/{ruleCode}
```

모든 필드는 선택입니다. 전달한 필드만 변경됩니다.

### Request

```json
{
  "enabled": true,
  "score": 100,
  "blocking": true,
  "thresholdValue": null,
  "description": "고위험 지갑 주소 출금",
  "changedBy": "admin",
  "changeReason": "고위험 지갑 Rule 정책 조정"
}
```

| Field | Type | Description |
| --- | --- | --- |
| `enabled` | boolean | Rule 활성화 여부 |
| `score` | number | Rule 적중 점수. 0 이상 |
| `blocking` | boolean | 즉시 차단 여부 |
| `thresholdValue` | string | Rule별 임계값. 최대 100자 |
| `description` | string | Rule 설명. 최대 1000자 |
| `changedBy` | string | 변경자. 필수, 최대 100자 |
| `changeReason` | string | 변경 사유. 필수, 최대 500자 |

Rule 설정 변경과 변경 이력 저장은 하나의 트랜잭션으로 처리됩니다. 대상 Rule이 없으면 설정도 변경되지 않고 이력도 저장되지 않습니다.

---

## 4. Rule Config Histories

```http
GET /api/admin/risk-rules/{ruleCode}/histories
```

특정 Rule 설정의 변경 이력을 최신 변경 시각순으로 조회합니다.

### Response

```json
[
  {
    "id": 1,
    "ruleCode": "HIGH_RISK_WALLET",
    "ruleName": "고위험 지갑 주소 출금",
    "beforeEnabled": true,
    "afterEnabled": true,
    "beforeScore": 100,
    "afterScore": 80,
    "beforeBlocking": true,
    "afterBlocking": true,
    "beforeThresholdValue": null,
    "afterThresholdValue": null,
    "beforeDescription": "고위험 지갑 주소로 출금하는 경우 차단",
    "afterDescription": "오탐 감소를 위해 점수를 조정",
    "changedBy": "admin",
    "changeReason": "고위험 지갑 Rule 점수 조정",
    "changedAt": "2026-05-12T10:00:00"
  }
]
```

---

## 5. Simulate Risk Rules

```http
POST /api/admin/risk-rules/simulate
```

입력한 출금 조건으로 현재 Rule 목록과 `DecisionEngine`을 사용해 평가 결과를 계산합니다.

### Request

```json
{
  "userId": 10001,
  "assetSymbol": "USDT",
  "chainType": "TRON",
  "toAddress": "TNORMAL000001",
  "amount": "100.000000000000000000"
}
```

| Field | Type | Description |
| --- | --- | --- |
| `userId` | number | 사용자 ID. 필수 |
| `assetSymbol` | string | 자산 심볼. 필수 |
| `chainType` | string | 체인 종류. 필수 |
| `toAddress` | string | 출금 대상 주소. 필수 |
| `amount` | decimal | 출금 수량. 필수, 0보다 큼 |

### Response

```json
{
  "totalScore": 20,
  "riskLevel": "NORMAL",
  "decision": "ALLOW",
  "ruleHits": [
    {
      "ruleCode": "NEW_WALLET_ADDRESS",
      "ruleName": "신규 지갑 주소 출금",
      "score": 20,
      "reason": "신규 지갑 주소로 출금 요청",
      "blocking": false
    }
  ]
}
```

### Persistence Notes

시뮬레이션은 실제 출금 처리 API가 아닙니다. 평가를 위해 메모리상의 `WithdrawalRequest` 객체와 `RiskContext`를 구성하지만, 다음 운영 데이터는 저장하지 않습니다.

- `WithdrawalRequest`
- `RiskEvaluation`
- `RiskRuleHit`
- `RiskCase`

다만 `RiskContextBuilder`가 조회하는 기존 로그인/보안 이벤트/지갑 위험도/과거 출금 데이터는 Rule 평가 근거로 사용될 수 있습니다.

---

## 6. Rule Statistics List

```http
GET /api/admin/risk-rule-statistics?page=0&size=20
```

### Response

```json
{
  "content": [
    {
      "ruleCode": "HIGH_RISK_WALLET",
      "hitCount": 12,
      "lastHitAt": "2026-05-12T10:00:00",
      "createdAt": "2026-05-10T09:00:00",
      "updatedAt": "2026-05-12T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 7. Top Rule Statistics

```http
GET /api/admin/risk-rule-statistics/top?limit=5
```

`limit`은 상위 Rule 개수를 의미합니다.

---

## 8. Rule Codes

| Rule Code | Description | Default Score | Blocking |
| --- | --- | ---: | --- |
| `NEW_DEVICE_WITHDRAWAL` | 신규 기기 로그인 직후 출금 | 30 | false |
| `OTP_RESET_WITHDRAWAL` | OTP 재설정 직후 출금 | 40 | false |
| `PASSWORD_CHANGED_WITHDRAWAL` | 비밀번호 변경 직후 출금 | 30 | false |
| `NEW_WALLET_ADDRESS` | 신규 지갑 주소 출금 | 20 | false |
| `HIGH_AMOUNT_WITHDRAWAL` | 고액 출금 | 40 | false |
| `FREQUENT_WITHDRAWAL_24H` | 24시간 내 반복 출금 | 30 | false |
| `HIGH_RISK_WALLET` | 고위험 지갑 주소 출금 | 100 | true |

---

## 9. Operational Notes

- `enabled=false`는 해당 Rule을 평가에서 제외합니다.
- `score`는 총점 기반 위험도 판단에 영향을 줍니다.
- `blocking=true` Rule은 적중 시 `CRITICAL` 판단을 유도할 수 있습니다.
- `thresholdValue`는 Rule별 해석이 다르므로 변경 전 테스트로 검증해야 합니다.
- Rule 설정을 변경할 때는 `changedBy`, `changeReason`을 함께 전달해야 합니다.
- 운영 Rule 조정 전에는 시뮬레이션 API로 예상 RuleHit과 Decision을 먼저 확인할 수 있습니다.
