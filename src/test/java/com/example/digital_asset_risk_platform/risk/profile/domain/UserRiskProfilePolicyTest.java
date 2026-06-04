package com.example.digital_asset_risk_platform.risk.profile.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRiskProfilePolicyTest {

    @Test
    @DisplayName("evaluationScore가 100 이상이면 profileScore는 50이다")
    void case1() {
        assertThat(UserRiskProfilePolicy.toProfileScore(100)).isEqualTo(50);
        assertThat(UserRiskProfilePolicy.toProfileScore(150)).isEqualTo(50);
    }

    @Test
    @DisplayName("evaluationScore가 70 이상 100 미만이면 profileScore는 30이다")
    void case2() {
        assertThat(UserRiskProfilePolicy.toProfileScore(70)).isEqualTo(30);
        assertThat(UserRiskProfilePolicy.toProfileScore(99)).isEqualTo(30);
    }

    @Test
    @DisplayName("evaluationScore가 30 이상 70 미만이면 profileScore는 10이다")
    void case3() {
        assertThat(UserRiskProfilePolicy.toProfileScore(30)).isEqualTo(10);
        assertThat(UserRiskProfilePolicy.toProfileScore(69)).isEqualTo(10);
    }

    @Test
    @DisplayName("evaluationScore가 30 미만이면 profileScore는 0이다")
    void case4() {
        assertThat(UserRiskProfilePolicy.toProfileScore(0)).isEqualTo(0);
        assertThat(UserRiskProfilePolicy.toProfileScore(29)).isEqualTo(0);
    }
}