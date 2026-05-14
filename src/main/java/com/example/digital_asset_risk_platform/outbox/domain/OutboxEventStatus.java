package com.example.digital_asset_risk_platform.outbox.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum OutboxEventStatus {
    PENDING("발행 대기"),
    PROCESSING("발행 처리 중"),
    SENT("발행 완료"),
    FAILED("발행 실패"),
    DEAD("최대 재시도 초과로 자동 발행 중단");

    private final String description;
}
