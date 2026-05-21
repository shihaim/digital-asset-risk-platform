package com.example.digital_asset_risk_platform.risk.config.dto;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;

import java.time.LocalDateTime;

public record RiskRuleConfigResponse(
        String ruleCode,
        String ruleName,
        boolean enabled,
        int score,
        boolean blocking,
        String thresholdValue,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RiskRuleConfigResponse from(RiskRuleConfig config) {
        return new RiskRuleConfigResponse(
                config.getRuleCode(),
                config.getRuleName(),
                config.isEnabled(),
                config.getScore(),
                config.isBlocking(),
                config.getThresholdValue(),
                config.getDescription(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}
