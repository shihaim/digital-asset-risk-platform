package com.example.digital_asset_risk_platform.risk.profile.domain;

public enum UserRiskLevel {
    NORMAL,
    WATCH,
    HIGH,
    CRITICAL;

    public static UserRiskLevel fromScore(int score) {
        if (score >= 150) {
            return CRITICAL;
        }

        if (score >= 80) {
            return HIGH;
        }

        if (score >= 30) {
            return WATCH;
        }

        return NORMAL;
    }
}
