package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FrequentWithdrawalRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        if (context.withdrawalCountLast24h() < 5) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "FREQUENT_WITHDRAWAL_24H",
                "24시간 내 반복 출금",
                30,
                "최근 24시간 내 출금 요청 횟수가 5회 이상",
                false
        ));
    }
}
