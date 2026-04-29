package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminRiskCaseControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @BeforeEach
    void setUp() {
        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자 RiskCase 목록 조회 API")
    void case1() throws Exception {
        //given
        createHighRiskWithdrawal(10001L);

        //when&then
        mockMvc.perform(get("/api/admin/risk-cases")
                .param("status", RiskCaseStatus.REVIEW_REQUIRED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].caseId").exists())
            .andExpect(jsonPath("$[0].status").value(RiskCaseStatus.REVIEW_REQUIRED.name()))
            .andExpect(jsonPath("$[0].caseType").value("AML_REVIEW"));
    }

    @Test
    @DisplayName("관리자 RiskCase 상세 조회 API")
    void case2() throws Exception {
        //given
        WithdrawalCreateResponse response = createHighRiskWithdrawal(20001L);

        //when&then
        mockMvc.perform(get("/api/admin/risk-cases/{caseId}", response.caseId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value(response.caseId()))
                .andExpect(jsonPath("$.status").value(RiskCaseStatus.REVIEW_REQUIRED.name()))
                .andExpect(jsonPath("$.withdrawal.withdrawalId").value(response.withdrawalId()))
                .andExpect(jsonPath("$.evaluation.totalScore").exists())
                .andExpect(jsonPath("$.ruleHits").isArray())
                .andExpect(jsonPath("$.timeline").isArray());
    }

    @Test
    @DisplayName("관리자 RiskCase 승인 API")
    void case3() throws Exception {
        //given
        WithdrawalCreateResponse response = createHeldWithdrawal(30001L);

        String requestBody = """
                    {
                        "reviewer": "admin01",
                        "comment": "본인 확인 완료"
                    }
                """;

        //when&then
        mockMvc.perform(post("/api/admin/risk-cases/{caseId}/approve", response.caseId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 RiskCase 거절 API")
    void case4() throws Exception {
        //given
        WithdrawalCreateResponse response = createHeldWithdrawal(40001L);

        String requestBody = """
                    {
                        "reviewer": "admin01",
                        "comment": "피싱 피해 의심"
                    }
                """;

        //when&then
        mockMvc.perform(post("/api/admin/risk-cases/{caseId}/reject", response.caseId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    private WithdrawalCreateResponse createHighRiskWithdrawal(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String address = "THACKED" + userId;

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                address,
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        accountEventService.createLoginEvent(new LoginEventCreateRequest(
                userId,
                "new-device-" + userId,
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-" + userId,
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        return withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                "TRON",
                address,
                new BigDecimal("10000.000000000000000000")
        ));
    }

    private WithdrawalCreateResponse createHeldWithdrawal(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String address = "TNEW" + userId;

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                address,
                WalletRiskLevel.LOW,
                0,
                "NORMAL",
                "MOCK_KYT"
        ));

        accountEventService.createLoginEvent(new LoginEventCreateRequest(
                userId,
                "new-device-" + userId,
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-" + userId,
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        return withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                "TRON",
                address,
                new BigDecimal("10000.000000000000000000")
        ));
    }
}
