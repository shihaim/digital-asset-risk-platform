package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.audit.application.AuditEventLogService;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventConsumer {

    private final AuditEventLogService auditEventLogService;

    @KafkaListener(
            topics = KafkaTopicConfig.WITHDRAWAL_REQUESTED,
            groupId = "audit-log-consumer",
            containerFactory = "withdrawalRequestedKafkaListenerContainerFactory"
    )
    public void consumeWithdrawalRequested(WithdrawalRequestedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key, Acknowledgment acknowledgment) {
        auditEventLogService.saveIfNotExists(event.eventId(), "WithdrawalRequestedEvent", KafkaTopicConfig.WITHDRAWAL_REQUESTED, key, event);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
            groupId = "audit-log-consumer",
            containerFactory = "riskEvaluationCompletedKafkaListenerContainerFactory"
    )
    public void consumeRiskEvaluationCompleted(RiskEvaluationCompletedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key, Acknowledgment acknowledgment) {
        auditEventLogService.saveIfNotExists(event.eventId(), "RiskEvaluationCompletedEvent", KafkaTopicConfig.RISK_EVALUATION_COMPLETED, key, event);

        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics = KafkaTopicConfig.RISK_CASE_CREATED,
            groupId = "audit-log-consumer",
            containerFactory = "riskCaseCreatedKafkaListenerContainerFactory"
    )
    public void consumeRiskCaseCreated(RiskCaseCreatedEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key, Acknowledgment acknowledgment) {
        auditEventLogService.saveIfNotExists(event.eventId(), "RiskCaseCreatedEvent", KafkaTopicConfig.RISK_CASE_CREATED, key, event);

        acknowledgment.acknowledge();
    }
}
