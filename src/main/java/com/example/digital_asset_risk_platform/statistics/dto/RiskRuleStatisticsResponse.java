package com.example.digital_asset_risk_platform.statistics.dto;

import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;

import java.time.LocalDateTime;

public record RiskRuleStatisticsResponse(
        String ruleCode,
        Long hitCount,
        LocalDateTime lastHitAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RiskRuleStatisticsResponse from(RiskRuleStatistics statistics) {
        return new RiskRuleStatisticsResponse(
                statistics.getRuleCode(),
                statistics.getHitCount(),
                statistics.getLastHitAt(),
                statistics.getCreatedAt(),
                statistics.getUpdatedAt()
        );
    }
}
