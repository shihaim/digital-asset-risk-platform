package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HighRiskWalletRule implements RiskRule {

    private final RiskRuleConfigService riskRuleConfigService;

    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        RiskRuleConfig config = riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_RISK_WALLET);

        if (!config.isEnabled()) {
            return Optional.empty();
        }

        if (!context.walletRisk().isHighRisk()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                config.getRuleCode(),
                config.getRuleName(),
                config.getScore(),
                "출금 주소가 고위험 지갑 주소로 분류됨: " + context.walletRisk().riskCategory(),
                config.isBlocking()
        ));
    }
}
