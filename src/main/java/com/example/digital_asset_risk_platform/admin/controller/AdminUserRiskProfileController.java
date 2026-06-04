package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.risk.profile.application.UserRiskProfileService;
import com.example.digital_asset_risk_platform.risk.profile.dto.UserRiskProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserRiskProfileController {

    private final UserRiskProfileService userRiskProfileService;

    @GetMapping("/{userId}/risk-profile")
    public ResponseEntity<UserRiskProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userRiskProfileService.getProfile(userId));
    }
}
