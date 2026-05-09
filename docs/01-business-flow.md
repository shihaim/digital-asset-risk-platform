# 출금 FDS 비즈니스 흐름

## 1. 출금 요청 흐름

### sync 모드

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant FDS
    participant DB

    Client->>API: 출금 요청
    API->>DB: Withdrawal 저장
    API->>FDS: FDS 평가
    FDS->>DB: RiskEvaluation 저장
    FDS->>DB: RiskRuleHit 저장
    FDS->>DB: RiskCase 생성 여부 결정
    API-->>Client: 최종 평가 결과 응답
```

### async 모드

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Outbox
    participant Kafka
    participant Consumer
    participant DB

    Client->>API: 출금 요청
    API->>DB: Withdrawal 저장(EVALUATING)
    API->>Outbox: WithdrawalRequestedEvent 저장
    API-->>Client: EVALUATING 응답
    Outbox->>Kafka: 이벤트 발행
    Kafka->>Consumer: 이벤트 소비
    Consumer->>DB: FDS 평가 결과 저장
    Consumer->>DB: 출금 상태 변경
    Consumer->>DB: RiskCase 생성
```

## 2. 위험 판단 결과

| Decision | WithdrawalStatus | 설명 |
| --- | --- | --- |
| `ALLOW` | `APPROVED` | 정상 출금 |
| `MONITOR` | `APPROVED` | 모니터링 대상이나 출금 허용 |
| `REQUIRE_ADDITIONAL_AUTH` | `HELD` | 추가 인증 또는 관리자 확인 필요 |
| `HOLD_WITHDRAWAL` | `HELD` | 출금 보류 |
| `BLOCK_WITHDRAWAL` | `BLOCKED` | 자동 차단 |

## 3. 관리자 심사 흐름

```mermaid
sequenceDiagram
    participant Admin
    participant API
    participant DB

    Admin->>API: RiskCase 목록 조회
    Admin->>API: RiskCase 상세 조회
    API->>DB: 출금/평가/RuleHit/Timeline 조회
    Admin->>API: 승인/거절/오탐/정탐 처리
    API->>DB: RiskCase 상태 변경
    API->>DB: Withdrawal 상태 변경
```

## 4. 심사 액션

| API | 결과 |
| --- | --- |
| `POST /api/admin/risk-cases/{caseId}/start-review` | Case를 `IN_REVIEW`로 변경 |
| `POST /api/admin/risk-cases/{caseId}/approve` | Case 승인 및 출금 승인 |
| `POST /api/admin/risk-cases/{caseId}/reject` | Case 거절 및 출금 거절 |
| `POST /api/admin/risk-cases/{caseId}/mark-false-positive` | 오탐 처리 및 출금 승인 |
| `POST /api/admin/risk-cases/{caseId}/mark-true-positive` | 정탐 처리 및 출금 거절 |

