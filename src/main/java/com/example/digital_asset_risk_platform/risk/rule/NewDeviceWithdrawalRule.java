package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NewDeviceWithdrawalRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        if (!context.accountRisk().newDeviceLoginWithin1h()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "NEW_DEVICE_WITHDRAWAL",
                "신규 기기 로그인 후 출금",
                30,
                "최근 1시간 이내 신규 기기 로그인 후 출금 요청",
                false
        ));
    }
}
