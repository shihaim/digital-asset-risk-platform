package com.example.digital_asset_risk_platform.e2e;

import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventPublisher;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.support.KafkaIntegrationTestSupport;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=true",
        "fds.evaluation.mode=async"
})
@ActiveProfiles("test")
public class KafkaWithdrawalFdsAsyncE2ETest extends KafkaIntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    OutboxEventPublisher outboxEventPublisher;

    @Autowired
    OutboxEventRepository outboxEventRepository;

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
    ConsumerProcessedEventRepository consumerProcessedEventRepository;

    @BeforeEach
    void setUp() {
        consumerProcessedEventRepository.deleteAll();
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
    @DisplayName("withdrawal.requested 이벤트를 실제 Kafka로 발행하면 FdsWithdrawalConsumer가 소비하여 FDS 평가와 Case 생성을 수행한다")
    void case1() {
        //given
        Long userId = 10001L;
        String chainType = "TRON";
        String highRiskAddress = "THACKED-KAFKA-E2E-001";
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
                "new-device-kafka-e2e",
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-kafka-e2e",
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        //when 1: async 모드 출금 요청
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                chainType,
                highRiskAddress,
                new BigDecimal("10000.000000000000000000")
        ));

        //then 1: 아직 평가는 수행되지 않음
        assertThat(response.status()).isEqualTo(WithdrawalStatus.EVALUATING);

        assertThat(riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId()))
                .isFalse();

        OutboxEvent withdrawalRequestedOutbox = outboxEventRepository.findAll().get(0);
        assertThat(withdrawalRequestedOutbox.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(withdrawalRequestedOutbox.getTopicName()).isEqualTo(KafkaTopicConfig.WITHDRAWAL_REQUESTED);

        //when 2: FdsWithdrawalConsumer가 실제 Kafka로 withdrawal.requested 발행
        outboxEventPublisher.publishPendingEvents();

        //then 2: FdsWithdrawalConsumer가 실제 Kafka 메시지를 소비하여 평가 수행
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(response.withdrawalId())
                            .orElseThrow();

                    assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.BLOCKED);

                    RiskEvaluation evaluation = riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId())
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

                    assertThat(outboxEventRepository.findAll())
                            .extracting(OutboxEvent::getEventType)
                            .contains("WithdrawalRequestedEvent", "RiskEvaluationCompletedEvent", "RiskCaseCreatedEvent");

                    OutboxEvent sentWithdrawalRequested = outboxEventRepository.findAll().stream()
                            .filter(event -> event.getEventType().equals("WithdrawalRequestedEvent"))
                            .findFirst()
                            .orElseThrow();

                    assertThat(sentWithdrawalRequested.getStatus()).isEqualTo(OutboxEventStatus.SENT);

                    assertThat(consumerProcessedEventRepository.existsByConsumerNameAndEventId("fds-withdrawal-consumer", withdrawalRequestedOutbox.getEventId()))
                            .isTrue();
                });
    }
}
