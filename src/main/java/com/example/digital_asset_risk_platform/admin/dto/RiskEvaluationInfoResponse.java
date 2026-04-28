package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;

import java.time.LocalDateTime;

public record RiskEvaluationInfoResponse(
        Long evaluationId,
        String refType,
        Long refId,
        Long userId,
        int totalScore,
        RiskLevel riskLevel,
        RiskDecisionType decision,
        LocalDateTime evaluatedAt
) {
    public static RiskEvaluationInfoResponse from(RiskEvaluation evaluation) {
        return new RiskEvaluationInfoResponse(
                evaluation.getId(),
                evaluation.getRefType(),
                evaluation.getRefId(),
                evaluation.getUserId(),
                evaluation.getTotalScore(),
                evaluation.getRiskLevel(),
                evaluation.getDecision(),
                evaluation.getEvaluatedAt()
        );
    }
}
