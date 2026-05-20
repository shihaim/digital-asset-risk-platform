package com.example.digital_asset_risk_platform.kyt.application;

import com.example.digital_asset_risk_platform.kyt.dto.KytLookupRequest;
import com.example.digital_asset_risk_platform.kyt.dto.KytLookupResult;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockKytProviderTest {

    private final MockKytProvider provider = new MockKytProvider();

    @Test
    @DisplayName("HACKED가 포함된 주소는 HIGH 지갑으로 판단한다")
    void case1() {
        //given
        KytLookupRequest request = new KytLookupRequest("TRON", "THACKED000001");

        //when
        KytLookupResult result = provider.lookup(request);

        //then
        assertThat(result.risky()).isTrue();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(result.riskScore()).isEqualTo(100);
        assertThat(result.riskCategory()).isEqualTo("HACKED_FUNDS");
        assertThat(result.provider()).isEqualTo("MOCK_KYT");
    }

    @Test
    @DisplayName("SANCTION이 포함된 주소는 CRITICAL 지갑으로 판단한다")
    void case2() {
        // given
        KytLookupRequest request = new KytLookupRequest("TRON", "TSANCTION000001");

        // when
        KytLookupResult result = provider.lookup(request);

        // then
        assertThat(result.risky()).isTrue();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.CRITICAL);
        assertThat(result.riskScore()).isEqualTo(100);
        assertThat(result.riskCategory()).isEqualTo("SANCTIONED_ADDRESS");
        assertThat(result.provider()).isEqualTo("MOCK_KYT");
    }

    @Test
    @DisplayName("MIXER가 포함된 주소는 HIGH 지갑으로 판단한다")
    void case3() {
        // given
        KytLookupRequest request = new KytLookupRequest("TRON", "TMIXER000001");

        // when
        KytLookupResult result = provider.lookup(request);

        // then
        assertThat(result.risky()).isTrue();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(result.riskScore()).isEqualTo(90);
        assertThat(result.riskCategory()).isEqualTo("MIXER");
        assertThat(result.provider()).isEqualTo("MOCK_KYT");
    }

    @Test
    @DisplayName("PHISH가 포함된 주소는 HIGH 지갑으로 판단한다")
    void case4() {
        // given
        KytLookupRequest request = new KytLookupRequest("TRON", "TPHISH000001");

        // when
        KytLookupResult result = provider.lookup(request);

        // then
        assertThat(result.risky()).isTrue();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(result.riskScore()).isEqualTo(85);
        assertThat(result.riskCategory()).isEqualTo("PHISHING");
        assertThat(result.provider()).isEqualTo("MOCK_KYT");
    }

    @Test
    @DisplayName("위험 키워드가 없는 주소는 정상 지갑으로 판단한다")
    void case5() {
        // given
        KytLookupRequest request = new KytLookupRequest("TRON", "TNORMAL000001");

        // when
        KytLookupResult result = provider.lookup(request);

        // then
        assertThat(result.risky()).isFalse();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.LOW);
        assertThat(result.riskScore()).isZero();
        assertThat(result.riskCategory()).isEqualTo("NORMAL");
        assertThat(result.provider()).isEqualTo("MOCK_KYT");
    }

    @Test
    @DisplayName("소문자 위험 키워드도 대소문자 구분 없이 탐지한다")
    void case6() {
        // given
        KytLookupRequest request = new KytLookupRequest("TRON", "thacked000001");

        // when
        KytLookupResult result = provider.lookup(request);

        // then
        assertThat(result.risky()).isTrue();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.HIGH);
        assertThat(result.riskCategory()).isEqualTo("HACKED_FUNDS");
    }

    @Test
    @DisplayName("주소가 null이면 예외 없이 정상 지갑으로 판단한다")
    void case7() {
        // given
        KytLookupRequest request = new KytLookupRequest("TRON", null);

        // when
        KytLookupResult result = provider.lookup(request);

        // then
        assertThat(result.risky()).isFalse();
        assertThat(result.riskLevel()).isEqualTo(WalletRiskLevel.LOW);
        assertThat(result.riskCategory()).isEqualTo("NORMAL");
    }
}