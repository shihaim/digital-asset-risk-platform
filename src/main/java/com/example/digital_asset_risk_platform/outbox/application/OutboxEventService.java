package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.mapper.OutboxEventMapper;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;

    public void save(Object event) {
        OutboxEvent outboxEvent = outboxEventMapper.toOutboxEvent(event);

        if (outboxEventRepository.existsByEventId(outboxEvent.getEventId())) {
            return;
        }

        outboxEventRepository.save(outboxEvent);
    }
}
