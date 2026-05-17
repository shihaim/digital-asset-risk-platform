package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.statistics.application.RiskRuleStatisticsService;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminRiskRuleStatisticsControllerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RiskRuleStatisticsService riskRuleStatisticsService;

    @Autowired
    RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    @BeforeEach
    void setUp() {
        riskRuleStatisticsRepository.deleteAll();
    }

    @Test
    @DisplayName("Rule 통계 목록을 조회한다")
    void case1() throws Exception {
        //given
        LocalDateTime now = LocalDateTime.now();

        riskRuleStatisticsService.increaseRuleHit("HIGH_RISK_WALLET", now);
        riskRuleStatisticsService.increaseRuleHit("HIGH_RISK_WALLET", now);
        riskRuleStatisticsService.increaseRuleHit("OTP_RESET_WITHDRAWAL", now);

        //when&then
        mockMvc.perform(get("/api/admin/risk-rule-statistics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].ruleCode").value("HIGH_RISK_WALLET"))
                .andExpect(jsonPath("$.content[0].hitCount").value(2))
                .andExpect(jsonPath("$.content[1].ruleCode").value("OTP_RESET_WITHDRAWAL"))
                .andExpect(jsonPath("$.content[1].hitCount").value(1));
    }

    @Test
    @DisplayName("상위 Rule 통계를 조회한다")
    void case2() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();

        riskRuleStatisticsService.increaseRuleHit("HIGH_RISK_WALLET", now);
        riskRuleStatisticsService.increaseRuleHit("HIGH_RISK_WALLET", now);
        riskRuleStatisticsService.increaseRuleHit("OTP_RESET_WITHDRAWAL", now);
        riskRuleStatisticsService.increaseRuleHit("NEW_DEVICE_WITHDRAWAL", now);

        // when & then
        mockMvc.perform(get("/api/admin/risk-rule-statistics/top")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].ruleCode").value("HIGH_RISK_WALLET"))
                .andExpect(jsonPath("$.content[0].hitCount").value(2));
    }
}