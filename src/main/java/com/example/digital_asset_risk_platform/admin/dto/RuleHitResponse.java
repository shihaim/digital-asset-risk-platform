package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;

public record RuleHitResponse(
        String ruleCode,
        String ruleName,
        int score,
        String reason,
        boolean blocking
) {
    public static RuleHitResponse from(RiskRuleHit hit) {
        return new RuleHitResponse(
                hit.getRuleCode(),
                hit.getRuleName(),
                hit.getScore(),
                hit.getReason(),
                "Y".equals(hit.getBlockingYn())
        );
    }
}
