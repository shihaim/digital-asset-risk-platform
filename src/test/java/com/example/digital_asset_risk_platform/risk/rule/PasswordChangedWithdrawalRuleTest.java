package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class PasswordChangedWithdrawalRuleTest {

    private final RiskRule rule = new PasswordChangedWithdrawalRule();

    @Test
    @DisplayName("최근 24시간 이내 비밀번호 변경 이벤트가 있으면 룰이 적중한다")
    void case1() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .passwordChangedWithin24h(true)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("PASSWORD_CHANGED_WITHDRAWAL");
        Assertions.assertThat(hit.ruleName()).isEqualTo("비밀번호 변경 후 출금");
        Assertions.assertThat(hit.score()).isEqualTo(30);
        Assertions.assertThat(hit.blocking()).isFalse();
        Assertions.assertThat(hit.reason()).contains("비밀번호");
    }

    @Test
    @DisplayName("최근 24시간 이내 비밀번호 변경 이벤트가 없으면 룰이 적중하지 않는다")
    void case2() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .passwordChangedWithin24h(false)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }
}