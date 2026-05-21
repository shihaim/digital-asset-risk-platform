package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FrequentWithdrawalRule implements RiskRule {

    private final RiskRuleConfigService riskRuleConfigService;

    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        RiskRuleConfig config = riskRuleConfigService.getConfig(RiskRuleCodes.FREQUENT_WITHDRAWAL_24H);

        if (!config.isEnabled()) {
            return Optional.empty();
        }

        int threshold = config.getThresholdAsInt(5);
        if (context.withdrawalCountLast24h() < threshold) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                config.getRuleCode(),
                config.getRuleName(),
                config.getScore(),
                "최근 24시간 내 출금 요청 횟수가 5회 이상",
                config.isBlocking()
        ));
    }
}
