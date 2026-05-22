package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;
import com.example.digital_asset_risk_platform.risk.support.RiskRuleConfigFixture;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.domain.WalletAddressRisk;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class RiskEvaluationRuleConfigIntegrationTest extends IntegrationTestSupport {

    @Autowired
    RiskEvaluationService riskEvaluationService;

    @Autowired
    RiskRuleConfigRepository riskRuleConfigRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;


    @BeforeEach
    void setUp() {
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();

        RiskRuleConfigFixture.ensureDefaultConfigs(riskRuleConfigRepository);
    }

    @Test
    @DisplayName("DB 설정에서 Rule이 비활성화되면 전체 평가 hit에서 제외된다")
    void case1() {
        //given
        RiskRuleConfig highRiskWalletConfig = riskRuleConfigRepository
                .findByRuleCode(RiskRuleCodes.HIGH_RISK_WALLET)
                .orElseThrow();
        highRiskWalletConfig.update(false, 100, true, null, "disabled test");
        riskRuleConfigRepository.saveAndFlush(highRiskWalletConfig);


        WithdrawalRequest withdrawal = saveHighRiskWithdrawal(10001L, "addr-disabled");

        //when
        RiskEvaluationResult result = riskEvaluationService.evaluationWithdrawal(withdrawal);

        //then
        assertThat(result.ruleHits())
                .extracting(RuleHit::ruleCode)
                .doesNotContain(RiskRuleCodes.HIGH_RISK_WALLET);
    }

    @Test
    @DisplayName("DB 설정의 score 변경이 hit score와 totalScore에 반영된다")
    void case2() {
        //given
        RiskRuleConfig highRiskWalletConfig = riskRuleConfigRepository
                .findByRuleCode(RiskRuleCodes.HIGH_RISK_WALLET)
                .orElseThrow();
        highRiskWalletConfig.update(true, 150, true, null, "score changed");
        riskRuleConfigRepository.saveAndFlush(highRiskWalletConfig);

        WithdrawalRequest withdrawal = saveHighRiskWithdrawal(10002L, "addr-score");

        //when
        RiskEvaluationResult result = riskEvaluationService.evaluationWithdrawal(withdrawal);

        //then
        assertThat(result.ruleHits())
                .filteredOn(hit -> hit.ruleCode().equals(RiskRuleCodes.HIGH_RISK_WALLET))
                .extracting(RuleHit::score)
                .containsExactly(150);

        assertThat(result.totalScore()).isGreaterThanOrEqualTo(150);
    }

    @Test
    @DisplayName("DB 설정의 blocking=false가 decision에 반영된다")
    void case3() {
        //given
        RiskRuleConfig highRiskWalletConfig = riskRuleConfigRepository
                .findByRuleCode(RiskRuleCodes.HIGH_RISK_WALLET)
                .orElseThrow();
        highRiskWalletConfig.update(true, 100, false, null, "non blocking");
        riskRuleConfigRepository.saveAndFlush(highRiskWalletConfig);

        WithdrawalRequest withdrawal = saveHighRiskWithdrawal(10003L, "addr-non-blocking");

        //when
        RiskEvaluationResult result = riskEvaluationService.evaluationWithdrawal(withdrawal);

        //then
        assertThat(result.ruleHits())
                .filteredOn(hit -> hit.ruleCode().equals(RiskRuleCodes.HIGH_RISK_WALLET))
                .extracting(RuleHit::blocking)
                .containsExactly(false);

        assertThat(result.decision()).isNotEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);
    }

    private WithdrawalRequest saveHighRiskWithdrawal(Long userId, String address) {
        walletAddressRiskRepository.save(new WalletAddressRisk(
                "TRON",
                address,
                WalletRiskLevel.HIGH,
                90,
                KytRiskCategory.SANCTIONED_ADDRESS,
                "MOCK_KYT"
        ));

        return withdrawalRequestRepository.save(new WithdrawalRequest(
                userId,
                "USDT",
                "TRON",
                address,
                new BigDecimal("10000.000000000000000000")
        ));
    }
}
