package com.example.digital_asset_risk_platform.risk.simulation.application;

import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.risk.simulation.dto.RiskRuleSimulationRequest;
import com.example.digital_asset_risk_platform.risk.simulation.dto.RiskRuleSimulationResponse;
import com.example.digital_asset_risk_platform.risk.support.RiskRuleConfigFixture;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RiskRuleSimulationServiceTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleSimulationService riskRuleSimulationService;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    RiskRuleConfigRepository riskRuleConfigRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @BeforeEach
    void setUp() {
        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
        accountLoginEventRepository.deleteAll();
        accountSecurityEventRepository.deleteAll();

        RiskRuleConfigFixture.resetDefaultConfigs(riskRuleConfigRepository);
    }

    @Test
    @DisplayName("Rule 시뮬레이션은 평가 결과를 반환한다")
    void case1() {
        //given
        RiskRuleSimulationRequest request = new RiskRuleSimulationRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        );

        //when
        RiskRuleSimulationResponse response = riskRuleSimulationService.simulate(request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.totalScore()).isGreaterThanOrEqualTo(0);
        assertThat(response.riskLevel()).isNotBlank();
        assertThat(response.decision()).isNotBlank();
        assertThat(response.ruleHits()).isNotNull();
    }

    @Test
    @DisplayName("Rule 시뮬레이션은 WithdrawalRequest를 저장하지 않는다")
    void case2() {
        //given
        long beforeCount = withdrawalRequestRepository.count();

        RiskRuleSimulationRequest request = new RiskRuleSimulationRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        );

        //when
        RiskRuleSimulationResponse response = riskRuleSimulationService.simulate(request);

        //then
        long afterCount = withdrawalRequestRepository.count();

        assertThat(afterCount).isEqualTo(beforeCount);
    }

    @Test
    @DisplayName("고액 출금 조건으로 시뮬레이션하면 고액 출금 RuleHit이 발생한다")
    void case3() {
        //given
        WithdrawalRequest pastWithdrawal = new WithdrawalRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        );

        withdrawalRequestRepository.save(pastWithdrawal);

        RiskRuleSimulationRequest request = new RiskRuleSimulationRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100000000")
        );

        //when
        RiskRuleSimulationResponse response = riskRuleSimulationService.simulate(request);

        //then
        assertThat(response.ruleHits())
                .anySatisfy(hit -> {
                    assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL);
                });
    }

    @Test
    @DisplayName("고위험 지갑 주소로 시뮬레이션하면 고위험 지갑 RuleHit이 발생한다")
    void case4() {
        //given
        RiskRuleSimulationRequest request = new RiskRuleSimulationRequest(
                10001L,
                "USDT",
                "TRON",
                "THACKED000001",
                new BigDecimal("100.000000000000000000")
        );

        //when
        RiskRuleSimulationResponse response = riskRuleSimulationService.simulate(request);

        //then
        assertThat(response.ruleHits())
                .anySatisfy(hit -> {
                    assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
                });
    }

    @Test
    @DisplayName("정상 조건으로 시뮬레이션하면 낮은 위험도로 평가된다")
    void case5() {
        //given
        RiskRuleSimulationRequest request = new RiskRuleSimulationRequest(
                10001L,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        );

        //when
        RiskRuleSimulationResponse response = riskRuleSimulationService.simulate(request);

        //then
        assertThat(response.totalScore()).isGreaterThanOrEqualTo(0);
        assertThat(response.decision()).isIn(RiskDecisionType.ALLOW.name(), RiskDecisionType.MONITOR.name());
    }
}
