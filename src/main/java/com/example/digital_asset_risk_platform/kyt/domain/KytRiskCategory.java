package com.example.digital_asset_risk_platform.kyt.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum KytRiskCategory {
    UNKNOWN_ADDRESS("조회되지 않은 주소"),
    NORMAL("정상 주소"),
    HACKED_FUNDS("해킹 자금 연관 주소"),
    PHISHING("피싱 연관 주소"),
    SANCTIONED_ADDRESS("제재 주소"),
    MIXER("믹서 연관 주소"),
    DARKNET_MARKET("다크넷 마켓 연관 주소"),
    SCAM("스캠 연관 주소"),
    HIGH_RISK_EXCHANGE("고위험 거래소 연관 주소");

    private final String description;
}
