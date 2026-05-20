package com.example.digital_asset_risk_platform.kyt.application;

import com.example.digital_asset_risk_platform.common.cache.CacheNames;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.domain.WalletAddressRisk;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCacheResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

public class WalletRiskServiceKytFallbackTest extends IntegrationTestSupport {

    @Autowired
    WalletRiskService walletRiskService;

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
    @DisplayName("DB에 없는 위험 주소는 KYT Provider 조회 후 WalletRisk로 동기화된다")
    void case1() {
        //given
        String chainType = "TRON";
        String address = "THACKED000001";

        //when
        WalletRiskCacheResponse result = walletRiskService.getWalletRisk(chainType, address);

        //then
        assertThat(result).isNotNull();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(result.riskCategory()).isEqualTo("HACKED_FUNDS");
        assertThat(walletAddressRiskRepository.existsByChainTypeAndAddress(chainType, address)).isTrue();
    }

    @Test
    @DisplayName("DB에 없는 정상 주소는 KYT 조회 결과 LOW로 반환되지만 DB에는 저장하지 않는다")
    void case2() {
        // given
        String chainType = "TRON";
        String address = "TNORMAL000001";

        //when
        WalletRiskCacheResponse result = walletRiskService.getWalletRisk(chainType, address);

        //then
        assertThat(result).isNotNull();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.LOW);
        assertThat(result.riskCategory()).isEqualTo("NORMAL");
        assertThat(walletAddressRiskRepository.existsByChainTypeAndAddress(chainType, address)).isFalse();
    }

    @Test
    @DisplayName("DB에 이미 있는 주소는 KYT fallback 없이 DB 값을 반환한다")
    void case3() {
        // given
        String chainType = "TRON";
        String address = "THACKED_DB_001";

        walletAddressRiskRepository.save(new WalletAddressRisk(
                chainType,
                address,
                WalletRiskLevel.CRITICAL,
                99,
                "MANUAL_SANCTION",
                "ADMIN"
        ));

        //when
        WalletRiskCacheResponse result = walletRiskService.getWalletRisk(chainType, address);

        //then
        assertThat(result).isNotNull();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.CRITICAL);
        assertThat(result.riskScore()).isEqualTo(99);
        assertThat(result.riskCategory()).isEqualTo("MANUAL_SANCTION");
        assertThat(result.provider()).isEqualTo("ADMIN");
        assertThat(walletAddressRiskRepository.count()).isOne();
    }
}
