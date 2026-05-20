package com.example.digital_asset_risk_platform.kyt.application;

import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCacheResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class KytLookupServiceTest extends IntegrationTestSupport {

    @Autowired
    KytLookupService kytLookupService;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @BeforeEach
    void setUp() {
        walletAddressRiskRepository.deleteAll();
    }

    @Test
    @DisplayName("KYT 조회 결과가 위험 지갑이면 내부 WalletRisk로 저장한다")
     void case1() {
        //given
        String chainType = "TRON";
        String address = "THACKED000001";

        //when
        WalletRiskCacheResponse result = kytLookupService.lookupAndSync(chainType, address);

        //then
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(result.riskCategory()).isEqualTo("HACKED_FUNDS");
        assertThat(result.provider()).isEqualTo("MOCK_KYT");
        assertThat(walletAddressRiskRepository.existsByChainTypeAndAddress(chainType, address)).isTrue();
    }

    @Test
    @DisplayName("KYT 조회 결과가 정상 지갑이면 내부 WalletRisk에는 저장하지 않는다")
    void case2() {
        //given
        String chainType = "TRON";
        String address = "TNORMAL000001";

        //when
        WalletRiskCacheResponse result = kytLookupService.lookupAndSync(chainType, address);

        //then
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.LOW);
        assertThat(result.riskScore()).isZero();
        assertThat(result.riskCategory()).isEqualTo("NORMAL");
        assertThat(walletAddressRiskRepository.existsByChainTypeAndAddress(chainType, address)).isFalse();
    }

    @Test
    @DisplayName("이미 저장된 위험 주소를 다시 조회해도 중복 저장하지 않는다")
    void case3() {
        //given
        String chainType = "TRON";
        String address = "THACKED_DUPLICATE_001";

        WalletRiskCacheResponse first = kytLookupService.lookupAndSync(chainType, address);
        long firstCount = walletAddressRiskRepository.count();
        assertThat(firstCount).isOne();

        //when
        WalletRiskCacheResponse second = kytLookupService.lookupAndSync(chainType, address);

        //then
        assertThat(first.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(second.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(walletAddressRiskRepository.count()).isOne();
    }
}