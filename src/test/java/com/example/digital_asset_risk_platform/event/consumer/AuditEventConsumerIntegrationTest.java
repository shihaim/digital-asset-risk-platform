package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.audit.repository.AuditEventLogRepository;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuditEventConsumerIntegrationTest extends IntegrationTestSupport {

    @Autowired
    AuditEventConsumer auditEventConsumer;

    @Autowired
    AuditEventLogRepository auditEventLogRepository;

    private final Acknowledgment acknowledgment = mock(Acknowledgment.class);

    @BeforeEach
    public void setUp() {
        auditEventLogRepository.deleteAll();
    }

    @Test
    @DisplayName("출금 요청 이벤트 소비 시 감사 로그를 저장한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-withdrawal-001", 1L, 10001L, "USDT", "TRON", "TADDRESS001",
                new BigDecimal("100.000000000000000000"), "REQUESTED", LocalDateTime.now(), LocalDateTime.now());

        //when
        auditEventConsumer.consumeWithdrawalRequested(event, String.valueOf(event.withdrawalId()), acknowledgment);

        //then
        Assertions.assertThat(auditEventLogRepository.count()).isOne();
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("같은 출금 요청 이벤트를 두 번 소비해도 감사 로그는 하나만 저장한다")
    void case2() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-withdrawal-duplicate", 1L, 10001L, "USDT", "TRON", "TADDRESS001",
                new BigDecimal("100.000000000000000000"), "REQUESTED", LocalDateTime.now(), LocalDateTime.now());

        //when
        auditEventConsumer.consumeWithdrawalRequested(event, String.valueOf(event.withdrawalId()), acknowledgment);
        auditEventConsumer.consumeWithdrawalRequested(event, String.valueOf(event.withdrawalId()), acknowledgment);

        //then
        Assertions.assertThat(auditEventLogRepository.count()).isOne();
    }
}
