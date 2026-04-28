package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalInfoResponse(
        Long withdrawalId,
        Long userId,
        String assetSymbol,
        String chainType,
        String toAddress,
        BigDecimal amount,
        WithdrawalStatus status,
        LocalDateTime requestedAt
) {
    public static WithdrawalInfoResponse from(WithdrawalRequest withdrawal) {
        return new WithdrawalInfoResponse(
                withdrawal.getId(),
                withdrawal.getUserId(),
                withdrawal.getAssetSymbol(),
                withdrawal.getChainType(),
                withdrawal.getToAddress(),
                withdrawal.getAmount(),
                withdrawal.getStatus(),
                withdrawal.getRequestedAt()
        );
    }
}
