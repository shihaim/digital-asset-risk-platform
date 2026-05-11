package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalDecisionApplier {

    public void apply(WithdrawalRequest withdrawal, RiskDecisionType decision) {
        if (decision == RiskDecisionType.ALLOW || decision == RiskDecisionType.MONITOR) {
            withdrawal.approve();
            return;
        }

        if (decision == RiskDecisionType.REQUIRE_ADDITIONAL_AUTH || decision == RiskDecisionType.HOLD_WITHDRAWAL) {
            withdrawal.hold();
            return;
        }

        if (decision == RiskDecisionType.BLOCK_WITHDRAWAL) {
            withdrawal.block();
            return;
        }

        throw new BusinessException(ErrorCode.UNSUPPORTED_RISK_DECISION, "지원하지 않는 위험 판단 결과입니다. decision=" + decision);
    }
}
