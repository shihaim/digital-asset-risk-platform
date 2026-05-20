package com.example.digital_asset_risk_platform.kyt.domain;

// TODO: risk category 관련 전부 마이그레이션 필요
public enum KytRiskCategory {
    NORMAL,
    HACKED_FUNDS,
    PHISHING,
    SANCTIONED_ADDRESS,
    MIXER,
    DARKNET_MARKET,
    SCAM,
    HIGH_RISK_EXCHANGE
}
