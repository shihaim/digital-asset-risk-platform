# 테스트 전략

## 1. 테스트 목표

본 프로젝트는 단순 CRUD 테스트보다 FDS 비즈니스 정합성을 검증하는 데 초점을 둡니다.

핵심 검증 대상:

- Rule 조건 적중 여부
- DecisionEngine 판단 결과
- 출금 상태 전이
- RiskEvaluation / RiskRuleHit 저장
- RiskCase 생성
- 관리자 심사 결과 반영
- Kafka 이벤트 발행
- Outbox 상태 변경
- Consumer 멱등 처리
- sync / async 평가 모드 차이

## 2. 테스트 계층

| 계층 | 테스트 |
| --- | --- |
| Unit Test | Rule, DecisionEngine, Consumer 위임 |
| Integration Test | WithdrawalService, AdminRiskCaseService, OutboxEventPublisher |
| E2E Test | 출금 요청부터 관리자 심사까지 전체 흐름 |

## 3. 주요 테스트 목록

### Rule 단위 테스트

- 신규 기기 로그인 후 출금 Rule
- OTP 재설정 후 출금 Rule
- 비밀번호 변경 후 출금 Rule
- 신규 지갑 주소 Rule
- 고위험 지갑 Rule
- 고액 출금 Rule
- 반복 출금 Rule

### DecisionEngine 테스트

- 점수 구간별 RiskLevel / Decision 검증
- blocking Rule 우선순위 검증

### 출금 FDS 통합 테스트

- 정상 출금 `APPROVED`
- 위험 출금 `HELD`
- 고위험 지갑 `BLOCKED`
- RiskCase 생성 여부 검증

### Kafka / Outbox 테스트

- OutboxEvent 저장
- Kafka 발행 성공 시 `SENT`
- Kafka 발행 실패 시 `FAILED`
- retryCount 증가
- 중복 eventId 저장 방지

### Async FDS 테스트

- async 모드 `EVALUATING` 응답
- Consumer 기반 FDS 평가
- 중복 eventId 처리 방지
- 이미 평가된 withdrawalId 재평가 방지

### E2E 테스트

- 고위험 출금 요청
- Outbox 이벤트 저장
- FDS 비동기 평가
- RiskCase 생성
- 관리자 알림 생성
- Case 상세 조회
- 정탐 처리
- 출금 `REJECTED` 처리

