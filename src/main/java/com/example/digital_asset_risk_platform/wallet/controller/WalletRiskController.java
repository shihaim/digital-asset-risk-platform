package com.example.digital_asset_risk_platform.wallet.controller;

import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallet-risks")
public class WalletRiskController {

    private final WalletRiskService walletRiskService;

    @PostMapping
    public ResponseEntity<Long> createWalletRisk(@RequestBody @Valid WalletRiskCreateRequest request) {
        return ResponseEntity.ok(walletRiskService.createWalletRisk(request));
    }
}
