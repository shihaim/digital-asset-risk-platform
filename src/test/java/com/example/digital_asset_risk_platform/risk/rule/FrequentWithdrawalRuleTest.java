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

public class FrequentWithdrawalRuleTest {

    private final RiskRuleConfigService riskRuleConfigService = mock(RiskRuleConfigService.class);
    private final RiskRule rule = new FrequentWithdrawalRule(riskRuleConfigService);

    @Test
    @DisplayName("최근 24시간 출금 횟수가 configured threshold 이상이면 Rule이 적중한다")
    void case1() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.FREQUENT_WITHDRAWAL_24H))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.FREQUENT_WITHDRAWAL_24H,
                        "24시간 내 반복 출금",
                        true,
                        30,
                        false,
                        "3",
                        "반복 출금"
                ));

        RiskContext context = RiskContextFixture.builder()
                .withdrawalCountLast24h(3)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isPresent();
        RuleHit hit = result.get();

        assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.FREQUENT_WITHDRAWAL_24H);
        assertThat(hit.ruleName()).isEqualTo("24시간 내 반복 출금");
        assertThat(hit.score()).isEqualTo(30);
        assertThat(hit.blocking()).isFalse();
    }

    @Test
    @DisplayName("최근 24시간 출금 횟수가 configured threshold 미만이면 Rule이 적중하지 않는다")
    void case2() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.FREQUENT_WITHDRAWAL_24H))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.FREQUENT_WITHDRAWAL_24H,
                        "24시간 내 반복 출금",
                        true,
                        30,
                        false,
                        "3",
                        "반복 출금"
                ));

        RiskContext context = RiskContextFixture.builder()
                .withdrawalCountLast24h(2)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }
}