package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventService;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminOutboxEventControllerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OutboxEventService outboxEventService;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Outbox 이벤트 상태 요약을 조회한다")
    void case1() throws Exception {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-summary",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        outboxEventService.save(event);

        //then
        mockMvc.perform(get("/api/admin/outbox-events/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingCount").value(1))
                .andExpect(jsonPath("$.failedCount").value(0))
                .andExpect(jsonPath("$.deadCount").value(0));
    }

    @Test
    @DisplayName("상태별 Outbox 이벤트 목록을 조회한다")
    void case2() throws Exception {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-list",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        outboxEventService.save(event);

        //then
        mockMvc.perform(get("/api/admin/outbox-events")
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventId").value("event-list"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Outbox 이벤트 상세를 조회한다")
    void case3() throws Exception {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-detail",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        outboxEventService.save(event);

        //then
        mockMvc.perform(get("/api/admin/outbox-events/{eventId}", "event-detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("event-detail"))
                .andExpect(jsonPath("$.payloadJson").exists());
    }

    @Test
    @DisplayName("FAILED Outbox 이벤트를 수동 재처리한다")
    void case4() throws Exception {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-retry",
                1L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        outboxEventService.save(event);

        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEvent.markProcessing();
        outboxEvent.markFailed("fail", 5);
        outboxEventRepository.saveAndFlush(outboxEvent);

        //then
        mockMvc.perform(post("/api/admin/outbox-events/{eventId}/retry", "event-retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("event-retry"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.lastErrorMessage").doesNotExist());
    }
}