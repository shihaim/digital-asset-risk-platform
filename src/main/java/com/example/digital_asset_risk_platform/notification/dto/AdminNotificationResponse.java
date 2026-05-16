package com.example.digital_asset_risk_platform.notification.dto;

import com.example.digital_asset_risk_platform.notification.domain.AdminNotification;

import java.time.LocalDateTime;

public record AdminNotificationResponse(
        Long notificationId,
        String eventId,
        Long caseId,
        Long userId,
        String notificationType,
        String title,
        String message,
        String readYn,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
    public static AdminNotificationResponse from(AdminNotification notification) {
        return new AdminNotificationResponse(
                notification.getId(),
                notification.getEventId(),
                notification.getCaseId(),
                notification.getUserId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReadYn(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
