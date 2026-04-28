package com.example.digital_asset_risk_platform.account.application;

import com.example.digital_asset_risk_platform.account.domain.AccountLoginEvent;
import com.example.digital_asset_risk_platform.account.domain.AccountSecurityEvent;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountEventService {

    private final AccountLoginEventRepository loginEventRepository;
    private final AccountSecurityEventRepository securityEventRepository;

    public Long createLoginEvent(LoginEventCreateRequest request) {
        boolean alreadyUsed = loginEventRepository.existsByUserIdAndDeviceId(request.userId(), request.deviceId());

        AccountLoginEvent event = new AccountLoginEvent(
                request.userId(),
                request.deviceId(),
                request.ipAddress(),
                request.countryCode(),
                request.userAgent(),
                !alreadyUsed,
                request.loginAt()
        );

        return loginEventRepository.save(event).getId();
    }

    public Long createSecurityEvent(SecurityEventCreateRequest request) {
        AccountSecurityEvent event = new AccountSecurityEvent(
                request.userId(),
                request.eventType(),
                request.deviceId(),
                request.ipAddress(),
                request.eventAt()
        );

        return securityEventRepository.save(event).getId();
    }
}
