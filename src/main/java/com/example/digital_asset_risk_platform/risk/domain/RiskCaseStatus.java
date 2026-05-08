package com.example.digital_asset_risk_platform.risk.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RiskCaseStatus {
    REVIEW_REQUIRED("검토 필요"),
    IN_REVIEW("검토 중"),
    APPROVED("승인"),
    REJECTED("거절"),
    FALSE_POSITIVE("오탐"),
    TRUE_POSITIVE("정탐"),
    CLOSED("종료");

    private final String description;
}
