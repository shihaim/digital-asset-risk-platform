package com.example.digital_asset_risk_platform.risk.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.event.application.ProcessedEventService;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalDecisionApplier;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FdsWithdrawalEvaluationService {

    private static final String CONSUMER_NAME = "fds-withdrawal-consumer";

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final RiskEvaluationRepository riskEvaluationRepository;
    private final RiskEvaluationService riskEvaluationService;
    private final RiskCaseService riskCaseService;
    private final WithdrawalDecisionApplier withdrawalDecisionApplier;
    private final ProcessedEventService processedEventService;

    public void evaluate(WithdrawalRequestedEvent event) {
        log.info(
                "Async FDS evaluation event received. eventId={}, withdrawalId={}, userId={}, status={}",
                event.eventId(),
                event.withdrawalId(),
                event.userId(),
                event.status()
        );

        if (processedEventService.isProcessed(CONSUMER_NAME, event.eventId())) {
            log.warn(
                    "Duplicate FDS evaluation event skipped. consumerName={}, eventId={}, withdrawalId={}",
                    CONSUMER_NAME,
                    event.eventId(),
                    event.withdrawalId()
            );

            return;
        }

        WithdrawalRequest withdrawal = withdrawalRequestRepository
                .findById(event.withdrawalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", withdrawal.getId())) {
            processedEventService.markProcessed(CONSUMER_NAME, event.eventId());

            log.warn(
                    "Withdrawal already evaluated. eventId={}, withdrawalId={}, status={}",
                    event.eventId(),
                    withdrawal.getId(),
                    withdrawal.getStatus()
            );

            return;
        }

        if (withdrawal.getStatus() != WithdrawalStatus.EVALUATING && withdrawal.getStatus() != WithdrawalStatus.REQUESTED) {
            processedEventService.markProcessed(CONSUMER_NAME, event.eventId());

            log.warn(
                    "FDS evaluation skipped by withdrawal status. eventId={}, withdrawalId={}, status={}",
                    event.eventId(),
                    withdrawal.getId(),
                    withdrawal.getStatus()
            );

            return;
        }

        RiskEvaluationResult evaluationResult = riskEvaluationService.evaluationWithdrawal(withdrawal);

        withdrawalDecisionApplier.apply(withdrawal, evaluationResult.decision());

        Long caseId = riskCaseService.createCaseIfNeeded(withdrawal, evaluationResult);

        processedEventService.markProcessed(CONSUMER_NAME, event.eventId());

        log.info(
                "Async FDS evaluation completed. eventId={}, withdrawalId={}, userId={}, status={}, riskLevel={}, decision={}, totalScore={}, caseId={}",
                event.eventId(),
                withdrawal.getId(),
                withdrawal.getUserId(),
                withdrawal.getStatus(),
                evaluationResult.riskLevel(),
                evaluationResult.decision(),
                evaluationResult.totalScore(),
                caseId
        );
    }
}
