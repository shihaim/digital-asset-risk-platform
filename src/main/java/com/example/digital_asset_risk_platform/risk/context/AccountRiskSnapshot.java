package com.example.digital_asset_risk_platform.risk.context;

import java.time.Duration;
import java.time.LocalDateTime;

public record AccountRiskSnapshot(
        LocalDateTime latestNewDeviceLoginAt,
        LocalDateTime latestPasswordChangedAt,
        LocalDateTime latestOtpResetAt
) {

    public boolean hasNewDeviceLoginWithin(LocalDateTime baseTime, Duration threshold) {
        return isWithin(latestNewDeviceLoginAt, baseTime, threshold);
    }

    public boolean hasPasswordChangedWithin(LocalDateTime baseTime, Duration threshold) {
        return isWithin(latestPasswordChangedAt, baseTime, threshold);
    }

    public boolean hasOtpResetWithin(LocalDateTime baseTime, Duration threshold) {
        return isWithin(latestOtpResetAt, baseTime, threshold);
    }

    private boolean isWithin(LocalDateTime eventAt, LocalDateTime baseTime, Duration threshold) {
        if (eventAt == null || baseTime == null || threshold == null) {
            return false;
        }
        LocalDateTime thresholdStart = baseTime.minus(threshold);
        return !eventAt.isBefore(thresholdStart) && !eventAt.isAfter(baseTime);
    }
}
