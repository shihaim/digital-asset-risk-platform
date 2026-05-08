package com.example.digital_asset_risk_platform.notification.repository;

import com.example.digital_asset_risk_platform.notification.domain.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    boolean existsByEventId(String eventId);
}
