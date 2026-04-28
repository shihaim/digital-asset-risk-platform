package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.admin.application.AdminRiskCaseService;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseDetailResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseReviewRequest;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseSummaryResponse;
import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/risk-cases")
public class AdminRiskCaseController {

    private final AdminRiskCaseService adminRiskCaseService;

    @GetMapping
    public ResponseEntity<List<RiskCaseSummaryResponse>> getRiskCases(@RequestParam(required = false) RiskCaseStatus status) {
        return ResponseEntity.ok(adminRiskCaseService.getRiskCases(status));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<RiskCaseDetailResponse> getRiskCaseDetail(@PathVariable Long caseId) {
        return ResponseEntity.ok(adminRiskCaseService.getRiskCaseDetail(caseId));
    }

    @PostMapping("/{caseId}/start-review")
    public ResponseEntity<Void> startReview(@PathVariable Long caseId, @RequestBody @Valid RiskCaseReviewRequest request) {
        adminRiskCaseService.startReview(caseId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{caseId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long caseId, @RequestBody @Valid RiskCaseReviewRequest request) {
        adminRiskCaseService.approve(caseId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{caseId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long caseId, @RequestBody @Valid RiskCaseReviewRequest request) {
        adminRiskCaseService.reject(caseId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{caseId}/mark-false-positive")
    public ResponseEntity<Void> markFalsePositive(@PathVariable Long caseId, @RequestBody @Valid RiskCaseReviewRequest request) {
        adminRiskCaseService.markFalsePositive(caseId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{caseId}/mark-true-positive")
    public ResponseEntity<Void> markTruePositive(@PathVariable Long caseId, @RequestBody @Valid RiskCaseReviewRequest request) {
        adminRiskCaseService.markTruePositive(caseId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
