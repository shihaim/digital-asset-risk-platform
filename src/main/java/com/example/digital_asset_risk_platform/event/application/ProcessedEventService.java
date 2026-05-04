package com.example.digital_asset_risk_platform.event.application;

import com.example.digital_asset_risk_platform.event.domain.ConsumerProcessedEvent;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ConsumerProcessedEventRepository processedEventRepository;

    @Transactional(readOnly = true)
    public boolean isProcessed(String consumerName, String eventId) {
        return processedEventRepository.existsByConsumerNameAndEventId(consumerName, eventId);
    }

    public void markProcessed(String consumerName, String eventId) {
        if (processedEventRepository.existsByConsumerNameAndEventId(consumerName, eventId)) {
            return;
        }

        processedEventRepository.save(new ConsumerProcessedEvent(consumerName, eventId));
    }
}
