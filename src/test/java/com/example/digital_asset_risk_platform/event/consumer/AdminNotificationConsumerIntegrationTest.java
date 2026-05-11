package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
public class AdminNotificationConsumerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    AdminNotificationConsumer adminNotificationConsumer;

    @Autowired
    AdminNotificationRepository adminNotificationRepository;

    @BeforeEach
    void setUp() {
        adminNotificationRepository.deleteAll();
    }

    @Test
    @DisplayName("RiskCase 생성 이벤트 소비 시 관리자 알림을 DB에 저장한다")
    void case1() {
        //given
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent("event-case-001", 100L, 10L, 10001L, "AML_REVIEW", "REVIEW_REQUIRED", "CRITICAL", LocalDateTime.now(), LocalDateTime.now());

        //when
        adminNotificationConsumer.consume(event, acknowledgment);

        //then
        Assertions.assertThat(adminNotificationRepository.count()).isOne();
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("같은 RiskCase 생성 이벤트를 두 번 소비해도 관리자 알림은 하나만 저장한다")
    void case2() {
        //given
        Acknowledgment acknowledgment = mock(Acknowledgment.class);

        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent("event-case-duplicate", 100L, 10L, 10001L, "AML_REVIEW", "REVIEW_REQUIRED", "CRITICAL", LocalDateTime.now(), LocalDateTime.now());

        //when
        adminNotificationConsumer.consume(event, acknowledgment);
        adminNotificationConsumer.consume(event, acknowledgment);

        //then
        Assertions.assertThat(adminNotificationRepository.count()).isOne();
    }
}
