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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (processedEventService.isProcessed(CONSUMER_NAME, event.eventId())) {
            return;
        }

        WithdrawalRequest withdrawal = withdrawalRequestRepository
                .findById(event.withdrawalId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (riskEvaluationRepository.existsByRefTypeAndRefId("WITHDRAWAL", withdrawal.getId())) {
            processedEventService.markProcessed(CONSUMER_NAME, event.eventId());
            return;
        }

        if (withdrawal.getStatus() != WithdrawalStatus.EVALUATING && withdrawal.getStatus() != WithdrawalStatus.REQUESTED) {
            processedEventService.markProcessed(CONSUMER_NAME, event.eventId());
            return;
        }

        RiskEvaluationResult evaluationResult = riskEvaluationService.evaluationWithdrawal(withdrawal);

        withdrawalDecisionApplier.apply(withdrawal, evaluationResult.decision());

        riskCaseService.createCaseIfNeeded(withdrawal, evaluationResult);

        processedEventService.markProcessed(CONSUMER_NAME, event.eventId());
    }
}
