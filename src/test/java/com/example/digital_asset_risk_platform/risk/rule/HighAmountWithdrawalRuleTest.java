package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class HighAmountWithdrawalRuleTest {

    private final RiskRule rule = new HighAmountWithdrawalRule();

    @Test
    @DisplayName("이번 출금액이 사용자 평균 출금액의 10배 이상이면 룰이 적중한다")
    void case1() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .amount("10000")
                .averageWithdrawalAmount("1000")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("HIGH_AMOUNT_WITHDRAWAL");
        Assertions.assertThat(hit.ruleName()).isEqualTo("평균 대비 고액 출금");
        Assertions.assertThat(hit.score()).isEqualTo(40);
        Assertions.assertThat(hit.blocking()).isFalse();
        Assertions.assertThat(hit.reason()).contains("10배");
    }

    @Test
    @DisplayName("이번 출금액이 사용자 평균 출금액의 10배 미만이면 룰이 적중하지 않는다")
    void case2() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .amount("9999")
                .averageWithdrawalAmount("1000")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 평균 출금액이 0이면 고액 출금 룰이 적중하지 않는다")
    void case3() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .amount("10000")
                .averageWithdrawalAmount("0")
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }
}