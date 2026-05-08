package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.event.publisher.DomainEventPublisher;
import com.example.digital_asset_risk_platform.risk.application.RiskCaseService;
import com.example.digital_asset_risk_platform.risk.application.RiskEvaluationResult;
import com.example.digital_asset_risk_platform.risk.application.RiskEvaluationService;
import com.example.digital_asset_risk_platform.risk.config.FdsEvaluationProperties;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalDetailResponse;
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
    private final WithdrawalDecisionApplier withdrawalDecisionApplier;
    private final DomainEventPublisher domainEventPublisher;
    private final FdsEvaluationProperties fdsEvaluationProperties;
    private final RiskEvaluationRepository riskEvaluationRepository;

    public WithdrawalCreateResponse createWithdrawal(WithdrawalCreateRequest request) {
        WithdrawalRequest withdrawal = new WithdrawalRequest(
                request.userId(),
                request.assetSymbol(),
                request.chainType(),
                request.toAddress(),
                request.amount()
        );

        WithdrawalRequest savedWithdrawal = withdrawalRequestRepository.save(withdrawal);

        if (fdsEvaluationProperties.isAsyncMode()) {
            savedWithdrawal.startEvaluation();

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

            return WithdrawalCreateResponse.evaluating(savedWithdrawal.getId(), savedWithdrawal.getStatus());
        }

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

        withdrawalDecisionApplier.apply(savedWithdrawal, evaluationResult.decision());

        Long caseId = riskCaseService.createCaseIfNeeded(savedWithdrawal, evaluationResult);

        return WithdrawalCreateResponse.evaluated(
                savedWithdrawal.getId(),
                savedWithdrawal.getStatus(),
                evaluationResult.riskLevel(),
                evaluationResult.decision(),
                evaluationResult.totalScore(),
                caseId
        );
    }

    @Transactional(readOnly = true)
    public WithdrawalDetailResponse getWithdrawal(Long withdrawalId) {
        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new IllegalArgumentException("출금 요청을 찾을 수 없습니다. withdrawalId=" + withdrawalId));

        return riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", withdrawal.getId())
                .map(evaluation -> WithdrawalDetailResponse.of(
                        withdrawal,
                        evaluation.getRiskLevel(),
                        evaluation.getDecision(),
                        evaluation.getTotalScore()
                ))
                .orElseGet(() -> WithdrawalDetailResponse.of(
                        withdrawal,
                        null,
                        null,
                        null
                ));
    }
}
