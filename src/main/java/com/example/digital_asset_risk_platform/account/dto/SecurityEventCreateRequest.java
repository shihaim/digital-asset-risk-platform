package com.example.digital_asset_risk_platform.account.dto;

import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SecurityEventCreateRequest(
        @NotNull Long userId,
        @NotNull SecurityEventType eventType,
        String deviceId,
        String ipAddress,
        @NotNull LocalDateTime eventAt
) {
}
