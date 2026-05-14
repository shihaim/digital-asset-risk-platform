package com.example.digital_asset_risk_platform.outbox.repository;

import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusInAndRetryCountLessThanOrderByCreatedAtAsc(List<OutboxEventStatus> statuses, int retryCount, Pageable pageable);

    Page<OutboxEvent> findByStatusOrderByCreatedAtDesc(OutboxEventStatus status, Pageable pageable);

    Optional<OutboxEvent> findByEventId(String eventId);

    boolean existsByEventId(String eventId);

    long countByStatus(OutboxEventStatus status);
}
