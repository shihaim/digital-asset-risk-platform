package com.example.digital_asset_risk_platform.wallet.controller;

import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/withdrawals")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public ResponseEntity<WithdrawalCreateResponse> createWithdrawal(@RequestBody @Valid WithdrawalCreateRequest request) {
        return ResponseEntity.ok(withdrawalService.createWithdrawal(request));
    }

    @GetMapping("/{withdrawalId}")
    public ResponseEntity<WithdrawalDetailResponse> getWithdrawal(@PathVariable Long withdrawalId) {
        return ResponseEntity.ok(withdrawalService.getWithdrawal(withdrawalId));
    }
}
