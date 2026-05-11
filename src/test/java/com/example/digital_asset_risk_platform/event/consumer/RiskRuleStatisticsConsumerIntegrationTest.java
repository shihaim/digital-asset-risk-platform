package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.repository.ConsumerProcessedEventRepository;
import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
public class RiskRuleStatisticsConsumerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleStatisticsConsumer riskRuleStatisticsConsumer;

    @Autowired
    RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    @Autowired
    ConsumerProcessedEventRepository consumerProcessedEventRepository;

    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @BeforeEach
    public void setup() {
        riskRuleStatisticsRepository.deleteAll();
        consumerProcessedEventRepository.deleteAll();
    }

    @Test
    @DisplayName("평가 완료 이벤트 소비 시 Rule 통계를 증가시킨다")
    void case1() {
        //given
        LocalDateTime evaluationAt = LocalDateTime.now();

        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-evaluation-001", 10L, "WITHDRAWAL", 1L, 10001L, 190, "CRITICAL", "BLOCK_WITHDRAWAL",
                List.of("NEW_DEVICE_WITHDRAWAL", "OPT_RESET_WITHDRAWAL", "HIGH_RISK_WALLET"), evaluationAt, LocalDateTime.now());

        //when
        riskRuleStatisticsConsumer.consume(event, acknowledgment);

        //then
        RiskRuleStatistics highRiskWallet = riskRuleStatisticsRepository.findByRuleCode("HIGH_RISK_WALLET").orElseThrow();

        Assertions.assertThat(highRiskWallet.getHitCount()).isOne();
        Assertions.assertThat(riskRuleStatisticsRepository.count()).isEqualTo(3);
        Assertions.assertThat(consumerProcessedEventRepository.count()).isOne();
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("같은 평가 완료 이벤트를 두 번 소비해도 Rule 통계는 중복 증가하지 않는다")
    void case2() {
        //given
        LocalDateTime evaluationAt = LocalDateTime.now();

        RiskEvaluationCompletedEvent event = new RiskEvaluationCompletedEvent(
                "event-evaluation-duplicate", 10L, "WITHDRAWAL", 1L, 10001L, 190, "CRITICAL", "BLOCK_WITHDRAWAL",
                List.of("HIGH_RISK_WALLET"), evaluationAt, LocalDateTime.now());

        //when
        riskRuleStatisticsConsumer.consume(event, acknowledgment);
        riskRuleStatisticsConsumer.consume(event, acknowledgment);

        //then
        RiskRuleStatistics statistics = riskRuleStatisticsRepository.findByRuleCode("HIGH_RISK_WALLET").orElseThrow();

        Assertions.assertThat(statistics.getHitCount()).isOne();
        Assertions.assertThat(consumerProcessedEventRepository.count()).isOne();
    }
}
