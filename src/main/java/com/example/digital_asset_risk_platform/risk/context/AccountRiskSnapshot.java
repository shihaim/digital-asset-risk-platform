package com.example.digital_asset_risk_platform.risk.context;

public record AccountRiskSnapshot(
        boolean newDeviceLoginWithin1h,
        boolean passwordChangedWithin24h,
        boolean otpResetWithin24h
) {
}
