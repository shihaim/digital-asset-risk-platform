package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.risk.profile.application.UserRiskProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class UserRiskProfileCaseConsumerTest {

    private final UserRiskProfileService userRiskProfileService = mock(UserRiskProfileService.class);
    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);

    private final UserRiskProfileCaseConsumer consumer = new UserRiskProfileCaseConsumer(userRiskProfileService, processedEventService);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @Test
    @DisplayName("Case 생성 이벤트 수신 시 case count를 증가시킨다")
    void case1() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-001",
                100L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed("user-risk-profile-case-consumer", "event-001"))
                .thenReturn(false);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(userRiskProfileService).increaseCaseCount(10001L);
        verify(processedEventService).isProcessed("user-risk-profile-case-consumer", "event-001");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리된 Case 생성 이벤트면 case count를 증가시키지 않고 ack 처리한다")
    void case2() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-002",
                100L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed("user-risk-profile-case-consumer", "event-002"))
                .thenReturn(true);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(userRiskProfileService, never()).increaseCaseCount(anyLong());
        verify(processedEventService, never()).markProcessed(anyString(), anyString());
        verify(acknowledgment).acknowledge();
    }
}