package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.publisher.DomainEventPublisher;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RiskEvaluationService {

    private final RiskContextBuilder riskContextBuilder;
    private final List<RiskRule> rules;
    private final DecisionEngine decisionEngine = new DecisionEngine();
    private final RiskEvaluationRepository riskEvaluationRepository;
    private final RiskRuleHitRepository riskRuleHitRepository;
    private final DomainEventPublisher domainEventPublisher;

    public RiskEvaluationResult evaluationWithdrawal(WithdrawalRequest withdrawal) {
        log.info(
                "Risk evaluation started. refType=WITHDRAWAL, withdrawalId={}, userId={}",
                withdrawal.getId(),
                withdrawal.getUserId()
        );

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

        log.info(
                "Risk evaluation completed. evaluationId={}, withdrawalId={}, userId={}, riskLevel={}, decision={}, totalScore={}",
                savedEvaluation.getId(),
                withdrawal.getId(),
                withdrawal.getUserId(),
                decision.riskLevel(),
                decision.decisionType(),
                decision.totalScore()
        );

        if (log.isDebugEnabled()) {
            hits.forEach(hit -> log.debug(
                    "Risk rule hit detail. withdrawalId={}, ruleCode={}, score={}, blocking={}, reason={}",
                    withdrawal.getId(),
                    hit.ruleCode(),
                    hit.score(),
                    hit.blocking(),
                    hit.reason()
            ));
        }

        domainEventPublisher.publish(new RiskEvaluationCompletedEvent(
                UUID.randomUUID().toString(),
                savedEvaluation.getId(),
                "WITHDRAWAL",
                withdrawal.getId(),
                withdrawal.getUserId(),
                decision.totalScore(),
                decision.riskLevel().name(),
                decision.decisionType().name(),
                hits.stream().map(RuleHit::ruleCode).toList(),
                savedEvaluation.getEvaluatedAt(),
                LocalDateTime.now()
        ));

        return new RiskEvaluationResult(
                savedEvaluation.getId(),
                decision.riskLevel(),
                decision.decisionType(),
                decision.totalScore(),
                hits
        );
    }
}
