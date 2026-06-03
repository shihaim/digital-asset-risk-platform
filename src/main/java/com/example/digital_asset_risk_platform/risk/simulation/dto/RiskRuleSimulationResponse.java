package com.example.digital_asset_risk_platform.risk.simulation.dto;

import com.example.digital_asset_risk_platform.risk.decision.RiskDecision;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;

import java.util.List;

public record RiskRuleSimulationResponse(
        int totalScore,
        String riskLevel,
        String decision,
        List<RiskRuleSimulationHitResponse> ruleHits
) {

    public static RiskRuleSimulationResponse of(RiskDecision decision, List<RuleHit> hits) {
        return new RiskRuleSimulationResponse(
                decision.totalScore(),
                decision.riskLevel().name(),
                decision.decisionType().name(),
                hits.stream()
                        .map(RiskRuleSimulationHitResponse::from)
                        .toList()
        );
    }
}
