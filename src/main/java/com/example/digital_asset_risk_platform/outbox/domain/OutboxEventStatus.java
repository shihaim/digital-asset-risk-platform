package com.example.digital_asset_risk_platform.outbox.domain;

public enum OutboxEventStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED
}
