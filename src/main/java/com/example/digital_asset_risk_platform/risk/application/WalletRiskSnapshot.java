package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCacheResponse;

public record WalletRiskSnapshot(
        WalletRiskLevel riskLevel,
        int riskScore,
        KytRiskCategory riskCategory
) {
    public boolean isHighRisk() {
        return riskLevel == WalletRiskLevel.HIGH || riskLevel == WalletRiskLevel.CRITICAL;
    }

    public static WalletRiskSnapshot from(WalletRiskCacheResponse response) {
        if (response == null) {
            return new WalletRiskSnapshot(
                    WalletRiskLevel.LOW,
                    0,
                    KytRiskCategory.UNKNOWN_ADDRESS
            );
        }

        return new WalletRiskSnapshot(
                response.riskLevel(),
                response.riskScore(),
                response.riskCategory()
        );
    }
}
