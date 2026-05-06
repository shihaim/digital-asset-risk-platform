package com.example.digital_asset_risk_platform.audit.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.audit.repository.AuditEventLogRepository;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ActiveProfiles("test")
class AuditEventLogServiceTest extends IntegrationTestSupport {

    @Autowired
    AuditEventLogService auditEventLogService;

    @Autowired
    AuditEventLogRepository auditEventLogRepository;

    @BeforeEach
    void setUp() {
        auditEventLogRepository.deleteAll();
    }

    @Test
    @DisplayName("이벤트 감사 로그를 저장한다")
    void case1() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-001", 1L, 10001L,
                "USDT", "TRON", "TADDRESS001", new BigDecimal("100.000000000000000000"), "REQUESTED", LocalDateTime.now(), LocalDateTime.now());

        //when
        auditEventLogService.saveIfNotExists(event.eventId(), "WithdrawalRequestedEvent", "withdrawal.requested", String.valueOf(event.withdrawalId()), event);

        //then
        Assertions.assertThat(auditEventLogRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("같은 eventId의 감사 로그는 중복 저장하지 않는다")
    void case2() {
        //given
        WithdrawalRequestedEvent event = new WithdrawalRequestedEvent(
                "event-duplicate", 1L, 10001L,
                "USDT", "TRON", "TADDRESS001", new BigDecimal("100.000000000000000000"), "REQUESTED", LocalDateTime.now(), LocalDateTime.now());

        //when
        auditEventLogService.saveIfNotExists(event.eventId(), "WithdrawalRequestedEvent", "withdrawal.requested", "1", event);
        auditEventLogService.saveIfNotExists(event.eventId(), "WithdrawalRequestedEvent", "withdrawal.requested", "1", event);

        //then
        Assertions.assertThat(auditEventLogRepository.count()).isEqualTo(1);
    }
}