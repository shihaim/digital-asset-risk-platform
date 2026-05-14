package com.example.digital_asset_risk_platform.outbox.dto;

import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;

import java.time.LocalDateTime;

public record OutboxEventResponse(
        Long id,
        String eventId,
        String eventType,
        String topicName,
        String messageKey,
        OutboxEventStatus status,
        int retryCount,
        String lastErrorMessage,
        LocalDateTime occurredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime sentAt
) {
    public static OutboxEventResponse from(OutboxEvent event) {
        return new OutboxEventResponse(
                event.getId(),
                event.getEventId(),
                event.getEventType(),
                event.getTopicName(),
                event.getMessageKey(),
                event.getStatus(),
                event.getRetryCount(),
                event.getLastErrorMessage(),
                event.getOccurredAt(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getSentAt()
        );
    }
}
