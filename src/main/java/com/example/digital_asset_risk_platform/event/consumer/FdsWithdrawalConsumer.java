package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.risk.application.FdsWithdrawalEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "fds.evaluation.mode", havingValue = "async")
public class FdsWithdrawalConsumer {

    private final FdsWithdrawalEvaluationService fdsWithdrawalEvaluationService;

    @KafkaListener(
            topics = KafkaTopicConfig.WITHDRAWAL_REQUESTED,
            groupId = "fds-withdrawal-consumer",
            containerFactory = "withdrawalRequestedKafkaListenerContainerFactory"
    )
    public void consume(WithdrawalRequestedEvent event, Acknowledgment acknowledgment) {
        log.info(
                "Kafka message consumed. consumer=fds-withdrawal-consumer, eventId={}, withdrawalId={}",
                event.eventId(),
                event.withdrawalId()
        );

        fdsWithdrawalEvaluationService.evaluate(event);
        acknowledgment.acknowledge();

        log.info(
                "Kafka message acknowledged. consumer=fds-withdrawal-consumer, eventId={}, withdrawalId={}",
                event.eventId(),
                event.withdrawalId()
        );
    }
}
