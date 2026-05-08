package com.example.digital_asset_risk_platform.event.consumer;


import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.risk.application.FdsWithdrawalEvaluationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class FdsWithdrawalConsumerTest {

    private final FdsWithdrawalEvaluationService fdsWithdrawalEvaluationService = mock(FdsWithdrawalEvaluationService.class);
    private final FdsWithdrawalConsumer fdsWithdrawalConsumer = new FdsWithdrawalConsumer(fdsWithdrawalEvaluationService);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @Test
    @DisplayName("withdrawal.requested 이벤트를 소비하면 FDS 평가 서비스를 호출하고 ack 처리한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-001",
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        fdsWithdrawalConsumer.consume(event, acknowledgment);

        //then
        verify(fdsWithdrawalEvaluationService).evaluate(event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("FDS 평가 중 예외가 발생하면 ack 처리하지 않는다")
    void case2() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-failed",
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100.000000000000000000"),
                "EVALUATING",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        RuntimeException exception = new RuntimeException("evaluation failed");

        doThrow(exception)
                .when(fdsWithdrawalEvaluationService)
                .evaluate(event);

        //when&then
        Assertions.assertThatThrownBy(() -> fdsWithdrawalConsumer.consume(event, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("evaluation failed");

        verify(acknowledgment, never()).acknowledge();
    }
}