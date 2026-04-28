package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

public record RiskCaseDetailResponse(
        Long caseId,
        RiskCaseType caseType,
        RiskCaseStatus status,
        RiskLevel riskLevel,
        String assignedTo,
        String reviewResult,
        String reviewComment,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        WithdrawalInfoResponse withdrawal,
        RiskEvaluationInfoResponse evaluation,
        List<RuleHitResponse> ruleHits,
        List<RiskTimelineEventResponse> timeline
) {
    public static RiskCaseDetailResponse of(
            RiskCase riskCase,
            WithdrawalInfoResponse withdrawal,
            RiskEvaluationInfoResponse evaluation,
            List<RuleHitResponse> ruleHits,
            List<RiskTimelineEventResponse> timeline
    ) {
        return new RiskCaseDetailResponse(
                riskCase.getId(),
                riskCase.getCaseType(),
                riskCase.getStatus(),
                riskCase.getRiskLevel(),
                riskCase.getAssignedTo(),
                riskCase.getReviewResult(),
                riskCase.getReviewComment(),
                riskCase.getCreatedAt(),
                riskCase.getClosedAt(),
                withdrawal,
                evaluation,
                ruleHits,
                timeline
        );
    }
}
