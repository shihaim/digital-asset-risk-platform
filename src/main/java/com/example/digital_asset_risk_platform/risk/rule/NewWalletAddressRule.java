package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NewWalletAddressRule implements RiskRule {
    @Override
    public Optional<RuleHit> evaluate(RiskContext context) {
        if (!context.isNewWalletAddress()) {
            return Optional.empty();
        }

        return Optional.of(new RuleHit(
                "NEW_WALLET_ADDRESS",
                "신규 지갑 주소 출금",
                20,
                "사용자가 과거에 출금한 적 없는 신규 지갑 주소",
                false
        ));
    }
}
