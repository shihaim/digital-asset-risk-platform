package com.example.digital_asset_risk_platform.common.exception;

import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class GlobalExceptionHandlerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("존재하지 않는 출금 조회 시 공통 예외 응답을 반환한다")
    void case1() throws Exception {
        mockMvc.perform(get("/api/withdrawals/{withdrawalId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WITHDRAWAL_001"))
                .andExpect(jsonPath("$.message").value("출금 요청을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("출금 요청 값이 유효하지 않으면 Validation 오류 응답을 반환한다")
    void case2() throws Exception {
        String requestBody = """
            {
              "userId": null,
              "assetSymbol": "",
              "chainType": "TRON",
              "toAddress": "",
              "amount": "-1"
            }
        """;

        mockMvc.perform(
                post("/api/withdrawals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON_001"))
            .andExpect(jsonPath("$.message").value("요청 값이 올바르지 않습니다."))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(greaterThan(0)));
    }
}