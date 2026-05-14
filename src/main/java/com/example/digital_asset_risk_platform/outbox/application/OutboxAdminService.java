package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventDetailResponse;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventResponse;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventSummaryResponse;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboxAdminService {

    private final OutboxEventRepository outboxEventRepository;

    public OutboxEventSummaryResponse getSummary() {
        return new OutboxEventSummaryResponse(
                outboxEventRepository.countByStatus(OutboxEventStatus.PENDING),
                outboxEventRepository.countByStatus(OutboxEventStatus.PROCESSING),
                outboxEventRepository.countByStatus(OutboxEventStatus.SENT),
                outboxEventRepository.countByStatus(OutboxEventStatus.FAILED),
                outboxEventRepository.countByStatus(OutboxEventStatus.DEAD)
        );
    }

    public Page<OutboxEventResponse> getEvents(OutboxEventStatus status, Pageable pageable) {
        return outboxEventRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(OutboxEventResponse::from);
    }

    public OutboxEventDetailResponse getEventDetail(String eventId) {
        OutboxEvent outboxEvent = outboxEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_EVENT_NOT_FOUND));

        return OutboxEventDetailResponse.from(outboxEvent);
    }

    @Transactional
    public OutboxEventDetailResponse retry(String eventId) {
        OutboxEvent outboxEvent = outboxEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OUTBOX_EVENT_NOT_FOUND));

        OutboxEventStatus previousStatus = outboxEvent.getStatus();

        outboxEvent.retryManually();

        log.info(
                "Outbox event manually retried. eventId={}, eventType={}, previousStatus={}, newStatus={}, retryCount={}",
                outboxEvent.getEventId(),
                outboxEvent.getEventType(),
                previousStatus,
                outboxEvent.getStatus(),
                outboxEvent.getRetryCount()
        );

        return OutboxEventDetailResponse.from(outboxEvent);
    }
}
