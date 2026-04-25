package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;

public record WalletRiskSnapshot(
        WalletRiskLevel riskLevel,
        int riskScore,
        String riskCategory
) {
    public boolean isHighRisk() {
        return riskLevel == WalletRiskLevel.HIGH || riskLevel == WalletRiskLevel.CRITICAL;
    }
}
