package com.example.digital_asset_risk_platform.risk.config.domain;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskRuleConfigTest {

    @Test
    @DisplayName("enabled와 blocking boolean 값을 YN 값으로 저장하고 다시 boolean으로 조회한다")
    void case1() {
        //when
        RiskRuleConfig config = new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        );

        //then
        assertThat(config.getEnabledYn()).isEqualTo("Y");
        assertThat(config.getBlockingYn()).isEqualTo("Y");
        assertThat(config.isEnabled()).isTrue();
        assertThat(config.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("score가 음수이면 Rule 설정 생성에 실패한다")
    void case2() {
        //when&then
        assertThatThrownBy(() -> new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                -1,
                true,
                null,
                "고위험 지갑 차단"
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_RISK_RULE_CONFIG.getMessage());
    }

    @Test
    @DisplayName("thresholdValue를 int로 파싱한다")
    void case3() {
        //when
        RiskRuleConfig config = new RiskRuleConfig(
                RiskRuleCodes.FREQUENT_WITHDRAWAL_24H,
                "24시간 내 반복 출금",
                true,
                30,
                false,
                "7",
                "반복 출금"
        );

        //then
        assertThat(config.getThresholdAsInt(5)).isEqualTo(7);
    }

    @Test
    @DisplayName("thresholdValue를 Duration으로 파싱한다")
    void case4() {
        //when
        RiskRuleConfig minuteConfig = new RiskRuleConfig(
                RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                "신규 기기 로그인 후 출금",
                true,
                30,
                false,
                "60m",
                "신규 기기"
        );

        RiskRuleConfig hourConfig = new RiskRuleConfig(
                RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                "OTP 재설정 후 출금",
                true,
                40,
                false,
                "24h",
                "OTP 재설정"
        );

        //then
        assertThat(minuteConfig.getThresholdAsDuration(Duration.ofMinutes(30))).isEqualTo(Duration.ofMinutes(60));
        assertThat(hourConfig.getThresholdAsDuration(Duration.ofHours(1))).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("thresholdValue가 배수 표현이면 multiplier로 파싱한다")
    void case5() {
        //when
        RiskRuleConfig config = new RiskRuleConfig(
                RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                "평균 대비 고액 출금",
                true,
                40,
                false,
                "10x",
                "고액 출금"
        );

        //then
        assertThat(config.getMultiplierThreshold(BigDecimal.ONE)).isEqualByComparingTo("10");
    }
}