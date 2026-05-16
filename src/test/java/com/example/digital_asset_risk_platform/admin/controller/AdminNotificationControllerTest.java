package com.example.digital_asset_risk_platform.admin.controller;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.application.AdminNotificationService;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
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
class AdminNotificationControllerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminNotificationService adminNotificationService;

    @Autowired
    AdminNotificationRepository adminNotificationRepository;

    @BeforeEach
    void setUp() {
        adminNotificationRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자 알림 목록을 조회한다")
    void case1() throws Exception {
        //given
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-001", 1L));

        //when&then
        mockMvc.perform(get("/api/admin/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventId").value("event-001"))
                .andExpect(jsonPath("$.content[0].readYn").value("N"));
    }

    @Test
    @DisplayName("읽지 않은 관리자 알림 개수를 조회한다")
    void case2() throws Exception {
        //given
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-001", 1L));

        //when&then
        mockMvc.perform(get("/api/admin/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));
    }

    @Test
    @DisplayName("관리자 알림을 읽음 처리한다")
    void case3() throws Exception {
        //given
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-001", 1L));

        Long notificationId = adminNotificationRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/admin/notifications/{notificationId}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId))
                .andExpect(jsonPath("$.readYn").value("Y"))
                .andExpect(jsonPath("$.readAt").exists());
    }

    private RiskCaseCreatedEvent riskCaseCreatedEvent(String eventId, Long caseId) {
        return new RiskCaseCreatedEvent(
                eventId,
                caseId,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}