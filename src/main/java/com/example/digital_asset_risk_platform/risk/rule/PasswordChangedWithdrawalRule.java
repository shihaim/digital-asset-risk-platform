package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PasswordChangedWithdrawalRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        if (!context.accountRisk().passwordChangedWithin24h()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "PASSWORD_CHANGED_WITHDRAWAL",
                "비밀번호 변경 후 출금",
                30,
                "최근 24시간 이내 비밀번호 변경 후 출금 요청",
                false
        ));
    }
}
