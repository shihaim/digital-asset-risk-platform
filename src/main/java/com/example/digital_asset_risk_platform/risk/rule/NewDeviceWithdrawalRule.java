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
public class NewDeviceWithdrawalRule implements RiskRule {

    private final RiskRuleConfigService riskRuleConfigService;

    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        RiskRuleConfig config = riskRuleConfigService.getConfig(RiskRuleCodes.NEW_DEVICE_WITHDRAWAL);

        if (!config.isEnabled()) {
            return Optional.empty();
        }

        Duration threshold = config.getThresholdAsDuration(Duration.ofMinutes(60));
        if (!context.accountRisk().hasNewDeviceLoginWithin(context.withdrawal().getRequestedAt(), threshold)) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
               config.getRuleCode(),
                config.getRuleName(),
                config.getScore(),
                "최근 1시간 이내 신규 기기 로그인 후 출금 요청",
                config.isBlocking()
        ));
    }
}
