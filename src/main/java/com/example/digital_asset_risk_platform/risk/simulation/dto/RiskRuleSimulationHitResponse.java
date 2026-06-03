package com.example.digital_asset_risk_platform.risk.simulation.dto;

import com.example.digital_asset_risk_platform.risk.rule.RuleHit;

public record RiskRuleSimulationHitResponse(
        String ruleCode,
        String ruleName,
        int score,
        String reason,
        boolean blocking
) {

    public static RiskRuleSimulationHitResponse from(RuleHit hit) {
        return new RiskRuleSimulationHitResponse(
                hit.ruleCode(),
                hit.ruleName(),
                hit.score(),
                hit.reason(),
                hit.blocking()
        );
    }
}
