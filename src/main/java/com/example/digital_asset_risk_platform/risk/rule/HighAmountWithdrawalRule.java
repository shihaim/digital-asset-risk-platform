package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.config.application.RiskRuleConfigService;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HighAmountWithdrawalRule implements RiskRule {

    private final RiskRuleConfigService riskRuleConfigService;

    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        RiskRuleConfig config = riskRuleConfigService.getConfig(RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL);

        if (!config.isEnabled()) {
            return Optional.empty();
        }

        BigDecimal average = context.averageWithdrawalAmount();
        if (average == null || average.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String thresholdValue = config.getThresholdValue();
        BigDecimal threshold;
        if (thresholdValue != null && thresholdValue.trim().toLowerCase().endsWith("x")) {
            threshold = average.multiply(config.getMultiplierThreshold(BigDecimal.TEN));
        } else {
            threshold = config.getThresholdAsBigDecimal(average.multiply(BigDecimal.TEN));
        }

        BigDecimal currentAmount = context.withdrawal().getAmount();

        if (currentAmount.compareTo(threshold) < 0) {
            return Optional.empty();
        }

        String reason = config.getDescription();
        if (reason == null || reason.isBlank()) {
            reason = "이번 출금액이 사용자 평균 출금액의 10배 이상";
        }

        return Optional.of(new RuleHit(
                config.getRuleCode(),
                config.getRuleName(),
                config.getScore(),
                reason,
                config.isBlocking()
        ));
    }
}
