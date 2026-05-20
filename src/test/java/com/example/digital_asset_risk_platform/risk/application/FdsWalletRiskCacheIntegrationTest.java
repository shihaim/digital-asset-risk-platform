package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.common.cache.CacheNames;
import com.example.digital_asset_risk_platform.event.publisher.DomainEventPublisher;
import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

@TestPropertySource(properties = {
        "spring.cache.type=redis",
        "fds.evaluation.mode=sync"
})
public class FdsWalletRiskCacheIntegrationTest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    CacheManager cacheManager;

    @MockitoBean
    DomainEventPublisher domainEventPublisher;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(CacheNames.WALLET_RISK);
        if (cache != null) {
            cache.clear();
        }

        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();

        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
    }

    @Test
    @DisplayName("FDS 평가 시 DB에 지갑 위험도 정보가 없어도 Redis 캐시로 HIGH_RISK_WALLET Rule이 적중한다")
    void case1() {
        //given
        String chainType = "TRON";
        String address = "THACKED_CACHE_001";

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                chainType,
                address,
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        WithdrawalCreateResponse firstResponse = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                10001L,
                "USDT",
                chainType,
                address,
                new BigDecimal("10000.000000000000000000")
        ));

        Assertions.assertThat(firstResponse.status()).isEqualTo(WithdrawalStatus.BLOCKED);

        Cache walletRiskCache = cacheManager.getCache(CacheNames.WALLET_RISK);
        Assertions.assertThat(walletRiskCache).isNotNull();
        Assertions.assertThat(walletRiskCache.get(chainType + ":" + address)).isNotNull();

        walletAddressRiskRepository.deleteAll();

        //when
        WithdrawalCreateResponse secondResponse = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                20001L,
                "USDT",
                chainType,
                address,
                new BigDecimal("10000.000000000000000000")
        ));

        //then
        Assertions.assertThat(secondResponse.status()).isEqualTo(WithdrawalStatus.BLOCKED);
        Assertions.assertThat(secondResponse.totalScore()).isGreaterThanOrEqualTo(100);
        Assertions.assertThat(walletAddressRiskRepository.count()).isZero();
        Assertions.assertThat(riskRuleHitRepository.findAll())
                .extracting(RiskRuleHit::getRuleCode)
                .contains("HIGH_RISK_WALLET");
    }
}
