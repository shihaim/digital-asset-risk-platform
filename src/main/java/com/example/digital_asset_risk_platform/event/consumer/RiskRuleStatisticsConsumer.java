package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.statistics.application.RiskRuleStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RiskRuleStatisticsConsumer {

    private static final String CONSUMER_NAME = "rule-statistics-consumer";

    private final RiskRuleStatisticsService riskRuleStatisticsService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(
            topics = KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
            groupId = CONSUMER_NAME,
            containerFactory = "riskEvaluationCompletedKafkaListenerContainerFactory"
    )
    public void consume(RiskEvaluationCompletedEvent event, Acknowledgment acknowledgment) {
        if (processedEventService.isProcessed(CONSUMER_NAME, event.eventId())) {
            acknowledgment.acknowledge();
            return;
        }
        
        event.ruleCodes().forEach(ruleCode -> riskRuleStatisticsService.increaseRuleHit(ruleCode, event.evaluatedAt()));

        processedEventService.markProcessed(CONSUMER_NAME, event.eventId());

        acknowledgment.acknowledge();
    }
}
