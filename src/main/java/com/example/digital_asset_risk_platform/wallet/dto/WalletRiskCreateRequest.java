package com.example.digital_asset_risk_platform.wallet.dto;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WalletRiskCreateRequest(
        @NotBlank String chainType,
        @NotBlank String address,
        @NotNull WalletRiskLevel riskLevel,
        @NotNull Integer riskScore,
        @NotNull KytRiskCategory riskCategory,
        String provider
) {
    public WalletRiskCreateRequest(
            String chainType,
            String address,
            WalletRiskLevel riskLevel,
            Integer riskScore,
            String riskCategory,
            String provider
    ) {
        this(
                chainType,
                address,
                riskLevel,
                riskScore,
                KytRiskCategory.valueOf(riskCategory),
                provider
        );
    }
}
