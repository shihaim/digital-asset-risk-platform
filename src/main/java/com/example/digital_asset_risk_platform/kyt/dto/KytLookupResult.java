package com.example.digital_asset_risk_platform.kyt.dto;

import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;

public record KytLookupResult(
        String chainType,
        String address,
        WalletRiskLevel riskLevel,
        Integer riskScore,
        String riskCategory,
        String provider,
        boolean risky
) {
    public static KytLookupResult normal(
            String chainType,
            String address,
            String provider
    ) {
        return new KytLookupResult(
                chainType,
                address,
                WalletRiskLevel.LOW,
                0,
                "NORMAL",
                provider,
                false
        );
    }

    public static KytLookupResult risky(
            String chainType,
            String address,
            WalletRiskLevel riskLevel,
            Integer riskScore,
            String riskCategory,
            String provider
    ) {
        return new KytLookupResult(
                chainType,
                address,
                riskLevel,
                riskScore,
                riskCategory,
                provider,
                true
        );
    }
}
