package com.example.digital_asset_risk_platform.event.consumer;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "fds.evaluation.mode=async")
@ActiveProfiles("test")
public class FdsWithdrawalConsumerAsyncModeBeanTest extends IntegrationTestSupport {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    @DisplayName("async 모드에서는 FdsWithdrawalConsumer Bean이 등록된다")
    void case1() {
        Assertions.assertThat(applicationContext.getBeansOfType(FdsWithdrawalConsumer.class))
                .hasSize(1);
    }
}
