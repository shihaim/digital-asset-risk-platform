package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class NewDeviceWithdrawalRuleTest {

    private final RiskRule rule = new NewDeviceWithdrawalRule();

    @Test
    @DisplayName("최근 1시간 이내 신규 기기 로그인이 있으면 룰이 적중한다")
    void case1() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .newDeviceLoginWithin1h(true)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("NEW_DEVICE_WITHDRAWAL");
        Assertions.assertThat(hit.ruleName()).isEqualTo("신규 기기 로그인 후 출금");
        Assertions.assertThat(hit.score()).isEqualTo(30);
        Assertions.assertThat(hit.blocking()).isFalse();
        Assertions.assertThat(hit.reason()).contains("신규 기기");
    }

    @Test
    @DisplayName("최근 1시간 이내 신규 기기 로그인이 없으면 룰이 적중하지 않는다")
    void case2() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .newDeviceLoginWithin1h(false)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }

}