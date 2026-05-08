package com.example.digital_asset_risk_platform.wallet.dto;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalDetailResponse(
        Long withdrawalId,
        Long userId,
        String assetSymbol,
        String chainType,
        String toAddress,
        BigDecimal amount,
        WithdrawalStatus status,
        RiskLevel riskLevel,
        RiskDecisionType decision,
        Integer totalScore,
        LocalDateTime requestedAt
) {
    public static WithdrawalDetailResponse of(WithdrawalRequest withdrawal, RiskLevel riskLevel, RiskDecisionType decision, Integer totalScore) {
        return new WithdrawalDetailResponse(
                withdrawal.getId(),
                withdrawal.getUserId(),
                withdrawal.getAssetSymbol(),
                withdrawal.getChainType(),
                withdrawal.getToAddress(),
                withdrawal.getAmount(),
                withdrawal.getStatus(),
                riskLevel,
                decision,
                totalScore,
                withdrawal.getRequestedAt()
        );
    }
}
