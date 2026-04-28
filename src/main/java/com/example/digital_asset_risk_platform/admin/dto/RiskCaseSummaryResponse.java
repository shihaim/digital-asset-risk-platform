package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;

import java.time.LocalDateTime;

public record RiskCaseSummaryResponse(
        Long caseId,
        Long evaluationId,
        Long userId,
        RiskCaseType caseType,
        RiskCaseStatus status,
        RiskLevel riskLevel,
        String assignedTo,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {
    public static RiskCaseSummaryResponse from(RiskCase riskCase) {
        return new RiskCaseSummaryResponse(
                riskCase.getId(),
                riskCase.getEvaluationId(),
                riskCase.getUserId(),
                riskCase.getCaseType(),
                riskCase.getStatus(),
                riskCase.getRiskLevel(),
                riskCase.getAssignedTo(),
                riskCase.getCreatedAt(),
                riskCase.getClosedAt()
        );
    }
}
