package com.example.digital_asset_risk_platform.outbox.repository;

import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(List<OutboxEventStatus> statuses, int retryCount, Pageable pageable);

    boolean existsByEventId(String eventId);
}
