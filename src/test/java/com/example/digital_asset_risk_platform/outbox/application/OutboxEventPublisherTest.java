package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.event.producer.RiskEventProducer;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEventStatus;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class OutboxEventPublisherTest extends IntegrationTestSupport {

    @Autowired
    OutboxEventService outboxEventService;

    @Autowired
    OutboxEventPublisher outboxEventPublisher;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @MockitoBean
    RiskEventProducer riskEventProducer;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("PENDING OutboxEvent 발행 성공 시 SENT 상태로 변경한다")
    void case1() {
        //given
        mockKafkaPublishSuccess();

        outboxEventService.save(withdrawalRequestedEvent("event-success"));

        //when
        outboxEventPublisher.publishPendingEvents();

        //then
        OutboxEvent saved = outboxEventRepository.findAll().get(0);

        Assertions.assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        Assertions.assertThat(saved.getRetryCount()).isZero();
        Assertions.assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("Kafka 발행 실패 시 FAILED 상태로 변경하고 retryCount를 증가시킨다")
    void case2() {
        //given
        mockKafkaPublishFailure();

        outboxEventService.save(withdrawalRequestedEvent("event-failed"));

        //when
        outboxEventPublisher.publishPendingEvents();

        //then
        OutboxEvent saved = outboxEventRepository.findAll().get(0);

        Assertions.assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        Assertions.assertThat(saved.getRetryCount()).isOne();
        Assertions.assertThat(saved.getLastErrorMessage()).contains("Kafka publish failed");
        Assertions.assertThat(saved.getSentAt()).isNull();
    }

    @Test
    @DisplayName("FAILED OutboxEvent도 재시도 대상이며 성공하면 SENT 상태로 변경한다")
    void case3() {
        //given
        mockKafkaPublishFailure();

        outboxEventService.save(withdrawalRequestedEvent("event-retry"));

        outboxEventPublisher.publishPendingEvents();

        OutboxEvent failed = outboxEventRepository.findAll().get(0);
        Assertions.assertThat(failed.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        Assertions.assertThat(failed.getRetryCount()).isOne();

        mockKafkaPublishSuccess();

        //when
        outboxEventPublisher.publishPendingEvents();

        //then
        OutboxEvent retried = outboxEventRepository.findAll().get(0);

        Assertions.assertThat(retried.getStatus()).isEqualTo(OutboxEventStatus.SENT);
        Assertions.assertThat(retried.getRetryCount()).isOne();
        Assertions.assertThat(retried.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("retryCount가 최대 재시도 횟수 이상이면 발행 대상에서 제외한다")
    void case4() {
        //given
        WithdrawalRequestedEvent event = withdrawalRequestedEvent("event-max-retry");
        outboxEventService.save(event);

        OutboxEvent outboxEvent = outboxEventRepository.findAll().get(0);
        outboxEvent.markFailed("fail-1");
        outboxEvent.markFailed("fail-2");
        outboxEvent.markFailed("fail-3");
        outboxEvent.markFailed("fail-4");
        outboxEvent.markFailed("fail-5");
        outboxEventRepository.save(outboxEvent);

        mockKafkaPublishSuccess();

        //when
        outboxEventPublisher.publishPendingEvents();

        //then
        OutboxEvent saved = outboxEventRepository.findAll().get(0);
        Assertions.assertThat(saved.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        Assertions.assertThat(saved.getRetryCount()).isEqualTo(5);
    }

    private void mockKafkaPublishSuccess() {
        SendResult<String, String> sendResult = mock(SendResult.class);

        when(riskEventProducer.publishRawJson(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
    }

    private void mockKafkaPublishFailure() {
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka publish failed"));

        when(riskEventProducer.publishRawJson(anyString(), anyString(), anyString()))
                .thenReturn(failedFuture);
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