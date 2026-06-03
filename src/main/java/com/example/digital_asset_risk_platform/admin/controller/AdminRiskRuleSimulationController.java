package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.risk.simulation.application.RiskRuleSimulationService;
import com.example.digital_asset_risk_platform.risk.simulation.dto.RiskRuleSimulationRequest;
import com.example.digital_asset_risk_platform.risk.simulation.dto.RiskRuleSimulationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk-rules")
public class AdminRiskRuleSimulationController {

    private final RiskRuleSimulationService riskRuleSimulationService;

    @PostMapping("/simulate")
    public ResponseEntity<RiskRuleSimulationResponse> simulate(@Valid @RequestBody RiskRuleSimulationRequest request) {
        return ResponseEntity.ok(riskRuleSimulationService.simulate(request));
    }
}
