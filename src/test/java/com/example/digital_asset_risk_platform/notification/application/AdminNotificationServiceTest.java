package com.example.digital_asset_risk_platform.notification.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@ActiveProfiles("test")
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
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-case-001", 1L, 10L, 10001L,
                "AML_REVIEW", "REVIEW_REQUIRED", "CRITICAL", LocalDateTime.now(), LocalDateTime.now());

        //when
        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);

        //then
        Assertions.assertThat(adminNotificationRepository.count()).isOne();
    }

    @Test
    @DisplayName("같은 eventId의 관리자 알림은 중복 생성하지 않는다")
    void case2() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-case-duplicate", 1L, 10L, 10001L,
                "AML_REVIEW", "REVIEW_REQUIRED", "CRITICAL", LocalDateTime.now(), LocalDateTime.now());

        //when
        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);
        adminNotificationService.createCaseCreatedNotificationIfNotExists(event);

        //then
        Assertions.assertThat(adminNotificationRepository.count()).isOne();
    }
}
