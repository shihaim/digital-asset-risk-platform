package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.risk.application.FdsWithdrawalEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
        fdsWithdrawalEvaluationService.evaluate(event);
        acknowledgment.acknowledge();
    }
}
