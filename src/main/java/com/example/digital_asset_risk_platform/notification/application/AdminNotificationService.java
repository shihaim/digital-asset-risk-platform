package com.example.digital_asset_risk_platform.notification.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.domain.AdminNotification;
import com.example.digital_asset_risk_platform.notification.dto.AdminNotificationResponse;
import com.example.digital_asset_risk_platform.notification.dto.AdminNotificationUnreadCountResponse;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminNotificationRepository adminNotificationRepository;

    public void createCaseCreatedNotificationIfNotExists(RiskCaseCreatedEvent event) {
        if (adminNotificationRepository.existsByEventId(event.eventId())) {
            return;
        }

        AdminNotification notification = new AdminNotification(
                event.eventId(), event.caseId(), event.userId(),
                "RISK_CASE_CREATED", "위험 Case가 생성되었습니다.",
                "Case ID " + event.caseId() + " / 사용자 " + event.userId() + " / 위험도 " + event.riskLevel() + " / 유형 " + event.caseType());

        adminNotificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<AdminNotificationResponse> getNotifications(String readYn, Pageable pageable) {
        Page<AdminNotification> notifications;

        if (readYn == null || readYn.isBlank()) {
            notifications = adminNotificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            notifications = adminNotificationRepository.findByReadYnOrderByCreatedAtDesc(readYn, pageable);
        }

        return notifications.map(AdminNotificationResponse::from);
    }

    @Transactional(readOnly = true)
    public AdminNotificationUnreadCountResponse getUnreadCount() {
        long unreadCount = adminNotificationRepository.countByReadYn("N");
        return new AdminNotificationUnreadCountResponse(unreadCount);
    }

    public AdminNotificationResponse markAsRead(Long notificationId) {
        AdminNotification notification = adminNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOTIFICATION_NOT_FOUND));

        notification.markAsRead();

        log.info(
                "Admin notification marked as read. notificationId={}, caseId={}, userId={}",
                notification.getId(),
                notification.getCaseId(),
                notification.getUserId()
        );

        return AdminNotificationResponse.from(notification);
    }
}
