package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.common.cache.CacheNames;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCacheResponse;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.cache.type=redis"})
class WalletRiskServiceCacheTest extends IntegrationTestSupport {
    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    WalletRiskQueryService walletRiskQueryService;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(CacheNames.WALLET_RISK);
        if (cache != null) {
            cache.clear();
        }

        walletAddressRiskRepository.deleteAll();
    }

    @Test
    @DisplayName("지갑 위험도 조회 결과를 Redis 캐시에 저장한다")
    void case1() {
        //given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "THACKED000001",
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        //when
        WalletRiskCacheResponse result = walletRiskQueryService.getWalletRisk("TRON", "THACKED000001");

        //then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);

        Cache.ValueWrapper cached = cacheManager
                .getCache(CacheNames.WALLET_RISK)
                .get("TRON:THACKED000001");

        Assertions.assertThat(cached).isNotNull();
    }

    @Test
    @DisplayName("캐시된 지갑 위험도는 DB 데이터가 삭제되어도 캐시에서 조회된다")
    void case2() {
        // given
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "THACKED000002",
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        WalletRiskCacheResponse first = walletRiskQueryService.getWalletRisk("TRON", "THACKED000002");

        Assertions.assertThat(first).isNotNull();

        walletAddressRiskRepository.deleteAll();

        //when
        WalletRiskCacheResponse second = walletRiskQueryService.getWalletRisk("TRON", "THACKED000002");

        //then
        Assertions.assertThat(second).isNotNull();
        Assertions.assertThat(second.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
    }

    @Test
    @DisplayName("존재하지 않는 지갑 위험도는 캐싱하지 않는다")
    void case3() {
        //when
        WalletRiskCacheResponse result = walletRiskQueryService.getWalletRisk("TRON", "TUNKOWN000001");

        //then
        Assertions.assertThat(result).isNull();

        Cache.ValueWrapper cached = cacheManager
                .getCache(CacheNames.WALLET_RISK)
                .get("TRON:TUNKOWN000001");

        Assertions.assertThat(cached).isNull();
    }

    @Test
    @DisplayName("지갑 위험도 등록 시 같은 key의 기존 캐시를 제거한다")
    void case4() {
        //given
        Cache cache = cacheManager.getCache(CacheNames.WALLET_RISK);

        cache.put("TRON:THACKED000003", new WalletRiskCacheResponse(
                "TRON",
                "THACKED000003",
                WalletRiskLevel.LOW,
                0,
                "STALE_CACHE",
                "TEST"
        ));

        Assertions.assertThat(cache.get("TRON:THACKED000003")).isNotNull();

        //when
        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "THACKED000003",
                WalletRiskLevel.CRITICAL,
                100,
                "SANCTIONED_ADDRESS",
                "MOCK_KYT"
        ));

        //then
        Assertions.assertThat(cache.get("TRON:THACKED000003")).isNull();
    }

}