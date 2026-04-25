package com.example.digital_asset_risk_platform.account.controller;

import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account-events")
public class AccountEventController {

    private final AccountEventService accountEventService;

    @PostMapping("/logins")
    public ResponseEntity<Long> createLoginEvent(@RequestBody @Valid LoginEventCreateRequest request) {
        return ResponseEntity.ok(accountEventService.createLoginEvent(request));
    }

    @PostMapping("/security")
    public ResponseEntity<Long> createSecurityEvent(@RequestBody @Valid SecurityEventCreateRequest request) {
        return ResponseEntity.ok(accountEventService.createSecurityEvent(request));
    }

}
