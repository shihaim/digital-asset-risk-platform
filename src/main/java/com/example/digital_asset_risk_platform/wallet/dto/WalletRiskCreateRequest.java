package com.example.digital_asset_risk_platform.wallet.dto;

import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WalletRiskCreateRequest(
        @NotBlank String chainType,
        @NotBlank String address,
        @NotNull WalletRiskLevel riskLevel,
        @NotNull Integer riskScore,
        String riskCategory,
        String provider
) {
}
