package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.mapper.OutboxEventMapper;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventMapper outboxEventMapper;

    public void save(Object event) {
        OutboxEvent outboxEvent = outboxEventMapper.toOutboxEvent(event);

        if (outboxEventRepository.existsByEventId(outboxEvent.getEventId())) {
            log.warn(
                    "Duplicate outbox event skipped. eventId={}, eventType={}, topicName={}",
                    outboxEvent.getEventId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getTopicName()
            );
            return;
        }

        if (outboxEventRepository.existsByEventId(outboxEvent.getEventId())) {
            return;
        }

        outboxEventRepository.save(outboxEvent);

        log.info(
                "Outbox event saved. eventId={}, eventType={}, topicName={}, messageKey={}, status={}",
                outboxEvent.getEventId(),
                outboxEvent.getEventType(),
                outboxEvent.getTopicName(),
                outboxEvent.getMessageKey(),
                outboxEvent.getStatus()
        );
    }
}
