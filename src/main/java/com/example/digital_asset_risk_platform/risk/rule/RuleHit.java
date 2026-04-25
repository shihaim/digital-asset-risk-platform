package com.example.digital_asset_risk_platform.risk.rule;

public record RuleHit(
        String ruleCode,
        String ruleName,
        int score,
        String reason,
        boolean blocking
) {
}
