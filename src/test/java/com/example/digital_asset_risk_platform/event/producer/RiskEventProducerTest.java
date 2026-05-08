package com.example.digital_asset_risk_platform.event.producer;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RiskEventProducerTest {

    private final KafkaTemplate<String, Object> objectKafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaTemplate<String, String> stringKafkaTemplate = mock(KafkaTemplate.class);
    private final RiskEventProducer producer = new RiskEventProducer(objectKafkaTemplate, stringKafkaTemplate);
    private final CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();

    @Test
    @DisplayName("출금 요청 이벤트를 withdrawal.requested topic으로 발행한다")
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
                "REQUESTED",
                LocalDateTime.of(2026, 5, 3, 10, 0),
                LocalDateTime.of(2026, 5, 3, 10, 0, 1)
        );

        //when
        when(objectKafkaTemplate.send(
                KafkaTopicConfig.WITHDRAWAL_REQUESTED,
                "1",
                event
        )).thenReturn(future);

        producer.publishWithdrawalRequested(event);

        //then
        verify(objectKafkaTemplate).send(
                KafkaTopicConfig.WITHDRAWAL_REQUESTED,
                "1",
                event
        );
    }

    @Test
    @DisplayName("FDS 평가 완료 이벤트를 risk.evaluation.completed topic으로 발행한다")
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
                List.of("NEW_DEVICE_WITHDRAWAL", "HIGH_RISK_WALLET"),
                LocalDateTime.of(2026, 5, 3, 10, 0, 2),
                LocalDateTime.of(2026, 5, 3, 10, 0, 3)
        );

        //when
        when(objectKafkaTemplate.send(
                KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
                "1",
                event
        )).thenReturn(future);

        producer.publishRiskEvaluationCompleted(event);

        //then
        verify(objectKafkaTemplate).send(
                KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
                "1",
                event
        );
    }

    @Test
    @DisplayName("RiskCase 생성 이벤트를 risk.case.created topic으로 발행한다")
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
                LocalDateTime.of(2026, 5, 3, 10, 0, 4),
                LocalDateTime.of(2026, 5, 3, 10, 0, 5)
        );

        //when
        when(objectKafkaTemplate.send(
                KafkaTopicConfig.RISK_CASE_CREATED,
                "100",
                event
        )).thenReturn(future);

        producer.publishRiskCaseCreated(event);

        //then
        verify(objectKafkaTemplate).send(
                KafkaTopicConfig.RISK_CASE_CREATED,
                "100",
                event
        );
    }

    @Test
    @DisplayName("Raw JSON payload를 지정한 topic과 key로 발행한다")
    void case4() {
        //given
        String topic = KafkaTopicConfig.WITHDRAWAL_REQUESTED;
        String key = "1";
        String payloadJson = """
            {
              "eventId": "event-001",
              "withdrawalId": 1,
              "userId": 10001
            }
        """;

        SendResult<String, String> sendResult = mock(SendResult.class);

        when(stringKafkaTemplate.send(topic, key, payloadJson))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        //when
        producer.publishRawJson(topic, key, payloadJson);

        //then
        verify(stringKafkaTemplate).send(topic, key, payloadJson);
    }
}
