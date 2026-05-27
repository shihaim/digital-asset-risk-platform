# Withdrawals API

출금 요청 생성과 출금 상세 조회 API입니다. 출금 생성 시 FDS 평가 모드에 따라 sync/async 응답이 달라집니다.

---

## 1. Create Withdrawal

```http
POST /api/withdrawals
```

### Request

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `userId` | number | yes | 사용자 ID |
| `assetSymbol` | string | yes | 자산 심볼 |
| `chainType` | string | yes | 체인 종류 |
| `toAddress` | string | yes | 출금 대상 주소 |
| `amount` | decimal | yes | 출금 수량. 0보다 커야 함 |

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

async mode에서는 출금 요청을 `EVALUATING` 상태로 저장하고 FDS 평가는 Kafka Consumer가 처리합니다.

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

sync mode에서는 요청 처리 중 FDS 평가를 완료하고 최종 결과를 응답합니다.

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

---

## 2. Get Withdrawal Detail

```http
GET /api/withdrawals/{withdrawalId}
```

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

---

## 3. Withdrawal Status

| Status | Description |
| --- | --- |
| `EVALUATING` | async mode에서 FDS 평가 대기/진행 중 |
| `APPROVED` | 출금 승인 |
| `HELD` | 추가 확인 또는 관리자 심사 필요 |
| `BLOCKED` | 차단 |
| `REJECTED` | 관리자 심사 등으로 거절 |

---

## 4. FDS Fields

| Field | Description |
| --- | --- |
| `riskLevel` | FDS 위험도 |
| `decision` | FDS 판단 결과 |
| `totalScore` | 적중 Rule 점수 합계 |
| `caseId` | 생성된 RiskCase ID. 케이스가 없으면 `null` |
