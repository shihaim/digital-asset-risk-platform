package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventDetailResponse;
import com.example.digital_asset_risk_platform.outbox.dto.OutboxEventSummaryResponse;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

class OutboxAdminServiceTest extends IntegrationTestSupport {

    @Autowired
    OutboxAdminService outboxAdminService;

    @Autowired
    OutboxEventService outboxEventService;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Transactional
    @Test
    @DisplayName("Outbox 이벤트 상태별 요약 정보를 조회한다")
    void case1() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-pending",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        outboxEventService.save(event);

        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEvent.markProcessing();
        outboxEvent.markFailed("fail", 5);

        //when
        OutboxEventSummaryResponse summary = outboxAdminService.getSummary();

        //then
        Assertions.assertThat(summary.pendingCount()).isZero();
        Assertions.assertThat(summary.failedCount()).isOne();
        Assertions.assertThat(summary.deadCount()).isZero();
    }

    @Transactional
    @Test
    @DisplayName("FAILED 상태 Outbox 이벤트를 수동 재처리하면 PENDING 상태로 변경된다")
    void case2() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-failed",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        outboxEventService.save(event);

        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEvent.markProcessing();
        outboxEvent.markFailed("fail", 5);

        //when
        OutboxEventDetailResponse response = outboxAdminService.retry(outboxEvent.getEventId());

        //then
        Assertions.assertThat(response.status()).isEqualTo(OutboxEventStatus.PENDING);
        Assertions.assertThat(response.lastErrorMessage()).isNull();
    }

    @Transactional
    @Test
    @DisplayName("DEAD 상태 Outbox 이벤트를 수동 재처리하면 PENDING 상태로 변경된다")
    void case3() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-dead",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        outboxEventService.save(event);

        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        for (int i = 0; i < 5; i++) {
            outboxEvent.markProcessing();
            outboxEvent.markFailed("fail", 5);
        }

        //when
        OutboxEventDetailResponse response = outboxAdminService.retry(outboxEvent.getEventId());

        //then
        Assertions.assertThat(response.status()).isEqualTo(OutboxEventStatus.PENDING);
        Assertions.assertThat(response.lastErrorMessage()).isNull();
    }
}