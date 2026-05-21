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

public class NewDeviceWithdrawalRuleTest {

    private final RiskRuleConfigService riskRuleConfigService = mock(RiskRuleConfigService.class);
    private final RiskRule rule = new NewDeviceWithdrawalRule(riskRuleConfigService);

    @Test
    @DisplayName("신규 기기 로그인 시간이 threshold 안이면 Rule이 적중한다")
    void case1() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.NEW_DEVICE_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                        "신규 기기 로그인 후 출금",
                        true,
                        55,
                        true,
                        "60m",
                        "신규 기기"
                ));

        RiskContext context = RiskContextFixture.builder()
                .newDeviceLoginWithin1h(true)
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isPresent();
        RuleHit hit = result.get();

        assertThat(hit.score()).isEqualTo(55);
        assertThat(hit.blocking()).isTrue();
    }

    @Test
    @DisplayName("신규 기기 로그인 시간이 threshold 밖이면 Rule이 적중하지 않는다")
    void case2() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.NEW_DEVICE_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                        "신규 기기 로그인 후 출금",
                        true,
                        30,
                        false,
                        "60m",
                        "신규 기기"
                ));

        RiskContext base = RiskContextFixture.builder().build();
        RiskContext context = RiskContextFixture.builder()
                .latestNewDeviceLoginAt(base.withdrawal().getRequestedAt().minusMinutes(90))
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }

}