package com.example.digital_asset_risk_platform.event.producer;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishWithdrawalRequested(WithdrawalRequestedEvent event) {
        kafkaTemplate.send(
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
        kafkaTemplate.send(
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
        kafkaTemplate.send(
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
}
