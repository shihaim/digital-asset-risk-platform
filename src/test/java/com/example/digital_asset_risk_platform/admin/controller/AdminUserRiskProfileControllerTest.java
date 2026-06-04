package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.risk.profile.application.UserRiskProfileService;
import com.example.digital_asset_risk_platform.risk.profile.dto.UserRiskProfileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserRiskProfileController.class)
class AdminUserRiskProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRiskProfileService userRiskProfileService;

    @Test
    @DisplayName("사용자 Risk Profile을 조회한다")
    void case1() throws Exception {
        //given
        Long userId = 10001L;

        UserRiskProfileResponse response = new UserRiskProfileResponse(
                userId,
                80,
                "HIGH",
                2,
                1,
                LocalDateTime.now()
        );

        when(userRiskProfileService.getProfile(userId))
                .thenReturn(response);

        //when&then
        mockMvc.perform(get("/api/admin/users/{userId}/risk-profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10001L))
                .andExpect(jsonPath("$.currentRiskScore").value(80))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.totalCaseCount").value(2))
                .andExpect(jsonPath("$.totalBlockedWithdrawalCount").value(1))
                .andExpect(jsonPath("$.lastEvaluatedAt").exists());
    }

    @Test
    @DisplayName("프로필 없는 사용자 조회 시 기본 Risk Profile을 반환한다")
    void case2() throws Exception {
        //given
        Long userId = 999L;

        UserRiskProfileResponse response = new UserRiskProfileResponse(
                userId,
                0,
                "NORMAL",
                0,
                0,
                null
        );

        when(userRiskProfileService.getProfile(userId))
                .thenReturn(response);

        //when&then
        mockMvc.perform(get("/api/admin/users/{userId}/risk-profile", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(999))
                .andExpect(jsonPath("$.currentRiskScore").value(0))
                .andExpect(jsonPath("$.riskLevel").value("NORMAL"))
                .andExpect(jsonPath("$.totalCaseCount").value(0))
                .andExpect(jsonPath("$.totalBlockedWithdrawalCount").value(0))
                .andExpect(jsonPath("$.lastEvaluatedAt").doesNotExist());
    }
}