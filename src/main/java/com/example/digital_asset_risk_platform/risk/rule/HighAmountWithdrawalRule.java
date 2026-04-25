package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class HighAmountWithdrawalRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        BigDecimal average = context.averageWithdrawalAmount();

        if (average == null || average.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        BigDecimal threshold = average.multiply(BigDecimal.TEN);
        BigDecimal currentAmount = context.withdrawal().getAmount();

        if (currentAmount.compareTo(threshold) < 0) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "HIGH_AMOUNT_WITHDRAWAL",
                "평균 대비 고액 출금",
                40,
                "이번 출금액이 사용자 평균 출금액의 10배 이상",
                false
        ));
    }
}
