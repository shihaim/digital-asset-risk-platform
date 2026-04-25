package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class HighRiskWalletRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        if (!context.walletRisk().isHighRisk()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "HIGH_RISK_WALLET",
                "고위험 지갑 주소 출금",
                100,
                "출금 주소가 고위험 지갑 주소로 분류됨: " + context.walletRisk().riskCategory(),
                true
        ));
    }
}
