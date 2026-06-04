package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.risk.profile.application.UserRiskProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class UserRiskProfileEvaluationConsumerTest {

    private final UserRiskProfileService userRiskProfileService = mock(UserRiskProfileService.class);
    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);

    private final UserRiskProfileEvaluationConsumer consumer = new UserRiskProfileEvaluationConsumer(userRiskProfileService, processedEventService);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @Test
    @DisplayName("평가 완료 이벤트 수신 시 UserRiskProfile을 갱신한다")
    void case1() {
        //given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-001",
                10L,
                "WITHDRAWAL",
                1L,
                10001L,
                100,
                "HIGH",
                "HOLD_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "OTP_RESET_WITHDRAWAL", "HIGH_RISK_WALLET"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed("user-risk-profile-evaluation-consumer", "event-001"))
                .thenReturn(false);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(userRiskProfileService).updateFromEvaluate(10001L, 100, false);
        verify(processedEventService).markProcessed("user-risk-profile-evaluation-consumer", "event-001");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("BLOCK_WITHDRAWAL 이벤트면 blocked true로 전달한다")
    void case2() {
        //given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-002",
                10L,
                "WITHDRAWAL",
                1L,
                10001L,
                190,
                "CRITICAL",
                "BLOCK_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "OTP_RESET_WITHDRAWAL", "HIGH_RISK_WALLET"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed("user-risk-profile-evaluation-consumer", "event-002"))
                .thenReturn(false);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(userRiskProfileService).updateFromEvaluate(10001L, 190, true);
        verify(processedEventService).markProcessed("user-risk-profile-evaluation-consumer", "event-002");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리된 평가 이벤트면 프로필을 갱신하지 않고 ack 처리한다")
    void case3() {
        //given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-003",
                10L,
                "WITHDRAWAL",
                1L,
                10001L,
                190,
                "CRITICAL",
                "BLOCK_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "OTP_RESET_WITHDRAWAL", "HIGH_RISK_WALLET"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed("user-risk-profile-evaluation-consumer", "event-003"))
                .thenReturn(true);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(userRiskProfileService, never()).updateFromEvaluate(anyLong(), anyInt(), anyBoolean());
        verify(processedEventService, never()).markProcessed(anyString(), anyString());
        verify(acknowledgment).acknowledge();
    }
}