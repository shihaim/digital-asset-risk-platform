package com.example.digital_asset_risk_platform.risk.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RiskCaseType {
    ACCOUNT_TAKEOVER("계정 탈취 의심"),
    WITHDRAWAL_FRAUD("출금 사기 의심"),
    AML_REVIEW("AML 검토");

    private final String description;
}
