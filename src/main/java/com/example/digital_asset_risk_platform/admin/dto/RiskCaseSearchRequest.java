package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;

public record RiskCaseSearchRequest(
        RiskCaseStatus status
) {
}
