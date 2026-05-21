package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigAdminService;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigResponse;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk-rules")
public class AdminRiskRuleConfigController {

    private final RiskRuleConfigAdminService riskRuleConfigAdminService;

    @GetMapping
    public ResponseEntity<List<RiskRuleConfigResponse>> getRuleConfigs() {
        return ResponseEntity.ok(riskRuleConfigAdminService.getRuleConfigs());
    }

    @GetMapping("/{ruleCode}")
    public ResponseEntity<RiskRuleConfigResponse> getRuleConfig(@PathVariable String ruleCode) {
        return ResponseEntity.ok(riskRuleConfigAdminService.getRuleConfig(ruleCode));
    }

    @PatchMapping("/{ruleCode}")
    public ResponseEntity<RiskRuleConfigResponse> updateRuleConfig(@PathVariable String ruleCode, @Valid @RequestBody RiskRuleConfigUpdateRequest request) {
        return ResponseEntity.ok(riskRuleConfigAdminService.updateRuleConfig(ruleCode, request));
    }
}
