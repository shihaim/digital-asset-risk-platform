package com.example.digital_asset_risk_platform.risk.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum RiskDecisionType {
    ALLOW("허용"),
    MONITOR("모니터링"),
    REQUIRE_ADDITIONAL_AUTH("추가 인증 필요"),
    HOLD_WITHDRAWAL("출금 보류"),
    BLOCK_WITHDRAWAL("출금 차단");

    private final String description;
}
