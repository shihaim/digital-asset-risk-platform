package com.example.digital_asset_risk_platform.risk.config.domain;

public record RiskRuleConfigSnapshot(
        String ruleCode,
        String ruleName,
        String enabledYn,
        int score,
        String blockingYn,
        String thresholdValue,
        String description
) {

    public static RiskRuleConfigSnapshot from(RiskRuleConfig config) {
        return new RiskRuleConfigSnapshot(
                config.getRuleCode(),
                config.getRuleName(),
                config.getEnabledYn(),
                config.getScore(),
                config.getBlockingYn(),
                config.getThresholdValue(),
                config.getDescription()
        );
    }

    public boolean enabled() {
        return "Y".equals(enabledYn);
    }

    public boolean blocking() {
        return "Y".equals(blockingYn);
    }
}
