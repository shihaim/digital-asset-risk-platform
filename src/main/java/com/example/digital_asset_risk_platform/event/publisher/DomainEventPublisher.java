package com.example.digital_asset_risk_platform.event.publisher;

import com.example.digital_asset_risk_platform.outbox.application.OutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final OutboxEventService outboxEventService;

    public void publish(Object event) {
        outboxEventService.save(event);
    }
}
