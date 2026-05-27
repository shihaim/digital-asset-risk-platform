# Support APIs

FDS 시나리오를 재현하기 위한 보조 API입니다. 핵심 출금 API는 아니지만 Rule 평가에 필요한 입력 데이터를 만들 때 사용합니다.

---

## 1. Create Login Event

```http
POST /api/account-events/logins
```

신규 기기 로그인 Rule 평가에 사용할 로그인 이벤트를 생성합니다.

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

---

## 2. Create Security Event

```http
POST /api/account-events/security
```

OTP 재설정, 비밀번호 변경 등 보안 이벤트를 생성합니다.

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

사용 가능한 `eventType`:

```text
PASSWORD_CHANGED
OTP_RESET
PHONE_CHANGED
EMAIL_CHANGED
WITHDRAWAL_ADDRESS_ADDED
```

### Response

```json
1
```

---

## 3. Create Wallet Risk

```http
POST /api/wallet-risks
```

고위험 지갑 주소 정보를 등록합니다. 등록된 주소는 `HIGH_RISK_WALLET` Rule 평가에 사용됩니다.

### Request

```json
{
  "chainType": "TRON",
  "address": "THACKED000001",
  "riskLevel": "CRITICAL",
  "riskScore": 100,
  "riskCategory": "SANCTIONED_ADDRESS",
  "provider": "INTERNAL"
}
```

### Response

```json
1
```

---

## 4. KYT Provider Mock

DB에 등록되지 않은 지갑 주소는 FDS 평가 중 KYT Provider Mock fallback으로 조회될 수 있습니다.

| Address Pattern | RiskLevel | RiskCategory |
| --- | --- | --- |
| `HACKED` | `HIGH` | `HACKED_FUNDS` |
| `SANCTION` | `CRITICAL` | `SANCTIONED_ADDRESS` |
| `MIXER` | `HIGH` | `MIXER` |
| `PHISH` | `HIGH` | `PHISHING` |
| 기타 | `LOW` | `NORMAL` |

위험 주소로 판정되면 `wallet_address_risk`에 저장되어 이후 평가에서 재사용됩니다.
