package com.example.digital_asset_risk_platform.risk.decision;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;

import java.util.List;

public class DecisionEngine {
    public RiskDecision decide(List<RuleHit> hits) {

        int totalScore = hits.stream().mapToInt(RuleHit::score).sum();

        boolean hasBlockingRule = hits.stream().anyMatch(RuleHit::blocking);

        if (hasBlockingRule) {
            return new RiskDecision(
                    RiskLevel.CRITICAL,
                    RiskDecisionType.BLOCK_WITHDRAWAL,
                    totalScore
            );
        }

        if (totalScore >= 120) {
            return new RiskDecision(
                    RiskLevel.CRITICAL,
                    RiskDecisionType.HOLD_WITHDRAWAL,
                    totalScore
            );
        }

        if (totalScore >= 80) {
            return new RiskDecision(
                    RiskLevel.HIGH,
                    RiskDecisionType.HOLD_WITHDRAWAL,
                    totalScore
            );
        }

        if (totalScore >= 60) {
            return new RiskDecision(
                    RiskLevel.CAUTION,
                    RiskDecisionType.REQUIRE_ADDITIONAL_AUTH,
                    totalScore
            );
        }

        if (totalScore >= 30) {
            return new RiskDecision(
                    RiskLevel.WATCH,
                    RiskDecisionType.MONITOR,
                    totalScore
            );
        }

        return new RiskDecision(
                RiskLevel.NORMAL,
                RiskDecisionType.ALLOW,
                totalScore
        );
    }
}
