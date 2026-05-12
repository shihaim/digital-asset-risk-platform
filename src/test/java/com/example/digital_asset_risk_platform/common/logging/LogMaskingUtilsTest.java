package com.example.digital_asset_risk_platform.common.logging;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogMaskingUtilsTest {

    @Test
    @DisplayName("지갑 주소를 마스킹한다")
    void case1() {
        String masked = LogMaskingUtils.maskAddress("THACKED000001");

        Assertions.assertThat(masked).isEqualTo("THACKE...0001");
    }

    @Test
    @DisplayName("짧은 지갑 주소는 전체 마스킹한다")
    void case2() {
        String masked = LogMaskingUtils.maskAddress("SHORT");

        Assertions.assertThat(masked).isEqualTo("****");
    }

    @Test
    @DisplayName("IP 마지막 octet을 마스킹한다")
    void case3() {
        String masked = LogMaskingUtils.maskIp("185.220.101.10");

        Assertions.assertThat(masked).isEqualTo("185.220.101.xxx");
    }
}