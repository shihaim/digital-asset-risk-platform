package com.example.digital_asset_risk_platform.risk.context;

import com.example.digital_asset_risk_platform.risk.application.AccountRiskSnapshot;
import com.example.digital_asset_risk_platform.risk.application.WalletRiskSnapshot;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;

import java.math.BigDecimal;

public record RiskContext(
        WithdrawalRequest withdrawal,
        AccountRiskSnapshot accountRisk,
        WalletRiskSnapshot walletRisk,
        boolean isNewWalletAddress,
        BigDecimal averageWithdrawalAmount,
        long withdrawalCountLast24h
) {
}
