package com.example.digital_asset_risk_platform.event.producer;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEventProducer {

    @Qualifier("objectKafkaTemplate")
    private final KafkaTemplate<String, Object> objectKafkaTemplate;
    @Qualifier("stringKafkaTemplate")
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    public void publishWithdrawalRequested(WithdrawalRequestedEvent event) {
        objectKafkaTemplate.send(
                KafkaTopicConfig.WITHDRAWAL_REQUESTED,
                String.valueOf(event.withdrawalId()),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish WithdrawalRequestedEvent. eventId={}", event.eventId(), ex);
                return;
            }

             log.info("Published WithdrawalRequestedEvent. eventId={}, topic={}, partition={}, offset={}",
                     event.eventId(),
                     result.getRecordMetadata().topic(),
                     result.getRecordMetadata().partition(),
                     result.getRecordMetadata().offset()
             );
        });
    }

    public void publishRiskEvaluationCompleted(RiskEvaluationCompletedEvent event) {
        objectKafkaTemplate.send(
                KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
                String.valueOf(event.refId()),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish RiskEvaluationCompletedEvent. eventId={}", event.eventId(), ex);
                return;
            }

            log.info("Published RiskEvaluationCompletedEvent. eventId={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }

    public void publishRiskCaseCreated(RiskCaseCreatedEvent event) {
        objectKafkaTemplate.send(
                KafkaTopicConfig.RISK_CASE_CREATED,
                String.valueOf(event.caseId()),
                event
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish RiskCaseCreatedEvent. eventId={}", event.eventId(), ex);
                return;
            }

            log.info("Published RiskCaseCreatedEvent. eventId={}, topic={}, partition={}, offset={}",
                    event.eventId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }

    public CompletableFuture<SendResult<String, String>> publishRawJson(String topicName, String messageKey, String payloadJson) {
        return stringKafkaTemplate.send(topicName, messageKey, payloadJson);
    }
}
