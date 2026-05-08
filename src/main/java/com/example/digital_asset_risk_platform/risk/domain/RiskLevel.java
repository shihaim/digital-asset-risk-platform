package com.example.digital_asset_risk_platform.risk.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RiskLevel {
    NORMAL("정상"),
    WATCH("관찰 필요"),
    CAUTION("주의"),
    HIGH("높음"),
    CRITICAL("치명적");

    private final String description;
}
