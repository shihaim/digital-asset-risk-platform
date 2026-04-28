package com.example.digital_asset_risk_platform.admin.dto;

import com.example.digital_asset_risk_platform.account.domain.AccountLoginEvent;
import com.example.digital_asset_risk_platform.account.domain.AccountSecurityEvent;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;

import java.time.LocalDateTime;

public record RiskTimelineEventResponse(
        String eventType,
        String description,
        LocalDateTime eventAt
) {
    public static RiskTimelineEventResponse toLoginTimeline(AccountLoginEvent event) {
        String description = event.isNewDevice() ? "신규 기기 로그인" : "기존 기기 로그인";

        return new RiskTimelineEventResponse("LOGIN", description + " - deviceId=" + event.getDeviceId(), event.getLoginAt());
    }

    public static RiskTimelineEventResponse toSecurityTimeline(AccountSecurityEvent event) {
        return new RiskTimelineEventResponse(event.getEventType().name(), "보안 이벤트 발생: " + event.getEventType().name(), event.getEventAt());
    }

    public static RiskTimelineEventResponse toWithdrawalTimeline(WithdrawalRequest withdrawal) {
        return new RiskTimelineEventResponse(
                "WITHDRAWAL_REQUESTED",
                withdrawal.getAssetSymbol() + " " + withdrawal.getAmount() + " 출금 요청 - status=" + withdrawal.getStatus(),
                withdrawal.getRequestedAt()
        );
    }
}
