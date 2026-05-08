package com.example.digital_asset_risk_platform.wallet.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum WithdrawalStatus {
    REQUESTED("출금 요청이 생성됨"),
    EVALUATING("FDS 평가 대기 또는 평가 중"),
    APPROVED("출금 승인 가능"),
    HELD("관리자 심사 필요"),
    BLOCKED("자동 차단"),
    REJECTED("관리자 또는 정책에 의해 거절"),
    COMPLETED("실제 출금 완료");

    private final String description;
}
