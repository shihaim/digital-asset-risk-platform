package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.event.publisher.DomainEventPublisher;
import com.example.digital_asset_risk_platform.risk.application.RiskCaseService;
import com.example.digital_asset_risk_platform.risk.application.RiskEvaluationResult;
import com.example.digital_asset_risk_platform.risk.application.RiskEvaluationService;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final RiskEvaluationService riskEvaluationService;
    private final RiskCaseService riskCaseService;
    private final DomainEventPublisher domainEventPublisher;

    public WithdrawalCreateResponse createWithdrawal(WithdrawalCreateRequest request) {
        WithdrawalRequest withdrawal = new WithdrawalRequest(
                request.userId(),
                request.assetSymbol(),
                request.chainType(),
                request.toAddress(),
                request.amount()
        );

        WithdrawalRequest savedWithdrawal = withdrawalRequestRepository.save(withdrawal);

        domainEventPublisher.publish(new WithdrawalRequestedEvent(
                UUID.randomUUID().toString(),
                savedWithdrawal.getId(),
                savedWithdrawal.getUserId(),
                savedWithdrawal.getAssetSymbol(),
                savedWithdrawal.getChainType(),
                savedWithdrawal.getToAddress(),
                savedWithdrawal.getAmount(),
                savedWithdrawal.getStatus().name(),
                savedWithdrawal.getRequestedAt(),
                LocalDateTime.now()
        ));

        RiskEvaluationResult evaluationResult = riskEvaluationService.evaluationWithdrawal(savedWithdrawal);

        applyDecision(savedWithdrawal, evaluationResult.decision());

        Long caseId = riskCaseService.createCaseIfNeeded(savedWithdrawal, evaluationResult);

        return new WithdrawalCreateResponse(
                savedWithdrawal.getId(),
                savedWithdrawal.getStatus(),
                evaluationResult.riskLevel(),
                evaluationResult.decision(),
                evaluationResult.totalScore(),
                caseId
        );
    }

    private void applyDecision(WithdrawalRequest withdrawal, RiskDecisionType decision) {
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

        throw new IllegalStateException("Unsupported decision: " + decision);
    }
}
