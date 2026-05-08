package com.example.digital_asset_risk_platform.outbox.support;

import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.event.publisher.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class OutboxRollbackTestService {

    private final DomainEventPublisher domainEventPublisher;

    public void publishEventAndThrowException() {
        domainEventPublisher.publish(new WithdrawalRequestedEvent(
                "event-rollback",
                1L,
                10001L,
                "USDT",
                "TRON",
                "TADDRESS001",
                new BigDecimal("100.000000000000000000"),
                "REQUESTED",
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        throw new RuntimeException("force rollback");
    }
}
