package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class FrequentWithdrawalRuleTest {

    private final RiskRule rule = new FrequentWithdrawalRule();

    @Test
    @DisplayName("최근 24시간 내 출금 요청이 5회 이상이면 룰이 적중한다")
    void case1() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .withdrawalCountLast24h(5)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("FREQUENT_WITHDRAWAL_24H");
        Assertions.assertThat(hit.ruleName()).isEqualTo("24시간 내 반복 출금");
        Assertions.assertThat(hit.score()).isEqualTo(30);
        Assertions.assertThat(hit.blocking()).isFalse();
        Assertions.assertThat(hit.reason()).contains("5회");
    }

    @Test
    @DisplayName("최근 24시간 내 출금 요청이 5회 미만이면 룰이 적중하지 않는다")
    void case2() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .withdrawalCountLast24h(4)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }
}