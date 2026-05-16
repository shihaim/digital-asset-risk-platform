package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.notification.application.AdminNotificationService;
import com.example.digital_asset_risk_platform.notification.dto.AdminNotificationResponse;
import com.example.digital_asset_risk_platform.notification.dto.AdminNotificationUnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping
    public ResponseEntity<Page<AdminNotificationResponse>> getNotifications(@RequestParam(required = false) String readYn, Pageable pageable) {
        return ResponseEntity.ok(adminNotificationService.getNotifications(readYn, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<AdminNotificationUnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(adminNotificationService.getUnreadCount());
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<AdminNotificationResponse> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(adminNotificationService.markAsRead(notificationId));
    }
}
