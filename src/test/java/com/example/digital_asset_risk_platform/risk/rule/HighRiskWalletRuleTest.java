package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HighRiskWalletRuleTest {

    private final RiskRuleConfigService riskRuleConfigService = mock(RiskRuleConfigService.class);
    private final RiskRule rule = new HighRiskWalletRule(riskRuleConfigService);

    @Test
    @DisplayName("지갑 위험도가 HIGH이면 고위험 지갑 룰이 적중한다")
    void case1() {
        //given
        highRiskWallet();

        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.HIGH, 100, KytRiskCategory.HACKED_FUNDS)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isPresent();
        RuleHit hit = result.get();

        assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
        assertThat(hit.ruleName()).isEqualTo("고위험 지갑 주소 출금");
        assertThat(hit.score()).isEqualTo(100);
        assertThat(hit.blocking()).isTrue();
        assertThat(hit.reason()).contains("고위험 지갑");
    }

    @Test
    @DisplayName("지갑 위험도가 CRITICAL이면 고위험 지갑 룰이 적중한다")
    void case2() {
        //given
        highRiskWallet();

        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.CRITICAL, 100, KytRiskCategory.SANCTIONED_ADDRESS)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isPresent();

        RuleHit hit = result.get();
        assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
        assertThat(hit.ruleName()).isEqualTo("고위험 지갑 주소 출금");
        assertThat(hit.score()).isEqualTo(100);
        assertThat(hit.blocking()).isTrue();
        assertThat(hit.reason()).contains("고위험 지갑");
    }

    @Test
    @DisplayName("지갑 위험도가 LOW이면 고위험 지갑 룰이 적중하지 않는다")
    void case3() {
        highRiskWallet();

        //given
        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.LOW, 0, KytRiskCategory.NORMAL)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("지갑 위험도가 MEDIUM이면 고위험 지갑 룰이 적중하지 않는다")
    void case4() {
        //given
        highRiskWallet();

        RiskContext context = RiskContextFixture.builder()
                .walletRisk(WalletRiskLevel.MEDIUM, 30, KytRiskCategory.SCAM)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }

    private void highRiskWallet() {
        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_RISK_WALLET))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_RISK_WALLET,
                        "고위험 지갑 주소 출금",
                        true,
                        100,
                        true,
                        null,
                        "고위험 지갑"
                ));
    }
}
