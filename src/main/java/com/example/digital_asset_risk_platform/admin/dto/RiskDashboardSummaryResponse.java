package com.example.digital_asset_risk_platform.admin.dto;

public record RiskDashboardSummaryResponse(
        long reviewRequiredCaseCount,
        long inReviewCaseCount,
        long approvedCaseCount,
        long rejectedCaseCount,
        long falsePositiveCaseCount,
        long truePositiveCaseCount,
        long heldWithdrawalCount,
        long blockedWithdrawalCount
) {
}
