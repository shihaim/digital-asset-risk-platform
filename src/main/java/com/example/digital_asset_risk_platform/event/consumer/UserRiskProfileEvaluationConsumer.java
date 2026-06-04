package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.profile.application.UserRiskProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRiskProfileEvaluationConsumer {

    private static final String CONSUMER_NAME = "user-risk-profile-evaluation-consumer";

    private final UserRiskProfileService userRiskProfileService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(
            topics = KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
            groupId = "${app.kafka.consumer.group.user-risk-profile-evaluation:user-risk-profile-evaluation-consumer}",
            containerFactory = "riskEvaluationCompletedKafkaListenerContainerFactory"
    )
    public void consume(RiskEvaluationCompletedEvent event, Acknowledgment acknowledgment) {
        if (processedEventService.isProcessed(CONSUMER_NAME, event.eventId())) {
            log.info(
                    "RiskEvaluationCompletedEvent already processed for user risk profile. eventId={}",
                    event.eventId()
            );
            acknowledgment.acknowledge();
            return;
        }

        boolean blocked = RiskDecisionType.BLOCK_WITHDRAWAL.name().equals(event.decision());

        userRiskProfileService.updateFromEvaluate(event.userId(), event.totalScore(), blocked);

        processedEventService.markProcessed(CONSUMER_NAME, event.eventId());
        acknowledgment.acknowledge();

        log.info(
                "RiskEvaluationCompletedEvent processed for user risk profile. eventId={}, userId={}",
                event.eventId(),
                event.userId()
        );
    }
}
