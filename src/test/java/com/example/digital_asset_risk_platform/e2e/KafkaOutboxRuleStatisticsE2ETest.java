package com.example.digital_asset_risk_platform.e2e;

import com.example.digital_asset_risk_platform.audit.repository.AuditEventLogRepository;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventPublisher;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventService;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import com.example.digital_asset_risk_platform.support.KafkaIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=true",
        "fds.evaluation.mode=async"
})
@ActiveProfiles("test")
public class KafkaOutboxRuleStatisticsE2ETest extends KafkaIntegrationTestSupport {

    @Autowired
    OutboxEventService outboxEventService;

    @Autowired
    OutboxEventPublisher outboxEventPublisher;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    @Autowired
    AuditEventLogRepository auditEventLogRepository;

    @Autowired
    ConsumerProcessedEventRepository consumerProcessedEventRepository;

    @BeforeEach
    void setUp() {
        consumerProcessedEventRepository.deleteAll();
        riskRuleStatisticsRepository.deleteAll();
        auditEventLogRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Outbox의 RiskEvaluationCompletedEvent를 Kafka로 발행하면 Rule 통계와 감사 로그가 저장된다")
    void case1() {
        //given
        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-evaluation-kafka-e2e-001",
                10L,
                "WITHDRAWAL",
                1L,
                10001L,
                190,
                "CRITICAL",
                "BLOCK_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "OTP_RESET_WITHDRAWAL", "HIGH_RISK_WALLET"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        outboxEventService.save(event);

        assertThat(outboxEventRepository.count()).isOne();

        OutboxEvent pendingEvent = outboxEventRepository.findAll().get(0);
        assertThat(pendingEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(pendingEvent.getTopicName()).isEqualTo(KafkaTopicConfig.RISK_EVALUATION_COMPLETED);

        //when
        outboxEventPublisher.publishPendingEvents();

        //then
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(300))
                .untilAsserted(() -> {
                    assertThat(riskRuleStatisticsRepository.count()).isEqualTo(3);
                    assertThat(auditEventLogRepository.count()).isOne();

                    RiskRuleStatistics highRiskWallet = riskRuleStatisticsRepository.findByRuleCode("HIGH_RISK_WALLET")
                            .orElseThrow();

                    assertThat(highRiskWallet.getHitCount()).isEqualTo(1L);

                    assertThat(consumerProcessedEventRepository.existsByConsumerNameAndEventId("rule-statistics-consumer", event.eventId()))
                            .isTrue();

                    OutboxEvent sentEvent = outboxEventRepository.findAll().get(0);
                    assertThat(sentEvent.getStatus()).isEqualTo(OutboxEventStatus.SENT);
                    assertThat(sentEvent.getSentAt()).isNotNull();
                });
    }
}
