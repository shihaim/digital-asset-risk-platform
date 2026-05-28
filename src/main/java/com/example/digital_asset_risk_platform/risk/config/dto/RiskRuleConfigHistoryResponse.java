package com.example.digital_asset_risk_platform.risk.config.dto;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfigHistory;

import java.time.LocalDateTime;

public record RiskRuleConfigHistoryResponse(
        Long id,
        String ruleCode,
        String ruleName,

        boolean beforeEnabled,
        boolean afterEnabled,

        int beforeScore,
        int afterScore,

        boolean beforeBlocking,
        boolean afterBlocking,

        String beforeThresholdValue,
        String afterThresholdValue,

        String beforeDescription,
        String afterDescription,

        String changedBy,
        String changeReason,
        LocalDateTime changedAt
) {

    public static RiskRuleConfigHistoryResponse from(RiskRuleConfigHistory history) {
        return new RiskRuleConfigHistoryResponse(
                history.getId(),
                history.getRuleCode(),
                history.getRuleName(),

                history.isBeforeEnabled(),
                history.isAfterEnabled(),

                history.getBeforeScore(),
                history.getAfterScore(),

                history.isBeforeBlocking(),
                history.isAfterBlocking(),

                history.getBeforeThresholdValue(),
                history.getAfterThresholdValue(),

                history.getBeforeDescription(),
                history.getAfterDescription(),

                history.getChangedBy(),
                history.getChangeReason(),
                history.getChangedAt()
        );
    }
}
