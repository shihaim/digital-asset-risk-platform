package com.example.digital_asset_risk_platform.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "outbox_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_outbox_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_outbox_status_created", columnList = "status, created_at"),
                @Index(name = "idx_outbox_topic_status", columnList = "topic_name, status")
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "message_key", nullable = false, length = 100)
    private String messageKey;

    @Lob
    @Column(name = "payload_json", nullable = false, columnDefinition = "LONGTEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OutboxEventStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error_message", length = 2000)
    private String lastErrorMessage;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public OutboxEvent(String eventId, String eventType, String topicName, String messageKey, String payloadJson, LocalDateTime occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.topicName = topicName;
        this.messageKey = messageKey;
        this.payloadJson = payloadJson;
        this.occurredAt = occurredAt;
        this.status = OutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    ///========================================///
    public void markProcessing() {
        this.status = OutboxEventStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSent() {
        this.status = OutboxEventStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastErrorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = OutboxEventStatus.FAILED;
        this.retryCount++;
        this.lastErrorMessage = trimErrorMessage(errorMessage);
        this.updatedAt = LocalDateTime.now();
    }

    public void retry() {
        this.status = OutboxEventStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    private String trimErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        if (errorMessage.length() <= 2000) {
            return errorMessage;
        }

        return errorMessage.substring(0, 2000);
    }
}
