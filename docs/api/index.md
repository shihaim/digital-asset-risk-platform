# API Documentation

API 문서는 도메인별로 분리합니다. 이 문서는 전체 API의 입구 역할을 하며, 각 상세 문서로 이동하기 위한 목차와 공통 규칙을 정리합니다.

---

## 1. API Groups

| Group | Document | Description |
| --- | --- | --- |
| Withdrawals | [withdrawals.md](withdrawals.md) | 출금 요청 생성/조회, sync/async FDS 응답 |
| Risk Cases | [risk-cases.md](risk-cases.md) | 관리자 RiskCase 조회 및 심사 처리 |
| Admin Operations | [admin-operations.md](admin-operations.md) | Outbox, 알림, 대시보드, 사용자 타임라인, 사용자 위험 프로필 |
| Risk Rules | [risk-rules.md](risk-rules.md) | Rule 설정, 변경 이력, 시뮬레이션, Rule 적중 통계 |
| Support | [support.md](support.md) | FDS 시나리오 재현용 보조 API |

---

## 2. Base URL

```text
http://localhost:8080
```

---

## 3. Common Notes

- 현재 문서 기준 인증/인가 계층은 별도 적용하지 않았습니다.
- 요청/응답은 JSON을 기준으로 합니다.
- 페이지 응답은 Spring Data `Page` 형태를 따릅니다.
- 날짜/시간은 `yyyy-MM-dd'T'HH:mm:ss` 형식의 로컬 날짜시간을 사용합니다.

---

## 4. Main Flow

```text
POST /api/withdrawals
  -> FDS 평가
  -> RiskEvaluation / RiskRuleHit 저장
  -> 위험 출금이면 RiskCase 생성
  -> 관리자 RiskCase 조회/심사
```

async mode에서는 출금 요청 직후 `EVALUATING`을 응답하고, Kafka Consumer가 FDS 평가를 후속 처리합니다.

```text
POST /api/withdrawals
  -> outbox_event 저장
  -> withdrawal.requested 발행
  -> FdsWithdrawalConsumer 처리
  -> risk.evaluation.completed / risk.case.created 발행
  -> UserRiskProfile 갱신
```

---

## 5. Admin Rule Operations

Rule 설정 변경은 변경 전/후 값을 `risk_rule_config_history`에 저장합니다. 변경 이력은 `GET /api/admin/risk-rules/{ruleCode}/histories`에서 최신 변경 시각순으로 조회할 수 있습니다.

Rule 시뮬레이션은 `POST /api/admin/risk-rules/simulate`에서 제공합니다. 이 API는 평가 결과만 반환하며 `WithdrawalRequest`, `RiskEvaluation`, `RiskRuleHit`, `RiskCase`를 저장하지 않습니다.

---

## 6. Status Values

대표 상태 값은 다음 문서에서 함께 설명합니다.

- Withdrawal status: [withdrawals.md](withdrawals.md)
- RiskCase status: [risk-cases.md](risk-cases.md)
- Outbox status: [admin-operations.md](admin-operations.md)
- Rule config: [risk-rules.md](risk-rules.md)
