package com.example.digital_asset_risk_platform.event.repository;

import com.example.digital_asset_risk_platform.event.domain.ConsumerProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumerProcessedEventRepository extends JpaRepository<ConsumerProcessedEvent, Long> {

    boolean existsByConsumerNameAndEventId(String consumerName, String eventId);
}
