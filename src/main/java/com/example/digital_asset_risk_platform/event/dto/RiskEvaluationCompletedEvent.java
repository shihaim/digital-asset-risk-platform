package com.example.digital_asset_risk_platform.event.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RiskEvaluationCompletedEvent(
        String eventId,
        Long evaluationId,
        String refType,
        Long refId,
        Long userId,
        int totalScore,
        String riskLevel,
        String decision,
        List<String> ruleCodes,
        LocalDateTime evaluatedAt,
        LocalDateTime occurredAt
) {
}
