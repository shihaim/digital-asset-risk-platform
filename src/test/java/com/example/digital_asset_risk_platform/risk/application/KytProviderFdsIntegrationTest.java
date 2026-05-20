package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class KytProviderFdsIntegrationTest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @BeforeEach
    void setUp() {
        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
    }

    @Test
    @DisplayName("사전 등록되지 않은 고위험 주소도 KYT Provider Mock 조회를 통해 차단한다")
    void case1() {
        //given
        String chainType = "TRON";
        String hackedAddress = "THACKED-KYT-MOCK-001";

        assertThat(walletAddressRiskRepository.existsByChainTypeAndAddress(chainType, hackedAddress)).isFalse();

        //when
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                10001L,
                "USDT",
                chainType,
                hackedAddress,
                new BigDecimal("10000.000000000000000000")
        ));

        //then
        assertThat(response.status()).isEqualTo(WithdrawalStatus.BLOCKED);
        assertThat(response.caseId()).isNotNull();
        assertThat(walletAddressRiskRepository.existsByChainTypeAndAddress(chainType, hackedAddress)).isTrue();
        assertThat(riskCaseRepository.count()).isOne();

        RiskEvaluation evaluation = riskEvaluationRepository
                .findByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId())
                .orElseThrow();

        assertThat(riskRuleHitRepository.findByEvaluationId(evaluation.getId()))
                .extracting(RiskRuleHit::getRuleCode)
                .contains("HIGH_RISK_WALLET");
    }

}
