package com.example.digital_asset_risk_platform.wallet.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WalletRiskLevel {
    LOW("낮음"),
    MEDIUM("보통"),
    HIGH("높음"),
    CRITICAL("치명적");

    private final String description;
}
