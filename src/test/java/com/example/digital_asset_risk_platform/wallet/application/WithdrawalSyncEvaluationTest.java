package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
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

@SpringBootTest(properties = "fds.evaluation.mode=sync")
public class WithdrawalSyncEvaluationTest extends IntegrationTestSupport {

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
    @DisplayName("sync 모드에서는 출금 요청 시 FDS 평가가 즉시 수행된다")
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
        Assertions.assertThat(response.status()).isEqualTo(WithdrawalStatus.APPROVED);
        Assertions.assertThat(response.riskLevel()).isEqualTo(RiskLevel.NORMAL);
        Assertions.assertThat(response.decision()).isEqualTo(RiskDecisionType.ALLOW);
        Assertions.assertThat(response.totalScore()).isEqualTo(20);
        Assertions.assertThat(response.caseId()).isNull();

        Assertions.assertThat(riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId())).isTrue();

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(response.withdrawalId()).orElseThrow();
        Assertions.assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
    }

    @Test
    @DisplayName("sync 모드에서도 WithdrawalRequestedEvent는 Outbox에 저장된다")
    void case2() {
        //given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "TNORMAL000002",
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
                "TNORMAL000002",
                new BigDecimal("100.000000000000000000")
        ));

        //then
        Assertions.assertThat(outboxEventRepository.findAll())
                .extracting(OutboxEvent::getEventType)
                .contains("WithdrawalRequestedEvent", "RiskEvaluationCompletedEvent");
    }
}
