package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OtpResetWithdrawalRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        if (!context.accountRisk().otpResetWithin24h()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "OTP_RESET_WITHDRAWAL",
                "OTP 재설정 후 출금",
                40,
                "최근 24시간 이내 OTP 재설정 후 출금 요청",
                false
        ));
    }
}
