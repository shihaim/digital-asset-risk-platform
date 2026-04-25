package com.example.digital_asset_risk_platform.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LoginEventCreateRequest(
        @NotNull Long userId,
        @NotBlank String deviceId,
        @NotBlank String ipAddress,
        String countryCode,
        String userAgent,
        @NotNull LocalDateTime loginAt
) { }
