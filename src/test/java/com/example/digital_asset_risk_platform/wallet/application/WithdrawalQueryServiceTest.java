package com.example.digital_asset_risk_platform.wallet.application;

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
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalDetailResponse;
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
public class WithdrawalQueryServiceTest extends IntegrationTestSupport {

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
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
    }

    @Test
    @DisplayName("FDS 평가 전 출금 상세 조회 시 평가 결과는 null이다")
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
                new BigDecimal("100")
        ));

        //when
        WithdrawalDetailResponse detail = withdrawalService.getWithdrawal(response.withdrawalId());

        //then
        Assertions.assertThat(detail.withdrawalId()).isEqualTo(response.withdrawalId());
        Assertions.assertThat(detail.status()).isEqualTo(WithdrawalStatus.EVALUATING);
        Assertions.assertThat(detail.riskLevel()).isNull();
        Assertions.assertThat(detail.decision()).isNull();
        Assertions.assertThat(detail.totalScore()).isNull();
    }
}
