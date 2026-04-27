package com.example.digital_asset_risk_platform.wallet.controller;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
public class WithdrawalControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("출금 요청 API를 호출하면 출금 요청이 생성되고 FDS 평가 결과를 응답한다")
    void case1() throws Exception {
        String requestBody = """
            {
              "userId": 10001,
              "assetSymbol": "USDT",
              "chainType": "TRON",
              "toAddress": "TNORMAL000001",
              "amount": "100.000000000000000000"
            }
        """;

        mockMvc.perform(post("/api/withdrawals").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.withdrawalId").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.riskLevel").exists())
                .andExpect(jsonPath("$.decision").exists())
                .andExpect(jsonPath("$.totalScore").exists());
    }
}
