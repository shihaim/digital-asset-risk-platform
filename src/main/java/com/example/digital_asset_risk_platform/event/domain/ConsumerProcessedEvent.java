package com.example.digital_asset_risk_platform.event.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "consumer_processed_event",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_consumer_event", columnNames = {"consumer_name", "event_id"})
        }
)
public class ConsumerProcessedEvent {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "consumer_name", nullable = false, length = 100)
        private String consumerName;

        @Column(name = "event_id", nullable = false)
        private String eventId;

        @Column(name = "processed_at", nullable = false)
        private LocalDateTime processedAt;

        public ConsumerProcessedEvent(String consumerName, String eventId) {
                this.consumerName = consumerName;
                this.eventId = eventId;
                this.processedAt = LocalDateTime.now();
        }
}
