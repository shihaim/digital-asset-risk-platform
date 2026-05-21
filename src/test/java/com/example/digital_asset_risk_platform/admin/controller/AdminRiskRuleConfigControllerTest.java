package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigAdminService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminRiskRuleConfigControllerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RiskRuleConfigAdminService riskRuleConfigAdminService;

    @Autowired
    RiskRuleConfigRepository riskRuleConfigRepository;

    @BeforeEach
    void setUp() {
        riskRuleConfigRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/admin/risk-rules - Rule 설정 목록을 조회한다")
    void case1() throws Exception {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        ));

        //when&then
        mockMvc.perform(get("/api/admin/risk-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ruleCode").value(RiskRuleCodes.HIGH_RISK_WALLET))
                .andExpect(jsonPath("$[0].enabled").value(true))
                .andExpect(jsonPath("$[0].score").value(100))
                .andExpect(jsonPath("$[0].blocking").value(true))
                .andExpect(jsonPath("$[0].description").value("고위험 지갑 차단"));
    }

    @Test
    @DisplayName("GET /api/admin/risk-rules/{ruleCode} - Rule 설정 상세를 조회한다")
    void case2() throws Exception {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                "OTP 재설정 후 출금",
                true,
                40,
                false,
                "24h",
                "OTP 재설정"
        ));

        //when&then
        mockMvc.perform(get("/api/admin/risk-rules/{ruleCode}", RiskRuleCodes.OTP_RESET_WITHDRAWAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleCode").value(RiskRuleCodes.OTP_RESET_WITHDRAWAL))
                .andExpect(jsonPath("$.thresholdValue").value("24h"));
    }

    @Test
    @DisplayName("PATCH /api/admin/risk-rules/{ruleCode} - Rule 설정을 수정한다")
    void case3() throws Exception {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        ));

        //when
        String body = """
            {
              "enabled": false,
              "score": 80,
              "blocking": false,
              "description": "비활성화 테스트"
            }
        """;

        //then
        mockMvc.perform(patch("/api/admin/risk-rules/{ruleCode}", RiskRuleCodes.HIGH_RISK_WALLET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.score").value(80))
                .andExpect(jsonPath("$.blocking").value(false))
                .andExpect(jsonPath("$.description").value("비활성화 테스트"));
    }

    @Test
    @DisplayName("PATCH /api/admin/risk-rules/{ruleCode} - score가 음수이면 400을 반환한다")
    void case4() throws Exception {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        ));

        String body = """
            {
              "score": -1
            }
        """;

        mockMvc.perform(patch("/api/admin/risk-rules/{ruleCode}", RiskRuleCodes.HIGH_RISK_WALLET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}