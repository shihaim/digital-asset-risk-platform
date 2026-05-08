package com.example.digital_asset_risk_platform.account.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SecurityEventType {
    PASSWORD_CHANGED("비밀번호 변경"),
    OTP_RESET("OTP 초기화"),
    PHONE_CHANGED(""),
    EMAIL_CHANGED(""),
    WITHDRAWAL_ADDRESS_ADDED("");
    
    private final String description;
}
