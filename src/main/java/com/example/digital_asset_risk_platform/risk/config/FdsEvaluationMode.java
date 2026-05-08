package com.example.digital_asset_risk_platform.risk.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FdsEvaluationMode {
    SYNC("동기 평가"),
    ASYNC("비동기 평가");

    private final String description;
}
