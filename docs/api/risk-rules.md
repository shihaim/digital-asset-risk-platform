# Risk Rules API

Rule 설정과 Rule 적중 통계 API입니다.

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
  "description": "고위험 지갑 주소 출금"
}
```

| Field | Type | Description |
| --- | --- | --- |
| `enabled` | boolean | Rule 활성화 여부 |
| `score` | number | Rule 적중 점수. 0 이상 |
| `blocking` | boolean | 즉시 차단 여부 |
| `thresholdValue` | string | Rule별 임계값. 최대 100자 |
| `description` | string | Rule 설명. 최대 1000자 |

---

## 4. Rule Statistics List

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

## 5. Top Rule Statistics

```http
GET /api/admin/risk-rule-statistics/top?limit=5
```

`limit`은 상위 Rule 개수를 의미합니다.

---

## 6. Rule Codes

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

## 7. Operational Notes

- `enabled=false`는 해당 Rule을 평가에서 제외합니다.
- `score`는 총점 기반 위험도 판단에 영향을 줍니다.
- `blocking=true` Rule은 적중 시 `CRITICAL` 판단을 유도할 수 있습니다.
- `thresholdValue`는 Rule별 해석이 다르므로 변경 전 테스트로 검증해야 합니다.
