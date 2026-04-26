package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class HighRiskWalletRuleTest {

    private final RiskRule rule = new HighRiskWalletRule();

    @Test
    @DisplayName("지갑 위험도가 HIGH이면 고위험 지갑 룰이 적중한다")
    void case1() {
        /**
         * riskScore와 riskCategory에 대한 정의가 명확하지 않음.
         */
        //given
        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.HIGH, 100, "HACKED_FUNDS")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("HIGH_RISK_WALLET");
        Assertions.assertThat(hit.ruleName()).isEqualTo("고위험 지갑 주소 출금");
        Assertions.assertThat(hit.score()).isEqualTo(100);
        Assertions.assertThat(hit.blocking()).isTrue();
        Assertions.assertThat(hit.reason()).contains("HACKED_FUNDS");
    }

    @Test
    @DisplayName("지갑 위험도가 CRITICAL이면 고위험 지갑 룰이 적중한다")
    void case2() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.CRITICAL, 100, "SANCTIONED_ADDRESS")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("HIGH_RISK_WALLET");
        Assertions.assertThat(hit.ruleName()).isEqualTo("고위험 지갑 주소 출금");
        Assertions.assertThat(hit.score()).isEqualTo(100);
        Assertions.assertThat(hit.blocking()).isTrue();
        Assertions.assertThat(hit.reason()).contains("SANCTIONED_ADDRESS");
    }

    @Test
    @DisplayName("지갑 위험도가 LOW이면 고위험 지갑 룰이 적중하지 않는다")
    void case3() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.LOW, 0, "NORMAL")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("지갑 위험도가 MEDIUM이면 고위험 지갑 룰이 적중하지 않는다")
    void case4() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.MEDIUM, 30, "SUSPICIOUS")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }
}