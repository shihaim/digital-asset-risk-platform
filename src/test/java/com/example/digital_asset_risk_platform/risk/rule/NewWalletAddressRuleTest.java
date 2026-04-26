package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class NewWalletAddressRuleTest {

    private final RiskRule rule = new NewWalletAddressRule();

    @Test
    @DisplayName("사용자가 과거에 출금한 적 없는 신규 지갑 주소면 룰이 적중한다")
    void case1() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .newWalletAddress(true)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isPresent();

        RuleHit hit = result.get();
        Assertions.assertThat(hit.ruleCode()).isEqualTo("NEW_WALLET_ADDRESS");
        Assertions.assertThat(hit.ruleName()).isEqualTo("신규 지갑 주소 출금");
        Assertions.assertThat(hit.score()).isEqualTo(20);
        Assertions.assertThat(hit.blocking()).isFalse();
        Assertions.assertThat(hit.reason()).contains("신규 지갑");
    }

    @Test
    @DisplayName("사용자가 과거에 출금한 적 있는 지갑 주소면 룰이 적중하지 않는다")
    void case2() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .newWalletAddress(false)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        Assertions.assertThat(result).isEmpty();
    }
}