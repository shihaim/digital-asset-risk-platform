package com.example.digital_asset_risk_platform.risk.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RiskRuleCodes {

    public static final String NEW_DEVICE_WITHDRAWAL = "NEW_DEVICE_WITHDRAWAL";
    public static final String OTP_RESET_WITHDRAWAL = "OTP_RESET_WITHDRAWAL";
    public static final String PASSWORD_CHANGED_WITHDRAWAL = "PASSWORD_CHANGED_WITHDRAWAL";
    public static final String NEW_WALLET_ADDRESS = "NEW_WALLET_ADDRESS";
    public static final String HIGH_AMOUNT_WITHDRAWAL = "HIGH_AMOUNT_WITHDRAWAL";
    public static final String FREQUENT_WITHDRAWAL_24H = "FREQUENT_WITHDRAWAL_24H";
    public static final String HIGH_RISK_WALLET = "HIGH_RISK_WALLET";
}
