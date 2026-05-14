package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

class OutboxEventServiceTest extends IntegrationTestSupport {

    @Autowired
    OutboxEventService outboxEventService;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("도메인 이벤트를 PENDING 상태의 OutboxEvent로 저장한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = withdrawalRequestedEvent("event-001");

        //when
        outboxEventService.save(event);

        //then
        List<OutboxEvent> events = outboxEventRepository.findAll();
        Assertions.assertThat(events).hasSize(1);
        OutboxEvent saved = events.get(0);

        Assertions.assertThat(saved.getEventId()).isEqualTo("event-001");
        Assertions.assertThat(saved.getEventType()).isEqualTo("WithdrawalRequestedEvent");
        Assertions.assertThat(saved.getTopicName()).isEqualTo(KafkaTopicConfig.WITHDRAWAL_REQUESTED);
        Assertions.assertThat(saved.getMessageKey()).isEqualTo("1");
        Assertions.assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        Assertions.assertThat(saved.getRetryCount()).isZero();
        Assertions.assertThat(saved.getPayloadJson()).contains("\"eventId\":\"event-001\"");
        Assertions.assertThat(saved.getPayloadJson()).contains("\"withdrawalId\":1");
    }

    @Test
    @DisplayName("같은 eventId의 OutboxEvent는 중복 저장하지 않는다")
    void case2() {
        //given
        WithdrawalRequestedEvent event = withdrawalRequestedEvent("event-duplicate");

        //when
        outboxEventService.save(event);
        outboxEventService.save(event);

        //then
        Assertions.assertThat(outboxEventRepository.count()).isOne();
    }

    private WithdrawalRequestedEvent withdrawalRequestedEvent(String eventId) {
        return new WithdrawalRequestedEvent(
                eventId,
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100.000000000000000000"),
                "REQUESTED",
                LocalDateTime.of(2026, 5, 6, 10, 0),
                LocalDateTime.of(2026, 5, 6, 10, 0, 1)
        );
    }
}