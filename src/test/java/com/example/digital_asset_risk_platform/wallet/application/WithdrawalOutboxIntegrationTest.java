package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ActiveProfiles("test")
public class WithdrawalOutboxIntegrationTest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @BeforeEach
    void setUp() {
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
    @DisplayName("정상 출금 요청 시 출금 요청 이벤트와 평가 완료 이벤트를 Outbox에 저장한다")
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

        //when
        withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        ));

        //then
        Assertions.assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .contains("WithdrawalRequestedEvent", "RiskEvaluationCompletedEvent")
                .doesNotContain("RiskCaseCreatedEvent");

        Assertions.assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getStatus)
                .containsOnly(OutboxEventStatus.PENDING);
    }

    @Test
    @DisplayName("고위험 출금 요청 시 Case 생성 이벤트까지 Outbox에 저장한다")
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
                "new-device-001",
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        //when
        withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                "TRON",
                "THACKED000001",
                new BigDecimal("10000.000000000000000000")
        ));

        //then
        Assertions.assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .contains("WithdrawalRequestedEvent", "RiskEvaluationCompletedEvent", "RiskCaseCreatedEvent");

        Assertions.assertThat(outboxEventRepository.count()).isEqualTo(3);
    }

}
