package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;

import java.util.List;

public record RiskEvaluationResult(
        Long evaluationId,
        RiskLevel riskLevel,
        RiskDecisionType decision,
        int totalScore,
        List<RuleHit> ruleHits
) {
}
