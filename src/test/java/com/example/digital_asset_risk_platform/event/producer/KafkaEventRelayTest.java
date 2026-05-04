package com.example.digital_asset_risk_platform.event.producer;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class KafkaEventRelayTest {

    private final RiskEventProducer riskEventProducer = mock(RiskEventProducer.class);
    private final KafkaEventRelay relay = new KafkaEventRelay(riskEventProducer);

    @Test
    @DisplayName("WithdrawalRequestedEvent를 RiskEventProducer로 위임한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-001",
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100"),
                "REQUESTED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        relay.handle(event);

        //then
        verify(riskEventProducer).publishWithdrawalRequested(event);
    }

    @Test
    @DisplayName("RiskEvaluationCompletedEvent를 RiskEventProducer로 위임한다")
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
                List.of("HIGH_RISK_WALLET"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        relay.handle(event);

        //then
        verify(riskEventProducer).publishRiskEvaluationCompleted(event);
    }

    @Test
    @DisplayName("RiskCaseCreatedEvent를 RiskEventProducer로 위임한다")
    void case3() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-003",
                100L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        relay.handle(event);

        //then
        verify(riskEventProducer).publishRiskCaseCreated(event);
    }
}
