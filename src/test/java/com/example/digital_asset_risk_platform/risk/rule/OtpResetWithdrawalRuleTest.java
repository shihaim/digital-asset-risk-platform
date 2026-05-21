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

public class OtpResetWithdrawalRuleTest {

    private final RiskRuleConfigService riskRuleConfigService = mock(RiskRuleConfigService.class);
    private final RiskRule rule = new OtpResetWithdrawalRule(riskRuleConfigService);

    @Test
    @DisplayName("OTP 재설정 시간이 threshold 안이면 Rule이 적중한다")
    void case1() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.OTP_RESET_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                        "OTP 재설정 후 출금",
                        true,
                        65,
                        true,
                        "24h",
                        "OTP 재설정"
                ));

        RiskContext base = RiskContextFixture.builder().build();
        RiskContext context = RiskContextFixture.builder()
                .latestOtpResetAt(base.withdrawal().getRequestedAt().minusHours(23))
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        assertThat(result).isPresent();
        RuleHit hit = result.get();

        assertThat(hit.ruleCode()).isEqualTo(RiskRuleCodes.OTP_RESET_WITHDRAWAL);
        assertThat(hit.ruleName()).isEqualTo("OTP 재설정 후 출금");
        assertThat(hit.score()).isEqualTo(65);
        assertThat(hit.blocking()).isTrue();
    }

    @Test
    @DisplayName("OTP 재설정 시간이 threshold 밖이면 Rule이 적중하지 않는다")
    void case2() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.OTP_RESET_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                        "OTP 재설정 후 출금",
                        true,
                        40,
                        false,
                        "24h",
                        "OTP 재설정"
                ));

        RiskContext base = RiskContextFixture.builder().build();
        RiskContext context = RiskContextFixture.builder()
                .latestOtpResetAt(base.withdrawal().getRequestedAt().minusHours(25))
                .build();

        //when
        Optional<RuleHit> result = rule.evaluate(context);

        //then
        assertThat(result).isEmpty();
    }
}