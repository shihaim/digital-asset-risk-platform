package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NewWalletAddressRule implements RiskRule {

    private final RiskRuleConfigService riskRuleConfigService;

    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        RiskRuleConfig config = riskRuleConfigService.getConfig(RiskRuleCodes.NEW_WALLET_ADDRESS);

        if (!config.isEnabled()) {
            return Optional.empty();
        }

        if (!context.isNewWalletAddress()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                config.getRuleCode(),
                config.getRuleName(),
                config.getScore(),
                "사용자가 과거에 출금한 적 없는 신규 지갑 주소",
                config.isBlocking()
        ));
    }
}
