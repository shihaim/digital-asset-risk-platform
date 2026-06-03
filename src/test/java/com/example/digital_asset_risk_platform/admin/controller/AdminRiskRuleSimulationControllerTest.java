package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.risk.simulation.application.RiskRuleSimulationService;
import com.example.digital_asset_risk_platform.risk.support.RiskRuleConfigFixture;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminRiskRuleSimulationControllerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RiskRuleSimulationService riskRuleSimulationService;

    @Autowired
    RiskRuleConfigRepository riskRuleConfigRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @BeforeEach
    void setUp() {
        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
        accountLoginEventRepository.deleteAll();
        accountSecurityEventRepository.deleteAll();

        RiskRuleConfigFixture.resetDefaultConfigs(riskRuleConfigRepository);
    }

    @Test
    @DisplayName("Rule 시뮬레이션 요청 성공")
    void case1() throws Exception {
        //given
        String requestBody = """
            {
                "userId": 10001,
                "assetSymbol": "USDT",
                "chainType": "TRON",
                "toAddress": "TNORMAL000001",
                "amount": "100.000000000000000000"
            }
        """;

        //when&then
        mockMvc.perform(post("/api/admin/risk-rules/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScore").value(20))
                .andExpect(jsonPath("$.riskLevel").value(RiskLevel.NORMAL.name()))
                .andExpect(jsonPath("$.decision").value(RiskDecisionType.ALLOW.name()))
                .andExpect(jsonPath("$.ruleHits[0].ruleCode").value(RiskRuleCodes.NEW_WALLET_ADDRESS))
                .andExpect(jsonPath("$.ruleHits[0].score").value(20));
    }
}
