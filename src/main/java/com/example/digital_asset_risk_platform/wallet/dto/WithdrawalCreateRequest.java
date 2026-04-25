package com.example.digital_asset_risk_platform.wallet.dto;

import java.math.BigDecimal;

public record WithdrawalCreateRequest(
        Long userId,
        String assetSymbol,
        String chainType,
        String toAddress,
        BigDecimal amount
) {
}
