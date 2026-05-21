package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RiskRuleEvaluationTest {

    private final RiskRuleConfigService riskRuleConfigService =  mock(RiskRuleConfigService.class);

    private final List<RiskRule> rules = List.of(
            new NewDeviceWithdrawalRule(riskRuleConfigService),
            new PasswordChangedWithdrawalRule(riskRuleConfigService),
            new OtpResetWithdrawalRule(riskRuleConfigService),
            new NewWalletAddressRule(riskRuleConfigService),
            new HighAmountWithdrawalRule(riskRuleConfigService),
            new FrequentWithdrawalRule(riskRuleConfigService),
            new HighRiskWalletRule(riskRuleConfigService)
    );

    @Test
    @DisplayName("신규 기기, OTP 재설정, 신규 지갑, 고위험 지갑 조건이면 4개 룰이 적중한다")
    void case1() {
        //given
        when(riskRuleConfigService.getConfig(RiskRuleCodes.NEW_DEVICE_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                        "신규 기기 로그인 후 출금",
                        true,
                        30,
                        false,
                        "60m",
                        "신규 기기"
                ));

        when(riskRuleConfigService.getConfig(RiskRuleCodes.PASSWORD_CHANGED_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.PASSWORD_CHANGED_WITHDRAWAL,
                        "비밀번호 변경 후 출금",
                        true,
                        30,
                        false,
                        "24h",
                        "비밀번호 변경"
                ));

        when(riskRuleConfigService.getConfig(RiskRuleCodes.OTP_RESET_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                        "OTP 재설정 후 출금",
                        true,
                        40,
                        false,
                        "24h",
                        "OTP 재설정"
                ));

        when(riskRuleConfigService.getConfig(RiskRuleCodes.NEW_WALLET_ADDRESS))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.NEW_WALLET_ADDRESS,
                        "신규 지갑 주소 출금",
                        true,
                        20,
                        false,
                        null,
                        "신규 지갑 주소"
                ));

        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "평균 대비 고액 출금",
                        true,
                        40,
                        false,
                        "10x",
                        "고액 출금"
                ));

        when(riskRuleConfigService.getConfig(RiskRuleCodes.FREQUENT_WITHDRAWAL_24H))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.FREQUENT_WITHDRAWAL_24H,
                        "24시간 내 반복 출금",
                        true,
                        30,
                        false,
                        "5",
                        "반복 출금"
                ));

        when(riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_RISK_WALLET))
                .thenReturn(new RiskRuleConfig(
                        RiskRuleCodes.HIGH_RISK_WALLET,
                        "고위험 지갑 주소 출금",
                        true,
                        100,
                        true,
                        null,
                        "고위험 지갑"
                ));

        RiskContext context = RiskContextFixture.builder()
                .newDeviceLoginWithin1h(true)
                .otpResetWithin24h(true)
                .newWalletAddress(true)
                .walletRisk(WalletRiskLevel.HIGH, 100, KytRiskCategory.HACKED_FUNDS)
                .build();

        //when
        List<RuleHit> hits = rules.stream()
                .map(rule -> rule.evaluate(context))
                .flatMap(Optional::stream)
                .toList();

        //then
        Assertions.assertThat(hits)
                .extracting(RuleHit::ruleCode)
                .containsExactlyInAnyOrder(
                        RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                        RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                        RiskRuleCodes.NEW_WALLET_ADDRESS,
                        RiskRuleCodes.HIGH_RISK_WALLET
                );

        Assertions.assertThat(hits)
                .extracting(RuleHit::score)
                .containsExactlyInAnyOrder(30, 40, 20, 100);

        int totalScore = hits.stream()
                .mapToInt(RuleHit::score)
                .sum();
        Assertions.assertThat(totalScore).isEqualTo(190);

        Assertions.assertThat(hits).anyMatch(RuleHit::blocking);
    }
}
