package com.example.digital_asset_risk_platform.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "audit_event_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_audit_event_id", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_audit_event_type_time", columnList = "event_type, consumed_at"),
                @Index(name = "idx_audit_topic_time", columnList = "topic_name, consumed_at")
        }
)
public class AuditEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "topic_name", nullable = false, length = 100)
    private String topicName;

    @Column(name = "message_key", length = 100)
    private String messageKey;

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AuditEventLog(String eventId, String eventType, String topicName, String messageKey, String payloadJson) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.topicName = topicName;
        this.messageKey = messageKey;
        this.payloadJson = payloadJson;
        this.consumedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
