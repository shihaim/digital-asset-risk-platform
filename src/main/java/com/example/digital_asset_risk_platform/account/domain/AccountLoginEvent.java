package com.example.digital_asset_risk_platform.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "account_login_event",
        indexes = {
                @Index(name = "idx_login_user_time", columnList = "user_id, login_at")
        }
)
public class AccountLoginEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "new_device_yn", nullable = false, length = 1)
    private String newDeviceYn;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AccountLoginEvent(
            Long userId,
            String deviceId,
            String ipAddress,
            String countryCode,
            String userAgent,
            boolean newDevice,
            LocalDateTime loginAt
    ) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.countryCode = countryCode;
        this.userAgent = userAgent;
        this.newDeviceYn = newDevice ? "Y" : "N";
        this.loginAt = loginAt;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isNewDevice() {
        return "Y".equals(this.newDeviceYn);
    }
}
