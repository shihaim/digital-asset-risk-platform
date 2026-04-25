package com.example.digital_asset_risk_platform.risk.rule;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;

import java.util.Optional;

public interface RiskRule {

    Optional<RuleHit> evaluate(RiskContext context);
}
