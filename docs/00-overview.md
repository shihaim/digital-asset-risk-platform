# 프로젝트 개요

## 1. 문제 정의

디지털 자산 서비스에서 출금은 자산 유출과 직접 연결되는 고위험 행위입니다.

특히 다음과 같은 상황은 계정 탈취, 피싱, 자금세탁 가능성으로 이어질 수 있습니다.

- 신규 기기 로그인 직후 출금
- OTP 재설정 직후 출금
- 비밀번호 변경 직후 출금
- 신규 지갑 주소로 고액 출금
- 고위험 지갑 주소로 출금
- 짧은 시간 내 반복 출금

본 프로젝트는 이러한 위험 신호를 Rule 기반으로 평가하고, 위험 출금은 자동 보류 또는 차단한 뒤 관리자 심사 Case로 전환하는 것을 목표로 합니다.

## 2. 목표

- 출금 요청에 대한 FDS 평가 자동화
- 위험 출금의 상태 변경 및 평가 증적 저장
- 관리자 심사 업무 흐름 구현
- Kafka 기반 이벤트 처리 구조 구성
- Outbox Pattern 기반 이벤트 발행 안정성 확보
- sync / async 평가 모드 전환 지원
- Consumer 멱등 처리로 중복 이벤트 처리 방지

## 3. 주요 도메인

| 도메인 | 역할 |
| --- | --- |
| `WithdrawalRequest` | 출금 요청과 상태를 관리 |
| `RiskEvaluation` | FDS 평가 결과 저장 |
| `RiskRuleHit` | 적중한 Rule과 점수, 사유 저장 |
| `RiskCase` | 관리자 심사 Case 관리 |
| `WalletAddressRisk` | 위험 지갑 주소 정보 관리 |
| `AccountLoginEvent` | 로그인 및 신규 기기 여부 관리 |
| `AccountSecurityEvent` | OTP 재설정, 비밀번호 변경 등 보안 이벤트 관리 |
| `OutboxEvent` | Kafka 발행 대상 이벤트 저장 |
| `ConsumerProcessedEvent` | Consumer별 이벤트 처리 이력 저장 |
| `AuditEventLog` | Kafka 이벤트 감사 로그 저장 |
| `AdminNotification` | 관리자 알림 저장 |
| `RiskRuleStatistics` | Rule 적중 통계 저장 |

## 4. 평가 모드

`fds.evaluation.mode` 설정에 따라 출금 FDS 평가 방식을 전환합니다.

| 모드 | 설명 |
| --- | --- |
| `sync` | 출금 요청 API 안에서 FDS 평가를 즉시 수행하고 최종 상태를 반환 |
| `async` | 출금 요청은 `EVALUATING`으로 응답하고 Kafka Consumer가 FDS 평가를 수행 |

