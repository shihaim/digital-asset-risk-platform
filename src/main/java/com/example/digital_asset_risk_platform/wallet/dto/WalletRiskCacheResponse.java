package com.example.digital_asset_risk_platform.wallet.dto;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.wallet.domain.WalletAddressRisk;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;

public record WalletRiskCacheResponse(
        String chainType,
        String address,
        WalletRiskLevel riskLevel,
        Integer riskScore,
        KytRiskCategory riskCategory,
        String provider
) {
    public static WalletRiskCacheResponse from(WalletAddressRisk walletRisk) {
        return new WalletRiskCacheResponse(
                walletRisk.getChainType(),
                walletRisk.getAddress(),
                walletRisk.getRiskLevel(),
                walletRisk.getRiskScore(),
                walletRisk.getRiskCategory(),
                walletRisk.getProvider()
        );
    }
}
