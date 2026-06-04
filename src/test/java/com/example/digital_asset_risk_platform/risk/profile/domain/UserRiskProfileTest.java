package com.example.digital_asset_risk_platform.risk.profile.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRiskProfileTest {

    @Test
    @DisplayName("평가 점수를 프로필 점수로 변환해 누적한다")
    void case1() {
        //given
        UserRiskProfile profile = UserRiskProfile.create(10001L);

        //when
        profile.applyEvaluationResult(100, false);

        //then
        assertThat(profile.getCurrentRiskScore()).isEqualTo(50);
        assertThat(profile.getRiskLevel()).isEqualTo(UserRiskLevel.WATCH);
        assertThat(profile.getTotalBlockedWithdrawalCount()).isZero();
        assertThat(profile.getLastEvaluatedAt()).isNotNull();
    }

    @Test
    @DisplayName("BLOCK_WITHDRAWAL이면 차단 출금 횟수를 증가시킨다")
    void case2() {
        //given
        UserRiskProfile profile = UserRiskProfile.create(10001L);

        //when
        profile.applyEvaluationResult(100, true);

        //then
        assertThat(profile.getCurrentRiskScore()).isEqualTo(50);
        assertThat(profile.getTotalBlockedWithdrawalCount()).isOne();
    }

    @Test
    @DisplayName("점수 누적에 따라 UserRiskLevel이 변경된다")
    void case3() {
        //given
        UserRiskProfile profile = UserRiskProfile.create(10001L);

        //when
        profile.applyEvaluationResult(100, false); // +50 WATCH
        profile.applyEvaluationResult(100, false); // +50 HIGH
        profile.applyEvaluationResult(100, false); // +50 CRITICAL

        //then
        assertThat(profile.getCurrentRiskScore()).isEqualTo(150);
        assertThat(profile.getRiskLevel()).isEqualTo(UserRiskLevel.CRITICAL);
    }

    @Test
    @DisplayName("increaseCaseCount는 totalCaseCount만 증가시킨다")
    void case4() {
        //given
        UserRiskProfile profile = UserRiskProfile.create(10001L);

        //when
        profile.increaseCaseCount();

        //then
        assertThat(profile.getTotalCaseCount()).isOne();
        assertThat(profile.getCurrentRiskScore()).isZero();
        assertThat(profile.getTotalBlockedWithdrawalCount()).isZero();
        assertThat(profile.getUpdatedAt()).isNotNull();
    }
}