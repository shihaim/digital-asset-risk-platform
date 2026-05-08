package com.example.digital_asset_risk_platform.wallet.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;

public record WithdrawalCreateResponse(
        Long withdrawalId,
        WithdrawalStatus status,
        RiskLevel riskLevel,
        RiskDecisionType decision,
        Integer totalScore,
        Long caseId
) {

    public static WithdrawalCreateResponse evaluating(Long withdrawalId, WithdrawalStatus status) {
        return new WithdrawalCreateResponse(
                withdrawalId,
                status,
                null,
                null,
                null,
                null
        );
    }

    public static WithdrawalCreateResponse evaluated(Long withdrawalId, WithdrawalStatus status, RiskLevel riskLevel, RiskDecisionType decision, int totalScore, Long caseId) {
        return new WithdrawalCreateResponse(withdrawalId, status, riskLevel, decision, totalScore, caseId);
    }
}
