package com.example.digital_asset_risk_platform.admin.application;

import com.example.digital_asset_risk_platform.admin.dto.RiskCaseDetailResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseReviewRequest;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseSummaryResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskEvaluationInfoResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineEventResponse;
import com.example.digital_asset_risk_platform.admin.dto.RuleHitResponse;
import com.example.digital_asset_risk_platform.admin.dto.WithdrawalInfoResponse;
import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminRiskCaseService {

    private final RiskCaseRepository riskCaseRepository;
    private final RiskEvaluationRepository riskEvaluationRepository;
    private final RiskRuleHitRepository riskRuleHitRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final RiskTimelineService riskTimelineService;

    @Transactional(readOnly = true)
    public List<RiskCaseSummaryResponse> getRiskCases(RiskCaseStatus status) {
        List<RiskCase> cases = status == null ? riskCaseRepository.findAllByOrderByCreatedAtDesc() : riskCaseRepository.findByStatusOrderByCreatedAtDesc(status);

        return cases.stream()
                .map(RiskCaseSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskCaseDetailResponse getRiskCaseDetail(Long caseId) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_CASE_NOT_FOUND));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVALUATION_NOT_FOUND));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        List<RiskRuleHit> ruleHits = riskRuleHitRepository.findByEvaluationId(evaluation.getId());

        List<RiskTimelineEventResponse> events = riskTimelineService.getUserTimeline(riskCase.getUserId()).events();

        return RiskCaseDetailResponse.of(
                riskCase,
                WithdrawalInfoResponse.from(withdrawal),
                RiskEvaluationInfoResponse.from(evaluation),
                ruleHits.stream().map(RuleHitResponse::from).toList(),
                events
        );
    }

    public void startReview(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_CASE_NOT_FOUND));

        riskCase.startReview(request.reviewer());
    }

    public void approve(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_CASE_NOT_FOUND));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVALUATION_NOT_FOUND));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        riskCase.approve(request.reviewer(), request.comment());
        withdrawal.approve();
    }

    public void reject(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_CASE_NOT_FOUND));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVALUATION_NOT_FOUND));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        riskCase.reject(request.reviewer(), request.comment());
        withdrawal.reject();
    }

    public void markFalsePositive(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_CASE_NOT_FOUND));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVALUATION_NOT_FOUND));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        riskCase.markFalsePositive(request.reviewer(), request.comment());
        withdrawal.approve();
    }

    public void markTruePositive(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_CASE_NOT_FOUND));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_EVALUATION_NOT_FOUND));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        riskCase.markTruePositive(request.reviewer(), request.comment());
        withdrawal.reject();
    }
}
