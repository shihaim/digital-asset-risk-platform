package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "fds.evaluation.mode=sync")
@ActiveProfiles("test")
public class FdsWithdrawalConsumerSyncModeBeanTest extends IntegrationTestSupport {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("sync 모드에서는 FdsWithdrawalConsumer Bean이 등록되지 않는다")
    void case1() {
        Assertions.assertThat(applicationContext.getBeansOfType(FdsWithdrawalConsumer.class))
                .isEmpty();
    }
}
