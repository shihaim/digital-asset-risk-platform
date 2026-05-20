package com.example.digital_asset_risk_platform.account.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SecurityEventType {
    PASSWORD_CHANGED("비밀번호 변경"),
    OTP_RESET("OTP 초기화"),
    PHONE_CHANGED("휴대폰 번호 변경"),
    EMAIL_CHANGED("이메일 변경"),
    WITHDRAWAL_ADDRESS_ADDED("출금 주소 추가");
    
    private final String description;
}
