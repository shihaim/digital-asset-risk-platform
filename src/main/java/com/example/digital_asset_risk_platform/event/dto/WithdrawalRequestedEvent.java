package com.example.digital_asset_risk_platform.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalRequestedEvent(
        String eventId,
        Long withdrawalId,
        Long userId,
        String assetSymbol,
        String chainType,
        String toAddress,
        BigDecimal amount,
        String status,
        LocalDateTime requestedAt,
        LocalDateTime occurredAt
) {
}
