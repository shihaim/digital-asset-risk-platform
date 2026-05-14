package com.example.digital_asset_risk_platform.outbox.dto;

public record OutboxEventSummaryResponse(
        long pendingCount,
        long processingCount,
        long sentCount,
        long failedCount,
        long deadCount
) {
}
