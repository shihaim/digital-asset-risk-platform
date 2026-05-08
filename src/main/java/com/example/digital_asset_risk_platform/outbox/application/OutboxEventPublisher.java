package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.event.producer.RiskEventProducer;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final RiskEventProducer riskEventProducer;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:3000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(
                List.of(OutboxEventStatus.PENDING, OutboxEventStatus.FAILED),
                MAX_RETRY_COUNT,
                PageRequest.of(0, BATCH_SIZE));

        for (OutboxEvent event : events) {
            try {
                event.markProcessing();

                riskEventProducer.publishRawJson(event.getTopicName(), event.getMessageKey(), event.getPayloadJson()).get();

                event.markSent();
            } catch (Exception e) {
                log.error("Failed to publish {}. messageKey={}", event.getTopicName(), event.getMessageKey(), e);
                event.markFailed(e.getMessage());
            }
        }
    }
}
