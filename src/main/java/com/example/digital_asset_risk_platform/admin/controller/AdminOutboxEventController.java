package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.outbox.application.OutboxAdminService;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventDetailResponse;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventResponse;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/outbox-events")
@RequiredArgsConstructor
public class AdminOutboxEventController {

    private final OutboxAdminService outboxAdminService;

    @GetMapping("/summary")
    public ResponseEntity<OutboxEventSummaryResponse> summary() {
        return ResponseEntity.ok(outboxAdminService.getSummary());
    }

    @GetMapping
    public ResponseEntity<Page<OutboxEventResponse>> getEvents(@RequestParam OutboxEventStatus status, Pageable pageable) {
        return ResponseEntity.ok(outboxAdminService.getEvents(status, pageable));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<OutboxEventDetailResponse> getEventDetail(@PathVariable String eventId) {
        return ResponseEntity.ok(outboxAdminService.getEventDetail(eventId));
    }

    @PostMapping("/{eventId}/retry")
    public ResponseEntity<OutboxEventDetailResponse> retry(@PathVariable String eventId) {
        return ResponseEntity.ok(outboxAdminService.retry(eventId));
    }
}
