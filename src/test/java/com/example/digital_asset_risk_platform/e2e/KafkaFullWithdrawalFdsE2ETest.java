package com.example.digital_asset_risk_platform.e2e;

import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.audit.repository.AuditEventLogRepository;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventPublisher;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
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
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaFullWithdrawalFdsE2ETest extends KafkaIntegrationTestSupport {

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

    @Autowired
    AuditEventLogRepository auditEventLogRepository;

    @Autowired
    AdminNotificationRepository adminNotificationRepository;

    @Autowired
    RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    @BeforeEach
    void setUp() {
        riskRuleStatisticsRepository.deleteAll();
        adminNotificationRepository.deleteAll();
        auditEventLogRepository.deleteAll();
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
    @DisplayName("Kafka 기반 전체 출금 FDS E2E: 출금 요청, 비동기 평가, Case 생성, 감사 로그, 알림, Rule 통계까지 검증한다")
    void case1() {
        //given
        Long userId = 10001L;
        String chainType = "TRON";
        String highRiskAddress = "THACKED-FULL-KAFKA-E2E-001";
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
                "new-device-full-kafka-e2e",
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-full-kafka-e2e",
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        //when 1: async 출금 요청
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                chainType,
                highRiskAddress,
                new BigDecimal("10000.000000000000000000")
        ));

        //then 1: 최초 상태는 EVALUATING
        assertThat(response.status()).isEqualTo(WithdrawalStatus.EVALUATING);
        assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .containsExactly("WithdrawalRequestedEvent");

        //when 2: withdrawal.requested를 Kafka로 발행
        outboxEventPublisher.publishPendingEvents();

        //then 2: FDS Consumer가 평가를 완료할 때까지 대기
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(response.withdrawalId())
                            .orElseThrow();

                    assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.BLOCKED);

                    assertThat(riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId()))
                            .isTrue();

                    assertThat(riskCaseRepository.count()).isOne();

                    assertThat(outboxEventRepository.findAll())
                            .extracting(OutboxEvent::getEventType)
                            .contains("WithdrawalRequestedEvent", "RiskEvaluationCompletedEvent", "RiskCaseCreatedEvent");
                });

        //when 3: 평가 완료/Case 생성 이벤트를 Kafka로 발행
        outboxEventPublisher.publishPendingEvents();

        //then 3: 후속 Consumer들이 모두 처리할 때까지 대기
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    RiskEvaluation evaluation = riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId())
                            .orElseThrow();

                    assertThat(adminNotificationRepository.count()).isOne();

                    Optional<RiskRuleStatistics> statisticsOpt = riskRuleStatisticsRepository.findByRuleCode("HIGH_RISK_WALLET");
                    assertThat(statisticsOpt).isPresent();
                    RiskRuleStatistics statistics = statisticsOpt.get();

                    assertThat(statistics.getHitCount()).isOne();

                    // [??] expected 3, but was 4 발생
                    // [2026-05-14] 전체 테스트시 expected 3, but was 2 발생
                    assertThat(auditEventLogRepository.count()).isEqualTo(3);

                    assertThat(outboxEventRepository.findAll())
                            .allSatisfy(event -> assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.SENT));
                });
    }
}
