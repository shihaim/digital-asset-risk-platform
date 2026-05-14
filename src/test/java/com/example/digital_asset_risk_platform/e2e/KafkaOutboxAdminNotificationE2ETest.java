package com.example.digital_asset_risk_platform.e2e;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.notification.repository.AdminNotificationRepository;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventPublisher;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventService;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.support.KafkaIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaOutboxAdminNotificationE2ETest extends KafkaIntegrationTestSupport {

    @Autowired
    OutboxEventService outboxEventService;

    @Autowired
    OutboxEventPublisher outboxEventPublisher;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    AdminNotificationRepository adminNotificationRepository;

    @BeforeEach
    void setUp() {
        adminNotificationRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Outbox의 RiskCaseCreatedEvent를 Kafka로 발행하면 AdminNotificationConsumer가 소비하여 알림을 저장한다")
    void case1() {
        //given
        RiskCaseCreatedEvent event = new RiskCaseCreatedEvent(
                "event-case-kafka-e2e-001",
                100L,
                10L,
                10001L,
                "AML_REVIEW",
                "REVIEW_REQUIRED",
                "CRITICAL",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        outboxEventService.save(event);

        assertThat(outboxEventRepository.count()).isOne();

        OutboxEvent pendingEvent = outboxEventRepository.findAll().get(0);
        assertThat(pendingEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(pendingEvent.getTopicName()).isEqualTo(KafkaTopicConfig.RISK_CASE_CREATED);

        //when
        outboxEventPublisher.publishPendingEvents();

        //then
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    assertThat(adminNotificationRepository.count()).isOne();

                    OutboxEvent sentEvent = outboxEventRepository.findAll().get(0);
                    assertThat(sentEvent.getStatus()).isEqualTo(OutboxEventStatus.SENT);
                    assertThat(sentEvent.getSentAt()).isNotNull();
                });
    }
}
