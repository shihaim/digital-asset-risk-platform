package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.application.AdminNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminNotificationConsumer {

    private final AdminNotificationService adminNotificationService;

    @KafkaListener(
            topics = KafkaTopicConfig.RISK_CASE_CREATED,
            groupId = "admin-notification-consumer",
            containerFactory = "riskCaseCreatedKafkaListenerContainerFactory"
    )
    public void consume(RiskCaseCreatedEvent event, Acknowledgment acknowledgment) {
        log.info(
                "Kafka message consumed. consumer=admin-notification-consumer, eventId={}, caseId={}, userId={}",
                event.eventId(),
                event.caseId(),
                event.userId()
        );

        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);
        acknowledgment.acknowledge();

        log.info(
                "Kafka message acknowledged. consumer=admin-notification-consumer, eventId={}, caseId={}",
                event.eventId(),
                event.caseId()
        );
    }
}
