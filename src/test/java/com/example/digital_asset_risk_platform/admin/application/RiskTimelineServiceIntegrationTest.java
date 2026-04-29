package com.example.digital_asset_risk_platform.admin.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineEventResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineResponse;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ActiveProfiles("test")
public class RiskTimelineServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    RiskTimelineService riskTimelineService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @BeforeEach
    void setUp() {
        withdrawalRequestRepository.deleteAll();
        accountSecurityEventRepository.deleteAll();
        accountLoginEventRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자의 로그인, 보안 이벤트, 출금 요청을 시간순으로 조회한다")
    void case1() {
        Long userId = 10001L;
        LocalDateTime now = LocalDateTime.now();

        accountEventService.createLoginEvent(new LoginEventCreateRequest(
                userId,
                "device-001",
                "127.0.0.1",
                "KR",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "device-001",
                "127.0.0.1",
                now.minusMinutes(20)
        ));

        withdrawalRequestRepository.save(new WithdrawalRequest(
                userId,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        ));

        //when
        RiskTimelineResponse response = riskTimelineService.getUserTimeline(userId);

        //then
        Assertions.assertThat(response.userId()).isEqualTo(userId);
        Assertions.assertThat(response.events()).hasSize(3);
        Assertions.assertThat(response.events())
                .extracting(RiskTimelineEventResponse::eventType)
                .contains("LOGIN", "OTP_RESET", "WITHDRAWAL_REQUESTED");
        Assertions.assertThat(response.events().get(0).eventAt())
                .isBeforeOrEqualTo(response.events().get(1).eventAt());
        Assertions.assertThat(response.events().get(1).eventAt())
                .isBeforeOrEqualTo(response.events().get(2).eventAt());
    }
}
