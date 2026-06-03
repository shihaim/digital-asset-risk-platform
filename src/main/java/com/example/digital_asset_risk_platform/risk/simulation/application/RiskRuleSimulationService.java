package com.example.digital_asset_risk_platform.risk.simulation.application;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.context.RiskContextBuilder;
import com.example.digital_asset_risk_platform.risk.decision.DecisionEngine;
import com.example.digital_asset_risk_platform.risk.decision.RiskDecision;
import com.example.digital_asset_risk_platform.risk.rule.RiskRule;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;
import com.example.digital_asset_risk_platform.risk.simulation.dto.RiskRuleSimulationRequest;
import com.example.digital_asset_risk_platform.risk.simulation.dto.RiskRuleSimulationResponse;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskRuleSimulationService {

    private final RiskContextBuilder riskContextBuilder;
    private final List<RiskRule> rules;

    private final DecisionEngine decisionEngine = new DecisionEngine();

    @Transactional(readOnly = true)
    public RiskRuleSimulationResponse simulate(RiskRuleSimulationRequest request) {
        WithdrawalRequest withdrawal = new WithdrawalRequest(
                request.userId(),
                request.assetSymbol(),
                request.chainType(),
                request.toAddress(),
                request.amount()
        );

        RiskContext context = riskContextBuilder.build(withdrawal);

        List<RuleHit> hits = rules.stream()
                .map(rule -> rule.evaluate(context))
                .flatMap(Optional::stream)
                .toList();

        RiskDecision decision = decisionEngine.decide(hits);

        log.info(
                "Risk rule simulation completed. userId={}, assetSymbol={}, chainType={}, toAddress={}, amount={}, totalScore={}, riskLevel={}, decision={}",
                request.userId(),
                request.assetSymbol(),
                request.chainType(),
                request.toAddress(),
                request.amount(),
                decision.totalScore(),
                decision.riskLevel(),
                decision.decisionType()
        );

        return RiskRuleSimulationResponse.of(decision, hits);
    }

}
