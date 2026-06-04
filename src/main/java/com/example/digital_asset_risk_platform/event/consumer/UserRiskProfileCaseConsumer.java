package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.risk.profile.application.UserRiskProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRiskProfileCaseConsumer {

    private static final String CONSUMER_NAME = "user-risk-profile-case-consumer";

    private final UserRiskProfileService userRiskProfileService;
    private final ProcessedEventService processedEventService;

    @KafkaListener(
            topics = KafkaTopicConfig.RISK_CASE_CREATED,
            groupId = "${app.kafka.consumer.group.user-risk-profile-case:user-risk-profile-case-consumer}",
            containerFactory = "riskCaseCreatedKafkaListenerContainerFactory"
    )
    public void consume(RiskCaseCreatedEvent event, Acknowledgment acknowledgment) {
        if (processedEventService.isProcessed(CONSUMER_NAME, event.eventId())) {
            log.info(
                    "RiskCaseCreatedEvent already processed for user risk profile. eventId={}",
                    event.eventId()
            );
            acknowledgment.acknowledge();
            return;
        }

        userRiskProfileService.increaseCaseCount(event.userId());

        processedEventService.markProcessed(CONSUMER_NAME, event.eventId());
        acknowledgment.acknowledge();

        log.info(
                "RiskCaseCreatedEvent processed for user risk profile. eventId={}, userId={}",
                event.eventId(),
                event.userId()
        );
    }
}
