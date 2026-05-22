package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpResetWithdrawalRule implements RiskRule {

    private final RiskRuleConfigService riskRuleConfigService;

    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        RiskRuleConfig config = riskRuleConfigService.getConfig(RiskRuleCodes.OTP_RESET_WITHDRAWAL);

        if (!config.isEnabled()) {
            return Optional.empty();
        }

        Duration threshold = config.getThresholdAsDuration(Duration.ofHours(24));

        if (!context.accountRisk().hasOtpResetWithin(context.withdrawal().getRequestedAt(), threshold)) {
            return Optional.empty();
        }

        String reason = config.getDescription();
        if (reason == null || reason.isBlank()) {
            reason = "최근 24시간 이내 OTP 재설정 후 출금 요청";
        }

        return Optional.of(new RuleHit(
                config.getRuleCode(),
                config.getRuleName(),
                config.getScore(),
                reason,
                config.isBlocking()
        ));
    }
}
