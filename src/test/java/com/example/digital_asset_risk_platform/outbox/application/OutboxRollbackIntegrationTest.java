package com.example.digital_asset_risk_platform.outbox.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.example.digital_asset_risk_platform.outbox.repository.OutboxEventRepository;
import com.example.digital_asset_risk_platform.outbox.support.OutboxRollbackTestService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class OutboxRollbackIntegrationTest extends IntegrationTestSupport {

    @Autowired
    OutboxRollbackTestService outboxRollbackTestService;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("트랜잭션 롤백 시 OutboxEvent 저장도 함께 롤백된다")
    void case1() {
        //when&then
        Assertions.assertThatThrownBy(() -> outboxRollbackTestService.publishEventAndThrowException())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("force rollback");

        Assertions.assertThat(outboxEventRepository.count()).isZero();
    }
}
