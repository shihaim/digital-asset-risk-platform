package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
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
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBootTest(properties = "fds.evaluation.mode=async")
@ActiveProfiles("test")
public class WithdrawalAsyncEvaluationTest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

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
    @DisplayName("async 모드에서는 출금 요청 시 EVALUATING 상태를 반환하고 FDS 평가는 즉시 수행하지 않는다")
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
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        ));

        //then
        Assertions.assertThat(response.withdrawalId()).isNotNull();
        Assertions.assertThat(response.status()).isEqualTo(WithdrawalStatus.EVALUATING);
        Assertions.assertThat(response.riskLevel()).isNull();
        Assertions.assertThat(response.decision()).isNull();
        Assertions.assertThat(response.totalScore()).isNull();
        Assertions.assertThat(response.caseId()).isNull();
    }

    @Test
    @DisplayName("async 모드에서는 출금 요청 이벤트만 Outbox에 저장하고 평가 완료 이벤트는 아직 저장하지 않는다")
    void case2() {
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "TNORMAL000002",
                WalletRiskLevel.LOW,
                0,
                "NORMAL",
                "MOCK_KYT"
        ));

        //when
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000002",
                new BigDecimal("100.000000000000000000")
        ));

        //then
        Assertions.assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .contains("WithdrawalRequestedEvent")
                .doesNotContain("RiskEvaluationCompletedEvent")
                .doesNotContain("RiskCaseCreatedEvent");

        Assertions.assertThat(outboxEventRepository.count()).isOne();
    }
}
