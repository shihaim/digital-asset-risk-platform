package com.example.digital_asset_risk_platform.risk.config.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record RiskRuleConfigUpdateRequest(
        Boolean enabled,

        @Min(value = 0, message = "score 0 이상이어야 합니다.")
        Integer score,

        Boolean blocking,

        @Size(max = 100, message = "thresholdValue는 최대 100자까지 입력할 수 있습니다.")
        String thresholdValue,

        @Size(max = 1000, message = "description은 최대 1000자까지 입력할 수 있습니다.")
        String description
) {
}
