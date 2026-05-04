package com.example.digital_asset_risk_platform.notification.applicaiton;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.domain.AdminNotification;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
