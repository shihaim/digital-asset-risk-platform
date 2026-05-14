package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest(properties = "fds.evaluation.mode=async")
class FdsWithdrawalEvaluationServiceTest extends IntegrationTestSupport {

    @Autowired
    FdsWithdrawalEvaluationService fdsWithdrawalEvaluationService;

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

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
    @DisplayName("withdrawal.requested 이벤트 처리 시 정상 출금을 평가하고 APPROVED로 변경한다")
    void case1() {
        //given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "TNORMAL000001",
                WalletRiskLevel.LOW,
                0,
                "NORMAL",
                "MOCK_KYT"
        ));

        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        ));

        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-normal-eval-001",
                response.withdrawalId(),
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        fdsWithdrawalEvaluationService.evaluate(event);

        //then
        WithdrawalRequest saved = withdrawalRequestRepository
                .findById(response.withdrawalId())
                .orElseThrow();

        Assertions.assertThat(saved.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
        Assertions.assertThat(riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId())).isTrue();
        Assertions.assertThat(riskCaseRepository.count()).isZero();
        Assertions.assertThat(consumerProcessedEventRepository.existsByConsumerNameAndEventId(
                "fds-withdrawal-consumer",
                "event-normal-eval-001"
        )).isTrue();
    }

    @Test
    @DisplayName("고위험 지갑 출금 이벤트 처리 시 BLOCKED로 변경하고 RiskCase를 생성한다")
    void case2() {
        //given
        Long userId = 20001L;
        LocalDateTime now = LocalDateTime.now();

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "THACKED000001",
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
                "new_device-001",
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                "TRON",
                "THACKED000001",
                new BigDecimal("10000.000000000000000000")
        ));

        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-high-risk-eval-001",
                response.withdrawalId(),
                userId,
                "USDT",
                "TRON",
                "THACKED000001",
                new BigDecimal("10000.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        fdsWithdrawalEvaluationService.evaluate(event);

        //then
        WithdrawalRequest saved = withdrawalRequestRepository
                .findById(response.withdrawalId())
                .orElseThrow();

        Assertions.assertThat(saved.getStatus()).isEqualTo(WithdrawalStatus.BLOCKED);
        Assertions.assertThat(riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId())).isTrue();
        Assertions.assertThat(riskRuleHitRepository.count()).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(riskCaseRepository.count()).isOne();
        Assertions.assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .contains("WithdrawalRequestedEvent", "RiskEvaluationCompletedEvent", "RiskCaseCreatedEvent");
    }

    @Test
    @DisplayName("같은 eventId를 두 번 처리해도 FDS 평가는 한 번만 수행된다")
    void case3() {
        //given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "THACKED000002",
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                30001L,
                "USDT",
                "TRON",
                "THACKED000002",
                new BigDecimal("10000.000000000000000000")
        ));

        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-duplicate-eval",
                response.withdrawalId(),
                30001L,
                "USDT",
                "TRON",
                "THACKED000002",
                new BigDecimal("10000.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        fdsWithdrawalEvaluationService.evaluate(event);
        fdsWithdrawalEvaluationService.evaluate(event);

        //then
        Assertions.assertThat(riskEvaluationRepository.count()).isOne();
        Assertions.assertThat(riskCaseRepository.count()).isOne();
        Assertions.assertThat(consumerProcessedEventRepository.count()).isOne();
    }

    @Test
    @DisplayName("이미 평가된 withdrawalId는 다른 eventId로 들어와도 재평가하지 않는다")
    void case4() {
        //given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "THACKED000003",
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                40001L,
                "USDT",
                "TRON",
                "THACKED000003",
                new BigDecimal("10000.000000000000000000")
        ));

        WithdrawalRequestedEvent firstEvent = new WithdrawalRequestedEvent(
                "event-first-eval",
                response.withdrawalId(),
                40001L,
                "USDT",
                "TRON",
                "THACKED000003",
                new BigDecimal("10000.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        WithdrawalRequestedEvent secondEvent = new WithdrawalRequestedEvent(
                "event-second-eval",
                response.withdrawalId(),
                40001L,
                "USDT",
                "TRON",
                "THACKED000003",
                new BigDecimal("10000.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        fdsWithdrawalEvaluationService.evaluate(firstEvent);
        fdsWithdrawalEvaluationService.evaluate(secondEvent);

        //then
        Assertions.assertThat(riskEvaluationRepository.count()).isOne();
        Assertions.assertThat(riskCaseRepository.count()).isOne();

        Assertions.assertThat(consumerProcessedEventRepository.existsByConsumerNameAndEventId(
                "fds-withdrawal-consumer",
                "event-second-eval"
        )).isTrue();
    }
}