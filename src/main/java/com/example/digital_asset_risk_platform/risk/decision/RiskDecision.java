package com.example.digital_asset_risk_platform.risk.decision;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;

public record RiskDecision(
        RiskLevel riskLevel,
        RiskDecisionType decisionType,
        int totalScore
) {
}
