package com.example.digital_asset_risk_platform.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawalCreateRequest(

        @NotNull(message = "userId는 필수입니다.")
        Long userId,

        @NotBlank(message = "assetSymbol은 필수입니다.")
        String assetSymbol,

        @NotBlank(message = "chainType은 필수입니다.")
        String chainType,

        @NotBlank(message = "toAddress는 필수입니다.")
        String toAddress,

        @NotNull(message = "amount는 필수입니다.")
        @DecimalMin(value = "0.000000000000000001", message = "amount는 0보다 커야 합니다.")
        BigDecimal amount
) {
}
