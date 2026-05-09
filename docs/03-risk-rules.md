# FDS Risk Rule 정책

## 1. Rule 평가 개요

출금 요청이 생성되면 `RiskContext`를 구성하고, 각 Rule이 해당 출금의 위험 신호를 평가합니다.

각 Rule은 다음 정보를 반환합니다.

| 필드 | 설명 |
| --- | --- |
| `ruleCode` | Rule 식별자 |
| `ruleName` | Rule 이름 |
| `score` | 위험 점수 |
| `reason` | 적중 사유 |
| `blocking` | 즉시 차단 여부 |

## 2. Rule 목록

### `NEW_DEVICE_WITHDRAWAL`

| 항목 | 내용 |
| --- | --- |
| 설명 | 최근 1시간 이내 신규 기기 로그인 후 출금 |
| 점수 | 30 |
| blocking | false |
| 위험 의미 | 계정 탈취자가 신규 기기로 로그인 후 즉시 출금하는 패턴 |
| 조치 | 단독 적중 시 모니터링 또는 다른 Rule과 조합 평가 |

### `OTP_RESET_WITHDRAWAL`

| 항목 | 내용 |
| --- | --- |
| 설명 | 최근 24시간 이내 OTP 재설정 후 출금 |
| 점수 | 40 |
| blocking | false |
| 위험 의미 | 공격자가 인증 수단을 재설정한 뒤 출금을 시도하는 패턴 |
| 조치 | 신규 기기/신규 지갑과 결합 시 보류 가능 |

### `PASSWORD_CHANGED_WITHDRAWAL`

| 항목 | 내용 |
| --- | --- |
| 설명 | 최근 24시간 이내 비밀번호 변경 후 출금 |
| 점수 | 30 |
| blocking | false |
| 위험 의미 | 계정 탈취 후 비밀번호 변경 및 자산 출금 가능성 |
| 조치 | 다른 계정 탈취 신호와 조합 평가 |

### `NEW_WALLET_ADDRESS`

| 항목 | 내용 |
| --- | --- |
| 설명 | 사용자가 과거에 출금한 이력이 없는 신규 지갑 주소로 출금 |
| 점수 | 20 |
| blocking | false |
| 위험 의미 | 탈취자가 자신의 지갑으로 최초 출금하는 패턴 |
| 조치 | 단독으로는 허용 가능하나 다른 Rule과 조합 시 보류 가능 |

### `HIGH_AMOUNT_WITHDRAWAL`

| 항목 | 내용 |
| --- | --- |
| 설명 | 사용자 평균 출금액 대비 10배 이상 출금 |
| 점수 | 40 |
| blocking | false |
| 위험 의미 | 평소 패턴과 다른 고액 출금 |
| 조치 | 추가 인증 또는 관리자 심사 가능 |

### `FREQUENT_WITHDRAWAL_24H`

| 항목 | 내용 |
| --- | --- |
| 설명 | 최근 24시간 내 5회 이상 반복 출금 |
| 점수 | 30 |
| blocking | false |
| 위험 의미 | 계정 탈취 후 분할 출금 또는 자산 분산 가능성 |
| 조치 | 모니터링 또는 보류 |

### `HIGH_RISK_WALLET`

| 항목 | 내용 |
| --- | --- |
| 설명 | 내부 위험 주소 DB에서 HIGH/CRITICAL 등 고위험으로 분류된 지갑으로 출금 |
| 점수 | 100 |
| blocking | true |
| 위험 의미 | 해킹 자금, 제재 주소, 피싱 주소, 믹서 연관 주소 가능성 |
| 조치 | 즉시 `BLOCKED` 및 심사 Case 생성 |

## 3. Decision 정책

| 총점/조건 | RiskLevel | Decision | WithdrawalStatus |
| --- | --- | --- | --- |
| 0~29 | `NORMAL` | `ALLOW` | `APPROVED` |
| 30~59 | `WATCH` | `MONITOR` | `APPROVED` |
| 60~79 | `CAUTION` | `REQUIRE_ADDITIONAL_AUTH` | `HELD` |
| 80~119 | `HIGH` | `HOLD_WITHDRAWAL` | `HELD` |
| 120 이상 | `CRITICAL` | `HOLD_WITHDRAWAL` | `HELD` |
| `blocking=true` | `CRITICAL` | `BLOCK_WITHDRAWAL` | `BLOCKED` |

