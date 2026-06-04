package com.example.digital_asset_risk_platform.risk.profile.application;

import com.example.digital_asset_risk_platform.risk.profile.domain.UserRiskLevel;
import com.example.digital_asset_risk_platform.risk.profile.domain.UserRiskProfile;
import com.example.digital_asset_risk_platform.risk.profile.dto.UserRiskProfileResponse;
import com.example.digital_asset_risk_platform.risk.profile.repository.UserRiskProfileRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class UserRiskProfileServiceTest extends IntegrationTestSupport {

    @Autowired
    UserRiskProfileService userRiskProfileService;

    @Autowired
    private UserRiskProfileRepository userRiskProfileRepository;

    @BeforeEach
    void setUp() {
        userRiskProfileRepository.deleteAll();
    }

    @Test
    @DisplayName("기존 프로필이 없으면 생성 후 평가 결과를 반영한다")
    void case1() {
        //given
        Long userId = 10001L;

        //when
        userRiskProfileService.updateFromEvaluate(userId, 100, false);

        //then
        UserRiskProfile profile = userRiskProfileRepository.findByUserId(userId)
                .orElseThrow();

        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getCurrentRiskScore()).isEqualTo(50);
        assertThat(profile.getRiskLevel()).isEqualTo(UserRiskLevel.WATCH);
        assertThat(profile.getTotalBlockedWithdrawalCount()).isZero();
        assertThat(profile.getTotalCaseCount()).isZero();
        assertThat(profile.getLastEvaluatedAt()).isNotNull();
    }

    @Test
    @DisplayName("기존 프로필이 있으면 평가 점수를 누적한다")
    void case2() {
        //given
        Long userId = 10001L;

        userRiskProfileRepository.save(UserRiskProfile.create(userId));

        //when
        userRiskProfileService.updateFromEvaluate(userId, 100, false);
        userRiskProfileService.updateFromEvaluate(userId, 70, false);

        //then
        UserRiskProfile profile = userRiskProfileRepository.findByUserId(userId)
                .orElseThrow();

        assertThat(profile.getCurrentRiskScore()).isEqualTo(80);
        assertThat(profile.getRiskLevel()).isEqualTo(UserRiskLevel.HIGH);
    }

    @Test
    @DisplayName("BLOCK_WITHDRAWAL이면 차단 출금 횟수를 증가시킨다")
    void case3() {
        //given
        Long userId = 10001L;

        //when
        userRiskProfileService.updateFromEvaluate(userId, 100, true);

        //then
        UserRiskProfile profile = userRiskProfileRepository.findByUserId(userId)
                .orElseThrow();

        assertThat(profile.getTotalBlockedWithdrawalCount()).isOne();
    }

    @Test
    @DisplayName("RiskCase 생성 이벤트를 반영해 case count를 증가시킨다")
    void case4() {
        //given
        Long userId = 10001L;

        userRiskProfileService.updateFromEvaluate(userId, 100, false);

        //when
        userRiskProfileService.increaseCaseCount(userId);

        //then
        UserRiskProfile profile = userRiskProfileRepository.findByUserId(userId)
                .orElseThrow();

        assertThat(profile.getTotalCaseCount()).isOne();
        assertThat(profile.getCurrentRiskScore()).isEqualTo(50);
    }

    @Test
    @DisplayName("getProfile에서 프로필이 없으면 DB 저장 없이 기본 응답을 반환한다")
    void case5() {
        //given
        Long userId = 999L;
        long beforeCount = userRiskProfileRepository.count();

        //when
        UserRiskProfileResponse response = userRiskProfileService.getProfile(userId);

        //then
        long afterCount = userRiskProfileRepository.count();

        assertThat(afterCount).isEqualTo(beforeCount);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.currentRiskScore()).isZero();
        assertThat(response.riskLevel()).isEqualTo(UserRiskLevel.NORMAL.name());
        assertThat(response.totalCaseCount()).isZero();
        assertThat(response.totalBlockedWithdrawalCount()).isZero();
        assertThat(response.lastEvaluatedAt()).isNull();
    }
}