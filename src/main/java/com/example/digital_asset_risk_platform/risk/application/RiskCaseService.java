package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.publisher.DomainEventPublisher;
import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RiskCaseService {

    private final RiskCaseRepository riskCaseRepository;
    private final DomainEventPublisher domainEventPublisher;

    public Long createCaseIfNeeded(WithdrawalRequest withdrawal, RiskEvaluationResult evaluationResult) {
        RiskDecisionType decision = evaluationResult.decision();
        boolean needCase = decision == RiskDecisionType.HOLD_WITHDRAWAL || decision == RiskDecisionType.BLOCK_WITHDRAWAL;

        if (!needCase) {
            return null;
        }

        RiskCaseType caseType = determineCaseType(evaluationResult);

        RiskCase riskCase = new RiskCase(
                evaluationResult.evaluationId(),
                withdrawal.getUserId(),
                caseType,
                evaluationResult.riskLevel()
        );

        RiskCase savedCase = riskCaseRepository.save(riskCase);

        domainEventPublisher.publish(new RiskCaseCreatedEvent(
                UUID.randomUUID().toString(),
                savedCase.getId(),
                savedCase.getEvaluationId(),
                savedCase.getUserId(),
                savedCase.getCaseType().name(),
                savedCase.getStatus().name(),
                savedCase.getRiskLevel().name(),
                savedCase.getCreatedAt(),
                LocalDateTime.now()
        ));

        return savedCase.getId();
    }

    private RiskCaseType determineCaseType(RiskEvaluationResult result) {
        boolean hasHighRiskWallet = result.ruleHits().stream()
                .anyMatch(hit -> "HIGH_RISK_WALLET".equals(hit.ruleCode()));

        if (hasHighRiskWallet) {
            return RiskCaseType.AML_REVIEW;
        }

        if (result.riskLevel() == RiskLevel.CRITICAL) {
            return RiskCaseType.ACCOUNT_TAKEOVER;
        }

        return RiskCaseType.WITHDRAWAL_FRAUD;
    }
}
