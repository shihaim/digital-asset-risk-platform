package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.admin.application.RiskDashboardService;
import com.example.digital_asset_risk_platform.admin.dto.RiskDashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk-dashboard")
public class AdminRiskDashboardController {

    private final RiskDashboardService riskDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<RiskDashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(riskDashboardService.getSummary());
    }
}
