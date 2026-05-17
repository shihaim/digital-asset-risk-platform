package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.statistics.application.RiskRuleStatisticsService;
import com.example.digital_asset_risk_platform.statistics.dto.RiskRuleStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk-rule-statistics")
public class AdminRiskRuleStatisticsController {

    private final RiskRuleStatisticsService riskRuleStatisticsService;

    @GetMapping
    public ResponseEntity<Page<RiskRuleStatisticsResponse>> getStatistics(Pageable pageable) {
        return ResponseEntity.ok(riskRuleStatisticsService.getStatistics(pageable));
    }

    @GetMapping("/top")
    public ResponseEntity<Page<RiskRuleStatisticsResponse>> getTopStatistics(@RequestParam(defaultValue = "5") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        return ResponseEntity.ok(riskRuleStatisticsService.getStatistics(PageRequest.of(0, safeLimit)));
    }
}
