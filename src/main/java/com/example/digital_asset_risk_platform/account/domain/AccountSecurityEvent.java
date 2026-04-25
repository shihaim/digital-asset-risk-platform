package com.example.digital_asset_risk_platform.account.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "account_security_event",
        indexes = {
                @Index(name = "idx_security_user_time", columnList = "user_id, event_at")
        }
)
public class AccountSecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SecurityEventType eventType;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "event_at", nullable = false)
    private LocalDateTime eventAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AccountSecurityEvent(
            Long userId,
            SecurityEventType eventType,
            String deviceId,
            String ipAddress,
            LocalDateTime eventAt
    ) {
        this.userId = userId;
        this.eventType = eventType;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.eventAt = eventAt;
        this.createdAt = LocalDateTime.now();
    }
}
