package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.application.AdminNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdminNotificationConsumerTest {

    private final AdminNotificationService adminNotificationService = mock(AdminNotificationService.class);
    private final AdminNotificationConsumer adminNotificationConsumer = new AdminNotificationConsumer(adminNotificationService);
    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @Test
    @DisplayName("RiskCase 생성 이벤트를 소비하면 관리자 알림을 생성하고 ack 처리한다")
    void case1() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-case-001", 100L, 10L, 10001L,
                "AML_REVIEW", "REVIEW_REQUIRED", "CRITICAL", LocalDateTime.now(), LocalDateTime.now());

        //when
        adminNotificationConsumer.consume(event, acknowledgment);

        //then
        verify(adminNotificationService).createCaseCreatedNotificationIfNotExists(event);
        verify(acknowledgment).acknowledge();
    }
}