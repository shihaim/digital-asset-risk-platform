package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.common.logging.LogMaskingUtils;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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
        log.info(
                "Withdrawal request received. userId={}, assetSymbol={}, chainType={}, toAddress={}, amount={}, evaluationMode={}",
                request.userId(),
                request.assetSymbol(),
                request.chainType(),
                LogMaskingUtils.maskAddress(request.toAddress()),
                request.amount(),
                fdsEvaluationProperties.getMode()
        );

        WithdrawalRequest withdrawal = new WithdrawalRequest(
                request.userId(),
                request.assetSymbol(),
                request.chainType(),
                request.toAddress(),
                request.amount()
        );

        WithdrawalRequest savedWithdrawal = withdrawalRequestRepository.save(withdrawal);

        log.info(
                "Withdrawal saved. withdrawalId={}, userId={}, status={}",
                savedWithdrawal.getId(),
                savedWithdrawal.getUserId(),
                savedWithdrawal.getStatus()
        );

        if (fdsEvaluationProperties.isAsyncMode()) {
            savedWithdrawal.startEvaluation();

            log.info(
                    "Withdrawal evaluation deferred. withdrawalId={}, userId={}, status={}",
                    savedWithdrawal.getId(),
                    savedWithdrawal.getUserId(),
                    savedWithdrawal.getStatus()
            );

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

        log.info(
                "Withdrawal evaluated synchronously. withdrawalId={}, userId={}, status={}, riskLevel={}, decision={}, totalScore={}, caseId={}",
                withdrawal.getId(),
                withdrawal.getUserId(),
                withdrawal.getStatus(),
                evaluationResult.riskLevel(),
                evaluationResult.decision(),
                evaluationResult.totalScore(),
                caseId
        );

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
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

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
