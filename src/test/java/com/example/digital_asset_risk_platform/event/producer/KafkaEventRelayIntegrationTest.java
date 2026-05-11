package com.example.digital_asset_risk_platform.event.producer;

import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.math.BigDecimal;
import java.time.Duration;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@Deprecated
@ActiveProfiles("test")
public class KafkaEventRelayIntegrationTest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @MockitoBean
    RiskEventProducer riskEventProducer;

//    @Test
    @DisplayName("트랜잭션 커밋 이후 도메인 이벤트를 Kafka Producer로 위임한다")
    void case1() {
        //given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest("TRON", "TNORMAL000001", WalletRiskLevel.LOW, 0, "NORMAL", "MOCK_KYT"));

        //when
        withdrawalService.createWithdrawal(new WithdrawalCreateRequest(10001L, "USDT", "TRON", "TNORMAL000001", new BigDecimal("100.000000000000000000")));

        //then
        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    verify(riskEventProducer).publishWithdrawalRequested(any(WithdrawalRequestedEvent.class));
                    verify(riskEventProducer).publishRiskEvaluationCompleted(any(RiskEvaluationCompletedEvent.class));
                });
    }
}
