package com.example.digital_asset_risk_platform.account.repository;

import com.example.digital_asset_risk_platform.account.domain.AccountSecurityEvent;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountSecurityEventRepository extends JpaRepository<AccountSecurityEvent, Long> {

    List<AccountSecurityEvent> findByUserIdAndEventAtAfter(Long userId, LocalDateTime eventAt);

    boolean existsByUserIdAndEventTypeAndEventAtAfter(Long userId, SecurityEventType eventType, LocalDateTime eventAt);
}
