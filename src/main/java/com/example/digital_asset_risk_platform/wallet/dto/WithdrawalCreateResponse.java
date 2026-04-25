package com.example.digital_asset_risk_platform.wallet.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;

public record WithdrawalCreateResponse(
        Long withdrawalId,
        WithdrawalStatus status,
        RiskLevel riskLevel,
        RiskDecisionType decision,
        int totalScore,
        Long caseId
) {
}
