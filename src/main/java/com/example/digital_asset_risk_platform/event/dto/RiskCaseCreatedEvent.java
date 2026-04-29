package com.example.digital_asset_risk_platform.event.dto;

import java.time.LocalDateTime;

public record RiskCaseCreatedEvent(
        String eventId,
        Long caseId,
        Long evaluationId,
        Long userId,
        String caseType,
        String status,
        String riskLevel,
        LocalDateTime createdAt,
        LocalDateTime occurredAt
) {
}
