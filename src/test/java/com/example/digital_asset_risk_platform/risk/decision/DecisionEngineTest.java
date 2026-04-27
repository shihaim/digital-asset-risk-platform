package com.example.digital_asset_risk_platform.risk.decision;

import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

public class DecisionEngineTest {

    private final DecisionEngine decisionEngine = new DecisionEngine();

    @Test
    @DisplayName("룰 적중이 없으면 정상 승인한다")
    void case1() {
        //given
        List<RuleHit> hits = List.of();

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.NORMAL);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.ALLOW);
        Assertions.assertThat(decision.totalScore()).isZero();
    }

    @Test
    @DisplayName("총점이 30점 이상 60점 미만이면 모니터링 대상으로 판단한다")
    void case2() {
        //given
        List<RuleHit> hits = List.of(ruleHit("NEW_DEVICE_WITHDRAWAL", 30, false));

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.WATCH);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.MONITOR);
        Assertions.assertThat(decision.totalScore()).isEqualTo(30);
    }

    @Test
    @DisplayName("총점이 60점 이상 80점 미만이면 추가 인증 대상으로 판단한다")
    void case3() {
        //given
        List<RuleHit> hits = List.of(
                ruleHit("NEW_DEVICE_WITHDRAWAL", 30, false),
                ruleHit("PASSWORD_CHANGED_WITHDRAWAL", 30, false)
        );

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.CAUTION);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.REQUIRE_ADDITIONAL_AUTH);
        Assertions.assertThat(decision.totalScore()).isEqualTo(60);
    }

    @Test
    @DisplayName("총점이 80점 이상 120점 미만이면 출금을 보류한다")
    void case4() {
        // given
        List<RuleHit> hits = List.of(
                ruleHit("OTP_RESET_WITHDRAWAL", 40, false),
                ruleHit("PASSWORD_CHANGED_WITHDRAWAL", 30, false),
                ruleHit("NEW_WALLET_ADDRESS", 20, false)
        );

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.HIGH);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.HOLD_WITHDRAWAL);
        Assertions.assertThat(decision.totalScore()).isEqualTo(90);
    }

    @Test
    @DisplayName("총점이 120점 이상이면 Critical 등급으로 출금을 보류한다")
    void case5() {
        // given
        List<RuleHit> hits = List.of(
                ruleHit("NEW_DEVICE_WITHDRAWAL", 30, false),
                ruleHit("PASSWORD_CHANGED_WITHDRAWAL", 30, false),
                ruleHit("OTP_RESET_WITHDRAWAL", 40, false),
                ruleHit("NEW_WALLET_ADDRESS", 20, false)
        );

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.HOLD_WITHDRAWAL);
        Assertions.assertThat(decision.totalScore()).isEqualTo(120);
    }

    @Test
    @DisplayName("blocking 룰이 있으면 점수와 무관하게 출금을 차단한다")
    void case6() {
        //given
        List<RuleHit> hits = List.of(ruleHit("HIGH_RISK_WALLET", 100, true));

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);
        Assertions.assertThat(decision.totalScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("blocking 룰은 총점 기준보다 우선한다")
    void case7() {
        //given
        List<RuleHit> hits = List.of(
                ruleHit("HIGH_RISK_WALLET", 100, true),
                ruleHit("NEW_DEVICE_WITHDRAWAL", 30, false),
                ruleHit("OTP_RESET_WITHDRAWAL", 40, false)
        );

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        Assertions.assertThat(decision.decisionType()).isEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);
        Assertions.assertThat(decision.totalScore()).isEqualTo(170);
    }

    @ParameterizedTest(name = "총점 {0}점이면 {1}/{2}로 판단한다")
    @CsvSource({
            "0, NORMAL, ALLOW",
            "29, NORMAL, ALLOW",
            "30, WATCH, MONITOR",
            "59, WATCH, MONITOR",
            "60, CAUTION, REQUIRE_ADDITIONAL_AUTH",
            "79, CAUTION, REQUIRE_ADDITIONAL_AUTH",
            "80, HIGH, HOLD_WITHDRAWAL",
            "119, HIGH, HOLD_WITHDRAWAL",
            "120, CRITICAL, HOLD_WITHDRAWAL"
    })
    @DisplayName("점수 구간에 따라 RiskLevel과 Decision을 결정한다")
    void case8(int score, RiskLevel expectedRiskLevel, RiskDecisionType expectedDecisionType) {
        //given
        List<RuleHit> hits = score == 0 ? List.of() : List.of(ruleHit("DUMMY_RULE", score, false));

        //when
        RiskDecision decision = decisionEngine.decide(hits);

        //then
        Assertions.assertThat(decision.riskLevel()).isEqualTo(expectedRiskLevel);
        Assertions.assertThat(decision.decisionType()).isEqualTo(expectedDecisionType);
        Assertions.assertThat(decision.totalScore()).isEqualTo(score);
    }

    private RuleHit ruleHit(String ruleCode, int score, boolean blocking) {
        return new RuleHit(ruleCode, "테스트 룰", score, "테스트 사유", blocking);
    }
}