package com.example.digital_asset_risk_platform.e2e;

import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.admin.application.AdminRiskCaseService;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseDetailResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseReviewRequest;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineEventResponse;
import com.example.digital_asset_risk_platform.admin.dto.RuleHitResponse;
import com.example.digital_asset_risk_platform.common.config.TestObjectMapperFactory;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.consumer.AdminNotificationConsumer;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.application.FdsWithdrawalEvaluationService;
import com.example.digital_asset_risk_platform.risk.domain.*;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = "fds.evaluation.mode=async")
public class WithdrawalFdsE2ETest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    FdsWithdrawalEvaluationService fdsWithdrawalEvaluationService;

    @Autowired
    AdminNotificationConsumer adminNotificationConsumer;

    @Autowired
    AdminRiskCaseService adminRiskCaseService;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    AdminNotificationRepository adminNotificationRepository;

    @Autowired
    ConsumerProcessedEventRepository consumerProcessedEventRepository;

    @Autowired(required = false)
    RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    private final ObjectMapper objectMapper = TestObjectMapperFactory.create();

    @BeforeEach
    void setUp() {
        if (riskRuleStatisticsRepository != null) {
            riskRuleStatisticsRepository.deleteAll();
        }

        consumerProcessedEventRepository.deleteAll();
        adminNotificationRepository.deleteAll();
        outboxEventRepository.deleteAll();

        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();

        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();

        accountSecurityEventRepository.deleteAll();
        accountLoginEventRepository.deleteAll();
    }

    @Test
    @DisplayName("고위험 출금 요청부터 비동기 FDS 평가, Case 생성, 관리자 정탐 처리까지 전체 흐름을 검증한다")
    void case1() throws JsonProcessingException {
        //given
        Long userId = 10001L;
        String chainType = "TRON";
        String highRiskAddress = "THACKED000001";
        LocalDateTime now = LocalDateTime.now();

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                chainType,
                highRiskAddress,
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        accountEventService.createLoginEvent(new LoginEventCreateRequest(
                userId,
                "new-device-001",
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-001",
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        //when 1: async 모드로 출금 요청
        WithdrawalCreateResponse createResponse = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                chainType,
                highRiskAddress,
                new BigDecimal("10000.000000000000000000")
        ));

        //then 1: 출금은 아직 평가 완료가 아니라 EVALUATING
        assertThat(createResponse.withdrawalId()).isNotNull();
        assertThat(createResponse.status()).isEqualTo(WithdrawalStatus.EVALUATING);
        assertThat(createResponse.riskLevel()).isNull();
        assertThat(createResponse.decision()).isNull();
        assertThat(createResponse.totalScore()).isNull();
        assertThat(createResponse.caseId()).isNull();

        assertThat(riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", createResponse.withdrawalId()))
                .isFalse();

        WithdrawalRequest evaluatingWithdrawal = withdrawalRequestRepository.findById(createResponse.withdrawalId())
                .orElseThrow();

        assertThat(evaluatingWithdrawal.getStatus()).isEqualTo(WithdrawalStatus.EVALUATING);

        //then 2: WithdrawalRequestedEvent가 Outbox에 저장됨
        OutboxEvent withdrawalRequestedOutbox = findOutboxEvent("WithdrawalRequestedEvent");

        assertThat(withdrawalRequestedOutbox.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(withdrawalRequestedOutbox.getTopicName()).isEqualTo(KafkaTopicConfig.WITHDRAWAL_REQUESTED);
        assertThat(withdrawalRequestedOutbox.getMessageKey()).isEqualTo(String.valueOf(createResponse.withdrawalId()));

        WithdrawalRequestedEvent withdrawalRequestedEvent = objectMapper.readValue(withdrawalRequestedOutbox.getPayloadJson(), WithdrawalRequestedEvent.class);

        assertThat(withdrawalRequestedEvent.withdrawalId()).isEqualTo(createResponse.withdrawalId());
        assertThat(withdrawalRequestedEvent.userId()).isEqualTo(userId);
        assertThat(withdrawalRequestedEvent.status()).isEqualTo("EVALUATING");

        //when 2: withdrawal.requested 이벤트를 FDS Consumer 처리 로직에 전달
        fdsWithdrawalEvaluationService.evaluate(withdrawalRequestedEvent);

        //then 3: FDS 평가 결과 저장 및 출금 차단
        WithdrawalRequest blockedWithdrawal = withdrawalRequestRepository.findById(createResponse.withdrawalId())
                .orElseThrow();

        assertThat(blockedWithdrawal.getChainType()).isEqualTo(chainType);
        assertThat(blockedWithdrawal.getToAddress()).isEqualTo(highRiskAddress);
        assertThat(blockedWithdrawal.getStatus()).isEqualTo(WithdrawalStatus.BLOCKED);

        RiskEvaluation evaluation = riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", createResponse.withdrawalId())
                .orElseThrow();

        assertThat(evaluation.getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(evaluation.getDecision()).isEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);
        assertThat(evaluation.getTotalScore()).isGreaterThanOrEqualTo(100);

        assertThat(riskRuleHitRepository.findByEvaluationId(evaluation.getId()))
                .extracting(RiskRuleHit::getRuleCode)
                .contains("HIGH_RISK_WALLET");

        RiskCase riskCase = riskCaseRepository.findByEvaluationId(evaluation.getId())
                .orElseThrow();

        assertThat(riskCase.getCaseType()).isEqualTo(RiskCaseType.AML_REVIEW);
        assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.REVIEW_REQUIRED);
        assertThat(riskCase.getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);

        //then 4: 평가 완료 / Case 생성 이벤트가 Outbox에 추가됨
        assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .contains(
                        "WithdrawalRequestedEvent",
                        "RiskEvaluationCompletedEvent",
                        "RiskCaseCreatedEvent"
                );

        OutboxEvent caseCreatedOutbox = findOutboxEvent("RiskCaseCreatedEvent");

        RiskCaseCreatedEvent caseCreatedEvent = objectMapper.readValue(caseCreatedOutbox.getPayloadJson(), RiskCaseCreatedEvent.class);

        assertThat(caseCreatedEvent.caseId()).isEqualTo(riskCase.getId());
        assertThat(caseCreatedEvent.evaluationId()).isEqualTo(evaluation.getId());
        assertThat(caseCreatedEvent.caseType()).isEqualTo(RiskCaseType.AML_REVIEW.name());
        assertThat(caseCreatedEvent.riskLevel()).isEqualTo(RiskLevel.CRITICAL.name());

        //when 3: risk.case.created 이벤트를 관리자 알림 Consumer가 처리
        adminNotificationConsumer.consume(caseCreatedEvent, acknowledgment);

        //then 5: 관리자 알림 생성
        verify(acknowledgment).acknowledge();

        assertThat(adminNotificationRepository.count()).isOne();

        //when 4: 관리자 Case 상세 조회
        RiskCaseDetailResponse detail = adminRiskCaseService.getRiskCaseDetail(riskCase.getId());

        //then 6: 상세 정보에 출금 / 평가 / RuleHit / Timeline 포함
        assertThat(detail.caseId()).isEqualTo(riskCase.getId());
        assertThat(detail.caseType()).isEqualTo(RiskCaseType.AML_REVIEW);
        assertThat(detail.status()).isEqualTo(RiskCaseStatus.REVIEW_REQUIRED);
        assertThat(detail.riskLevel()).isEqualTo(RiskLevel.CRITICAL);

        assertThat(detail.withdrawal().withdrawalId()).isEqualTo(createResponse.withdrawalId());
        assertThat(detail.withdrawal().status()).isEqualTo(WithdrawalStatus.BLOCKED);

        assertThat(detail.evaluation().evaluationId()).isEqualTo(evaluation.getId());
        assertThat(detail.evaluation().decision()).isEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);

        assertThat(detail.ruleHits())
                .extracting(RuleHitResponse::ruleCode)
                .contains("HIGH_RISK_WALLET");

        assertThat(detail.timeline())
                .extracting(RiskTimelineEventResponse::eventType)
                .contains("LOGIN", "OTP_RESET", "WITHDRAWAL_REQUESTED");

        //when 5: 관리자가 정탐 처리
        adminRiskCaseService.markTruePositive(
                riskCase.getId(),
                new RiskCaseReviewRequest(
                        "admin01",
                        "고위험 지갑 및 계정 탈취 의심 정황 확인"
        ));

        //then 7: Case TRUE_POSITIVE, 출금 REJECTED
        RiskCase reviewedCase = riskCaseRepository.findById(riskCase.getId())
                .orElseThrow();

        WithdrawalRequest rejectedWithdrawal = withdrawalRequestRepository.findById(createResponse.withdrawalId())
                .orElseThrow();

        assertThat(reviewedCase.getStatus()).isEqualTo(RiskCaseStatus.TRUE_POSITIVE);
        assertThat(reviewedCase.getAssignedTo()).isEqualTo("admin01");
        assertThat(reviewedCase.getReviewComment()).contains("고위험 지갑");
        assertThat(reviewedCase.getClosedAt()).isNotNull();

        assertThat(rejectedWithdrawal.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);
        assertThat(rejectedWithdrawal.getRejectedAt()).isNotNull();
    }

    private OutboxEvent findOutboxEvent(String eventType) {
        return outboxEventRepository.findAll()
                .stream()
                .filter(event -> event.getEventType().equals(eventType))
                .max(Comparator.comparing(OutboxEvent::getId))
                .orElseThrow(() -> new AssertionError("OutboxEvent not found. eventType=" + eventType));
    }
}
