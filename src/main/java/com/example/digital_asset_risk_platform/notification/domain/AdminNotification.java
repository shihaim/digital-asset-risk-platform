package com.example.digital_asset_risk_platform.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "admin_notification",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_notification_event", columnNames = "event_id")
        },
        indexes = {
                @Index(name = "idx_admin_notification_read_time", columnList = "read_yn, created_at"),
                @Index(name = "idex_admin_notification_case", columnList = "case_id")
        }
)
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "read_yn", nullable = false, length = 1)
    private String readYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public AdminNotification(String eventId, Long caseId, Long userId, String notificationType, String title, String message) {
        this.eventId = eventId;
        this.caseId = caseId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.readYn = "N";
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        if ("Y".equals(this.readYn)) {
            return;
        }

        this.readYn = "Y";
        this.readAt = LocalDateTime.now();
    }
}
