package com.example.digital_asset_risk_platform.outbox.mapper;

import com.example.digital_asset_risk_platform.common.config.TestObjectMapperFactory;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

class OutboxEventMapperTest {

    private final OutboxEventMapper outboxEventMapper = new OutboxEventMapper(TestObjectMapperFactory.create());

    @Test
    @DisplayName("WithdrawalRequestedEvent를 OutboxEvent로 변환한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-withdrawal-001",
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100.000000000000000000"),
                "REQUESTED",
                LocalDateTime.of(2026, 5, 6, 10, 0),
                LocalDateTime.of(2026, 5, 6, 10, 0, 1)
        );

        //when
        OutboxEvent outboxEvent = outboxEventMapper.toOutboxEvent(event);

        //then
        Assertions.assertThat(outboxEvent.getEventId()).isEqualTo("event-withdrawal-001");
        Assertions.assertThat(outboxEvent.getEventType()).isEqualTo("WithdrawalRequestedEvent");
        Assertions.assertThat(outboxEvent.getTopicName()).isEqualTo(KafkaTopicConfig.WITHDRAWAL_REQUESTED);
        Assertions.assertThat(outboxEvent.getMessageKey()).isEqualTo("1");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"withdrawalId\":1");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"userId\":10001");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"assetSymbol\":\"USDT\"");
    }

    @Test
    @DisplayName("RiskEvaluationCompletedEvent를 OutboxEvent로 변환한다")
    void  case2() {
        //given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-evaluation-001",
                10L,
                "WITHDRAWAL",
                1L,
                10001L,
                190,
                "CRITICAL",
                "BLOCK_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "HIGH_RISK_WALLET"),
                LocalDateTime.of(2026, 5, 6, 10, 1),
                LocalDateTime.of(2026, 5, 6, 10, 1, 1)
        );

        //when
        OutboxEvent outboxEvent = outboxEventMapper.toOutboxEvent(event);

        //then
        Assertions.assertThat(outboxEvent.getEventId()).isEqualTo("event-evaluation-001");
        Assertions.assertThat(outboxEvent.getEventType()).isEqualTo("RiskEvaluationCompletedEvent");
        Assertions.assertThat(outboxEvent.getTopicName()).isEqualTo(KafkaTopicConfig.RISK_EVALUATION_COMPLETED);
        Assertions.assertThat(outboxEvent.getMessageKey()).isEqualTo("1");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"evaluationId\":10");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"decision\":\"BLOCK_WITHDRAWAL\"");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("HIGH_RISK_WALLET");
    }

    @Test
    @DisplayName("RiskCaseCreatedEvent를 OutboxEvent로 변환한다")
    void case3() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-case-001",
                100L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.of(2026, 5, 6, 10, 2),
                LocalDateTime.of(2026, 5, 6, 10, 2, 1)
        );

        //when
        OutboxEvent outboxEvent = outboxEventMapper.toOutboxEvent(event);

        //then
        Assertions.assertThat(outboxEvent.getEventId()).isEqualTo("event-case-001");
        Assertions.assertThat(outboxEvent.getEventType()).isEqualTo("RiskCaseCreatedEvent");
        Assertions.assertThat(outboxEvent.getTopicName()).isEqualTo(KafkaTopicConfig.RISK_CASE_CREATED);
        Assertions.assertThat(outboxEvent.getMessageKey()).isEqualTo("100");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"caseId\":100");
        Assertions.assertThat(outboxEvent.getPayloadJson()).contains("\"caseType\":\"AML_REVIEW\"");
    }

    @Test
    @DisplayName("지원하지 않는 이벤트 타입이면 예외가 발생한다")
    void case4() {
        //given
        Object unsupportedEvent = new Object();

        //when&then
        Assertions.assertThatThrownBy(() -> outboxEventMapper.toOutboxEvent(unsupportedEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 Outbox 이벤트 타입");
    }
}