package com.example.digital_asset_risk_platform.notification.application;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.dto.AdminNotificationResponse;
import com.example.digital_asset_risk_platform.notification.dto.AdminNotificationUnreadCountResponse;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminNotificationServiceTest extends IntegrationTestSupport {

    @Autowired
    AdminNotificationService adminNotificationService;

    @Autowired
    AdminNotificationRepository adminNotificationRepository;

    @BeforeEach
    void setUp() {
        adminNotificationRepository.deleteAll();
    }

    @Test
    @DisplayName("RiskCase 생성 이벤트로 관리자 알림을 생성한다")
    void case1() {
        //given
        RiskCaseCreatedEvent event = riskCaseCreatedEvent("event-case-001", 1L);

        //when
        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);

        //then
        assertThat(adminNotificationRepository.count()).isOne();
    }

    @Test
    @DisplayName("같은 eventId의 관리자 알림은 중복 생성하지 않는다")
    void case2() {
        //given
        RiskCaseCreatedEvent event = riskCaseCreatedEvent("event-case-duplicate", 1L);

        //when
        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);
        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);

        //then
        assertThat(adminNotificationRepository.count()).isOne();
    }

    @Test
    @DisplayName("관리자 알림 목록을 조회한다")
    void case3() {
        //given
        RiskCaseCreatedEvent event = riskCaseCreatedEvent("event-001", 1L);

        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);

        //when
        Page<AdminNotificationResponse> result = adminNotificationService.getNotifications(null, PageRequest.of(0, 10));

        //then
        assertThat(result.getTotalElements()).isOne();
        assertThat(result.getContent().get(0).eventId()).isEqualTo("event-001");
        assertThat(result.getContent().get(0).readYn()).isEqualTo("N");
    }

    @Test
    @DisplayName("읽지 않은 관리자 알림 개수를 조회한다")
    void case4() {
        //given
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-001", 1L));
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-002", 2L));

        //when
        AdminNotificationUnreadCountResponse response = adminNotificationService.getUnreadCount();

        //then
        assertThat(response.unreadCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("관리자 알림을 읽음 처리한다")
    void case5() {
        //given
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-001", 1L));

        //when
        Long notificationId = adminNotificationRepository.findAll().get(0).getId();

        AdminNotificationResponse response = adminNotificationService.markAsRead(notificationId);

        //then
        assertThat(response.readYn()).isEqualTo("Y");
        assertThat(response.readAt()).isNotNull();
        assertThat(adminNotificationService.getUnreadCount().unreadCount()).isZero();
    }

    @Test
    @DisplayName("읽지 않은 알림만 조회한다")
    void case6() {
        //given
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-001", 1L));
        adminNotificationService.createCaseCreatedNotificationIfNotExists(riskCaseCreatedEvent("event-002", 2L));

        //when
        Long notificationId = adminNotificationRepository.findAll().get(0).getId();
        adminNotificationService.markAsRead(notificationId);

        Page<AdminNotificationResponse> result = adminNotificationService.getNotifications("N", PageRequest.of(0, 10));

        //then
        assertThat(result.getTotalElements()).isOne();
        assertThat(result.getContent().get(0).readYn()).isEqualTo("N");
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
