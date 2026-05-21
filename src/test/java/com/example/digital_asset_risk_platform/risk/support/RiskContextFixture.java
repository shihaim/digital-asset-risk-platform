package com.example.digital_asset_risk_platform.risk.support;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.risk.context.AccountRiskSnapshot;
import com.example.digital_asset_risk_platform.risk.application.WalletRiskSnapshot;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

        private LocalDateTime requestedAt = LocalDateTime.now();
        private LocalDateTime latestNewDeviceLoginAt = null;
        private LocalDateTime latestPasswordChangedAt = null;
        private LocalDateTime latestOtpResetAt = null;

        private WalletRiskLevel walletRiskLevel = WalletRiskLevel.LOW;
        private int walletRiskScore = 0;
        private KytRiskCategory walletRiskCategory = KytRiskCategory.NORMAL;

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
            this.latestNewDeviceLoginAt = value ? requestedAt.minusMinutes(30) : null;
            return this;
        }

        public Builder passwordChangedWithin24h(boolean value) {
            this.latestPasswordChangedAt = value ? requestedAt.minusHours(12) : null;
            return this;
        }

        public Builder otpResetWithin24h(boolean value) {
            this.latestOtpResetAt = value ? requestedAt.minusHours(12) : null;
            return this;
        }

        public Builder walletRisk(
                WalletRiskLevel riskLevel,
                int riskScore,
                String riskCategory
        ) {
            return walletRisk(riskLevel, riskScore, KytRiskCategory.valueOf(riskCategory));
        }

        public Builder walletRisk(
                WalletRiskLevel riskLevel,
                int riskScore,
                KytRiskCategory riskCategory
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

        public Builder requestedAt(LocalDateTime requestedAt) {
            this.requestedAt = requestedAt;
            return this;
        }

        public Builder latestNewDeviceLoginAt(LocalDateTime value) {
            this.latestNewDeviceLoginAt = value;
            return this;
        }

        public Builder latestPasswordChangedAt(LocalDateTime value) {
            this.latestPasswordChangedAt = value;
            return this;
        }

        public Builder latestOtpResetAt(LocalDateTime value) {
            this.latestOtpResetAt = value;
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
                    latestNewDeviceLoginAt,
                    latestPasswordChangedAt,
                    latestOtpResetAt
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
