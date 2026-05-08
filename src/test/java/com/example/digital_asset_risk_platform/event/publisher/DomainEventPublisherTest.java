package com.example.digital_asset_risk_platform.event.publisher;

import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.outbox.application.OutboxEventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DomainEventPublisherTest {

    private final OutboxEventService outboxEventService = mock(OutboxEventService.class);
    private final DomainEventPublisher domainEventPublisher = new DomainEventPublisher(outboxEventService);

    @Test
    @DisplayName("도메인 이벤트 발행 시 OutboxEventService에 저장을 위임한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-001",
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100.000000000000000000"),
                "REQUESTED",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //when
        domainEventPublisher.publish(event);

        //then
        verify(outboxEventService).save(event);
    }

}