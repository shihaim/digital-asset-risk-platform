package com.example.digital_asset_risk_platform.risk.support;

import com.example.digital_asset_risk_platform.risk.context.AccountRiskSnapshot;
import com.example.digital_asset_risk_platform.risk.application.WalletRiskSnapshot;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;

import java.math.BigDecimal;

public class RiskContextFixture {
    private RiskContextFixture() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static RiskContext normalContext() {
        return builder().build();
    }

    public static class Builder {

        private Long userId = 10001L;
        private String assetSymbol = "USDT";
        private String chainType = "TRON";
        private String toAddress = "TNORMAL000001";
        private BigDecimal amount = new BigDecimal("100.000000000000000000");

        private boolean newDeviceLoginWithin1h = false;
        private boolean passwordChangedWithin24h = false;
        private boolean otpResetWithin24h = false;

        private WalletRiskLevel walletRiskLevel = WalletRiskLevel.LOW;
        private int walletRiskScore = 0;
        private String walletRiskCategory = "NORMAL";

        private boolean newWalletAddress = false;
        private BigDecimal averageWithdrawalAmount = new BigDecimal("100.000000000000000000");
        private long withdrawalCountLast24h = 0L;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = new BigDecimal(amount);
            return this;
        }

        public Builder newDeviceLoginWithin1h(boolean value) {
            this.newDeviceLoginWithin1h = value;
            return this;
        }

        public Builder passwordChangedWithin24h(boolean value) {
            this.passwordChangedWithin24h = value;
            return this;
        }

        public Builder otpResetWithin24h(boolean value) {
            this.otpResetWithin24h = value;
            return this;
        }

        public Builder walletRisk(
                WalletRiskLevel riskLevel,
                int riskScore,
                String riskCategory
        ) {
            this.walletRiskLevel = riskLevel;
            this.walletRiskScore = riskScore;
            this.walletRiskCategory = riskCategory;
            return this;
        }

        public Builder newWalletAddress(boolean value) {
            this.newWalletAddress = value;
            return this;
        }

        public Builder averageWithdrawalAmount(String amount) {
            this.averageWithdrawalAmount = new BigDecimal(amount);
            return this;
        }

        public Builder withdrawalCountLast24h(long count) {
            this.withdrawalCountLast24h = count;
            return this;
        }

        public RiskContext build() {
            WithdrawalRequest withdrawal = new WithdrawalRequest(
                    userId,
                    assetSymbol,
                    chainType,
                    toAddress,
                    amount
            );

            AccountRiskSnapshot accountRisk = new AccountRiskSnapshot(
                    newDeviceLoginWithin1h,
                    passwordChangedWithin24h,
                    otpResetWithin24h
            );

            WalletRiskSnapshot walletRisk = new WalletRiskSnapshot(
                    walletRiskLevel,
                    walletRiskScore,
                    walletRiskCategory
            );

            return new RiskContext(
                    withdrawal,
                    accountRisk,
                    walletRisk,
                    newWalletAddress,
                    averageWithdrawalAmount,
                    withdrawalCountLast24h
            );
        }
    }
}
