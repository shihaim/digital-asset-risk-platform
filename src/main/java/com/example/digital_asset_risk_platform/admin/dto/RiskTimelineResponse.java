package com.example.digital_asset_risk_platform.admin.dto;

import java.util.List;

public record RiskTimelineResponse(
        Long userId,
        List<RiskTimelineEventResponse> events
) {
}
