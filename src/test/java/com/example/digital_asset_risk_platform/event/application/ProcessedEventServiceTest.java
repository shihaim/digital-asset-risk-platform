package com.example.digital_asset_risk_platform.event.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ProcessedEventServiceTest extends IntegrationTestSupport {

    @Autowired
    ProcessedEventService processedEventService;

    @Autowired
    ConsumerProcessedEventRepository consumerProcessedEventRepository;

    @BeforeEach
    void setUp() {
        consumerProcessedEventRepository.deleteAll();
    }

    @Test
    @DisplayName("처리 완료 이벤트를 기록한다")
    void case1() {
        //given
        String consumerName = "rule-statistics-consumer";
        String eventId = "event-001";

        //when
        processedEventService.markProcessed(consumerName, eventId);

        //then
        Assertions.assertThat(processedEventService.isProcessed(consumerName, eventId)).isTrue();
        Assertions.assertThat(consumerProcessedEventRepository.count()).isOne();
    }

    @Test
    @DisplayName("같은 consumerName과 eventId는 중복 기록하지 않는다")
    void case2() {
        //given
        String consumerName = "rule-statistics-consumer";
        String eventId = "event-duplicate";

        //when
        processedEventService.markProcessed(consumerName, eventId);
        processedEventService.markProcessed(consumerName, eventId);

        //then
        Assertions.assertThat(consumerProcessedEventRepository.count()).isOne();
    }
}