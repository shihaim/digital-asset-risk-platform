package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HighAmountWithdrawalRuleTest {

    private final RiskRuleConfigService riskRuleConfigService = mock(RiskRuleConfigService.class);
    private final RiskRule rule = new HighAmountWithdrawalRule(riskRuleConfigService);

    @Test
    @DisplayName("출금액이 평균 금액의 configured multiplier 이상이면 Rule이 적중한다")
    void case1() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "평균 대비 고액 출금",
                        true,
                        70,
                        false,
                        "5x",
                        "평균 대비 5배"
                ));

        RiskContext context = RiskContextFixture.builder()
                .averageWithdrawalAmount("100")
                .amount("500")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isPresent();
        RuleHit hit = result.get();

        assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL);
        assertThat(hit.ruleName()).isEqualTo("평균 대비 고액 출금");
        assertThat(hit.score()).isEqualTo(70);
        assertThat(hit.blocking()).isFalse();
        assertThat(hit.reason()).contains("5배");
    }

    @Test
    @DisplayName("출금액이 평균 금액의 configured multiplier 미만이면 Rule이 적중하지 않는다")
    void case2() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "평균 대비 고액 출금",
                        true,
                        90,
                        false,
                        "10x",
                        "평균 대비 10배"
                ));

        RiskContext context = RiskContextFixture.builder()
                .averageWithdrawalAmount("1000")
                .amount("9999.9999")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("출금액이 고정 threshold 이상이면 Rule이 적중한다")
    void case3() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "고액 출금",
                        true,
                        40,
                        false,
                        "10000",
                        "고정 금액 기준"
                ));

        RiskContext context = RiskContextFixture.builder()
                .averageWithdrawalAmount("100")
                .amount("10000")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isPresent();
        RuleHit hit = result.get();

        assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL);
        assertThat(hit.ruleName()).isEqualTo("고액 출금");
        assertThat(hit.score()).isEqualTo(40);
        assertThat(hit.blocking()).isFalse();
        assertThat(hit.reason()).contains("고정");
    }

    @Test
    @DisplayName("출금액이 고정 threshold 미만이면 Rule이 적중하지 않는다")
    void case4() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "고액 출금",
                        true,
                        40,
                        false,
                        "10000",
                        "고정 금액 기준"
                ));

        RiskContext context = RiskContextFixture.builder()
                .averageWithdrawalAmount("1")
                .amount("9999")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 평균 출금액이 0이면 고액 출금 룰이 적중하지 않는다")
    void case5() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "평균 대비 고액 출금",
                        true,
                        70,
                        false,
                        "5x",
                        "평균 대비 5배"
                ));

        RiskContext context = RiskContextFixture.builder()
                .amount("10000")
                .averageWithdrawalAmount("0")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }
}