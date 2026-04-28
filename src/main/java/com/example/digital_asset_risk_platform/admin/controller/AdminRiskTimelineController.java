package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.admin.application.RiskTimelineService;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminRiskTimelineController {

    private final RiskTimelineService riskTimelineService;

    @GetMapping("/{userId}/risk-timeline")
    public ResponseEntity<RiskTimelineResponse> getUserTimeline(@PathVariable Long userId) {
        return ResponseEntity.ok(riskTimelineService.getUserTimeline(userId));
    }
}
