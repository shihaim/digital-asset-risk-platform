package com.example.digital_asset_risk_platform.audit.repository;

import com.example.digital_asset_risk_platform.audit.domain.AuditEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventLogRepository extends JpaRepository<AuditEventLog, Long> {

    boolean existsByEventId(String eventId);
}
