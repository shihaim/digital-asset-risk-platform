package com.example.digital_asset_risk_platform.risk.profile.dto;

import com.example.digital_asset_risk_platform.risk.profile.domain.UserRiskProfile;

import java.time.LocalDateTime;

public record UserRiskProfileResponse(
        Long userId,
        int currentRiskScore,
        String riskLevel,
        int totalCaseCount,
        int totalBlockedWithdrawalCount,
        LocalDateTime lastEvaluatedAt
) {
    public static UserRiskProfileResponse from(UserRiskProfile profile) {
        return new UserRiskProfileResponse(
                profile.getUserId(),
                profile.getCurrentRiskScore(),
                profile.getRiskLevel().name(),
                profile.getTotalCaseCount(),
                profile.getTotalBlockedWithdrawalCount(),
                profile.getLastEvaluatedAt()
        );
    }
}
