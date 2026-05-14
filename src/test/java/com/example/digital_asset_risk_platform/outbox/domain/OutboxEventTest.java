package com.example.digital_asset_risk_platform.outbox.domain;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class OutboxEventTest {

    @Test
    @DisplayName("발행 실패 횟수가 최대 재시도 횟수 미만이면 FAILED 상태가 된다")
    void case1() {
        //given
        OutboxEvent event = new OutboxEvent(
                "event-001",
                "RiskCaseCreatedEvent",
                KafkaTopicConfig.RISK_CASE_CREATED,
                "1",
                "{}",
                LocalDateTime.now()
        );

        //when
        event.markProcessing();
        event.markFailed("fail", 5);

        //then
        Assertions.assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        Assertions.assertThat(event.getRetryCount()).isOne();
        Assertions.assertThat(event.getLastErrorMessage()).isEqualTo("fail");
    }

    @Test
    @DisplayName("발행 실패 횟수가 최대 재시도 횟수에 도달하면 DEAD 상태가 된다")
    void case2() {
        //given
        OutboxEvent event = new OutboxEvent(
                "event-001",
                "RiskCaseCreatedEvent",
                "risk.case.created",
                "1",
                "{}",
                LocalDateTime.now()
        );

        //when
        for (int i = 0; i < 5; i++) {
            event.markProcessing();
            event.markFailed("fail", 5);
        }

        //then
        Assertions.assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.DEAD);
        Assertions.assertThat(event.getRetryCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("FAILED 상태 이벤트는 수동 재처리 시 PENDING 상태가 된다")
    void case3() {
        //given
        OutboxEvent event = new OutboxEvent(
                "event-001",
                "RiskCaseCreatedEvent",
                "risk.case.created",
                "1",
                "{}",
                LocalDateTime.now()
        );

        //when
        event.markProcessing();
        event.markFailed("fail", 5);

        event.retryManually();

        //then
        Assertions.assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        Assertions.assertThat(event.getLastErrorMessage()).isNull();
    }

    @Test
    @DisplayName("SENT 상태 이벤트는 수동 재처리할 수 없다")
    void case4() {
        //given
        OutboxEvent event = new OutboxEvent(
                "event-001",
                "RiskCaseCreatedEvent",
                "risk.case.created",
                "1",
                "{}",
                LocalDateTime.now()
        );

        //when
        event.markProcessing();
        event.markSent();

        Assertions.assertThatThrownBy(event::retryManually).isInstanceOf(BusinessException.class);
    }

}