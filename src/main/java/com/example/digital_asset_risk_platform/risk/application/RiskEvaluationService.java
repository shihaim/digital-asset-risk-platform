package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.risk.context.RiskContext;
import com.example.digital_asset_risk_platform.risk.context.RiskContextBuilder;
import com.example.digital_asset_risk_platform.risk.decision.DecisionEngine;
import com.example.digital_asset_risk_platform.risk.decision.RiskDecision;
import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRule;
import com.example.digital_asset_risk_platform.risk.rule.RuleHit;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskEvaluationService {

    private final RiskContextBuilder riskContextBuilder;
    private final List<RiskRule> rules;
    private final DecisionEngine decisionEngine = new DecisionEngine();
    private final RiskEvaluationRepository riskEvaluationRepository;
    private final RiskRuleHitRepository riskRuleHitRepository;

    public RiskEvaluationResult evaluationWithdrawal(WithdrawalRequest withdrawal) {
        RiskContext context = riskContextBuilder.build(withdrawal);

        List<RuleHit> hits = rules.stream()
                .map(rule -> rule.evaluate(context))
                .flatMap(Optional::stream)
                .toList();

        RiskDecision decision = decisionEngine.decide(hits);

        RiskEvaluation evaluation = new RiskEvaluation(
                "WITHDRAWAL",
                withdrawal.getId(),
                withdrawal.getUserId(),
                decision.totalScore(),
                decision.riskLevel(),
                decision.decisionType()
        );

        RiskEvaluation savedEvaluation = riskEvaluationRepository.save(evaluation);

        List<RiskRuleHit> ruleHitEntities = hits.stream()
                .map(hit -> new RiskRuleHit(
                        savedEvaluation.getId(),
                        hit.ruleCode(),
                        hit.ruleName(),
                        hit.score(),
                        hit.reason(),
                        hit.blocking()
                ))
                .toList();

        riskRuleHitRepository.saveAll(ruleHitEntities);

        return new RiskEvaluationResult(
                savedEvaluation.getId(),
                decision.riskLevel(),
                decision.decisionType(),
                decision.totalScore(),
                hits
        );
    }
}
