package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class RiskCaseService {

    private final RiskCaseRepository riskCaseRepository;

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

        return riskCaseRepository.save(riskCase).getId();
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
