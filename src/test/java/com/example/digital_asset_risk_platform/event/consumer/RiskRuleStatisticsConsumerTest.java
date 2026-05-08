package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.statistics.application.RiskRuleStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class RiskRuleStatisticsConsumerTest {

    private final RiskRuleStatisticsService riskRuleStatisticsService = mock(RiskRuleStatisticsService.class);
    private final ProcessedEventService processedEventService = mock(ProcessedEventService.class);
    private final RiskRuleStatisticsConsumer consumer = new RiskRuleStatisticsConsumer(riskRuleStatisticsService, processedEventService);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @Test
    @DisplayName("평가 완료 이벤트의 ruleCodes 기준으로 룰 통계를 증가시키고 ack 처리한다")
    void case1() {
        //given
        LocalDateTime evaluatedAt = LocalDateTime.now();

        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-evaluation-001", 10L, "WITHDRAWAL", 1L, 10001L,
                190, "CRITICAL", "BLOCK_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "OTP_RESET_WITHDRAWAL", "HIGH_RISK_WALLET"),
                evaluatedAt,
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed("rule-statistics-consumer", event.eventId())).thenReturn(false);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(riskRuleStatisticsService).increaseRuleHit("NEW_DEVICE_WITHDRAWAL", evaluatedAt);
        verify(riskRuleStatisticsService).increaseRuleHit("OTP_RESET_WITHDRAWAL", evaluatedAt);
        verify(riskRuleStatisticsService).increaseRuleHit("HIGH_RISK_WALLET", evaluatedAt);
        verify(processedEventService).markProcessed("rule-statistics-consumer", event.eventId());
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이미 처리한 eventId면 통계를 증가시키지 않고 ack 처리한다")
    void case2() {
        // given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-already-processed",
                10L,
                "WITHDRAWAL",
                1L,
                10001L,
                190,
                "CRITICAL",
                "BLOCK_WITHDRAWAL",
                List.of("HIGH_RISK_WALLET"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(processedEventService.isProcessed(
                "rule-statistics-consumer",
                event.eventId()
        )).thenReturn(true);

        //when
        consumer.consume(event, acknowledgment);

        //then
        verify(riskRuleStatisticsService, never()).increaseRuleHit("HIGH_RISK_WALLET", event.evaluatedAt());
        verify(processedEventService, never()).markProcessed("rule-statistics-consumer", event.eventId());
        verify(acknowledgment).acknowledge();
    }
}