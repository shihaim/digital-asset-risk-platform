package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.support.RiskContextFixture;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class RiskRuleEvaluationTest {

    private final List<RiskRule> rules = List.of(
            new NewDeviceWithdrawalRule(),
            new PasswordChangedWithdrawalRule(),
            new OtpResetWithdrawalRule(),
            new NewWalletAddressRule(),
            new HighAmountWithdrawalRule(),
            new FrequentWithdrawalRule(),
            new HighRiskWalletRule()
    );

    @Test
    @DisplayName("신규 기기, OTP 재설정, 신규 지갑, 고위험 지갑 조건이면 4개 룰이 적중한다")
    void case1() {
        //given
        RiskContext context = RiskContextFixture.builder()
                .newDeviceLoginWithin1h(true)
                .otpResetWithin24h(true)
                .newWalletAddress(true)
                .walletRisk(WalletRiskLevel.HIGH, 100, "HACKED_FUNDS")
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
                        "NEW_DEVICE_WITHDRAWAL",
                        "OTP_RESET_WITHDRAWAL",
                        "NEW_WALLET_ADDRESS",
                        "HIGH_RISK_WALLET"
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
