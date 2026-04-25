package com.example.digital_asset_risk_platform.account.repository;

import com.example.digital_asset_risk_platform.account.domain.AccountLoginEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountLoginEventRepository extends JpaRepository<AccountLoginEvent, Long> {
    List<AccountLoginEvent> findByUserIdAndLoginAtAfter(Long userId, LocalDateTime loginAt);

    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);

}
