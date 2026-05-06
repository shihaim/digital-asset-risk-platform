package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.audit.application.AuditEventLogService;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditEventConsumerTest {

    private final AuditEventLogService auditEventLogService = mock(AuditEventLogService.class);
    private final AuditEventConsumer auditEventConsumer = new AuditEventConsumer(auditEventLogService);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @Test
    @DisplayName("withdrawal.requested 이벤트를 감사 로그로 저장하고 ack 처리한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-withdrawal-001", 1L, 10001L,
                "USDT", "TRON", "TADDRESS001", new BigDecimal("100.000000000000000000"), "REQUESTED", LocalDateTime.now(), LocalDateTime.now());

        //when
        auditEventConsumer.consumeWithdrawalRequested(event, "1", acknowledgment);

        //then
        verify(auditEventLogService).saveIfNotExists(event.eventId(), "WithdrawalRequestedEvent", KafkaTopicConfig.WITHDRAWAL_REQUESTED, "1", event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("risk.evaluation.completed 이벤트를 감사 로그로 저장하고 ack 처리한다")
    void case2() {
        //given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-evaluation-001", 10L, "WITHDRAWAL", 1L, 10001L, 190, "CRITICAL", "BLOCK_WITHDRAWAL",
                List.of("HIGH_RISK_WALLET"), LocalDateTime.now(), LocalDateTime.now());

        //when
        auditEventConsumer.consumeRiskEvaluationCompleted(event, "1", acknowledgment);

        //then
        verify(auditEventLogService).saveIfNotExists(event.eventId(), "RiskEvaluationCompletedEvent", KafkaTopicConfig.RISK_EVALUATION_COMPLETED, "1", event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("risk.case.created 이벤트를 감사 로그로 저장하고 ack 처리한다")
    void case3() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-case-001", 100L, 10L, 10001L,
                "AML_REVIEW", "REVIEW_REQUIRED", "CRITICAL", LocalDateTime.now(), LocalDateTime.now()
        );

        //when
        auditEventConsumer.consumeRiskCaseCreated(event, "100", acknowledgment);

        //then
        verify(auditEventLogService).saveIfNotExists(event.eventId(), "RiskCaseCreatedEvent", KafkaTopicConfig.RISK_CASE_CREATED, "100", event);
        verify(acknowledgment).acknowledge();
    }
}