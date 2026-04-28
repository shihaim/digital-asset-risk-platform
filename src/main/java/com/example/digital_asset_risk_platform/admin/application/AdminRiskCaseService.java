package com.example.digital_asset_risk_platform.admin.application;

import com.example.digital_asset_risk_platform.admin.dto.RiskCaseDetailResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseReviewRequest;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseSummaryResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskEvaluationInfoResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineEventResponse;
import com.example.digital_asset_risk_platform.admin.dto.RuleHitResponse;
import com.example.digital_asset_risk_platform.admin.dto.WithdrawalInfoResponse;
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
                .orElseThrow(() -> new IllegalArgumentException("RiskCase를 찾을 수 없습니다. caseId=" + caseId));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new IllegalArgumentException("RiskEvaluation을 찾을 수 없습니다. evaluationId=" + riskCase.getEvaluationId()));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new IllegalArgumentException("출금 요청을 찾을 수 없습니다. withdrawalId=" + evaluation.getRefId()));

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
                .orElseThrow(() -> new IllegalArgumentException("RiskCase를 찾을 수 없습니다. caseId=" + caseId));

        riskCase.startReview(request.reviewer());
    }

    public void approve(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("RiskCase를 찾을 수 없습니다. caseId=" + caseId));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new IllegalArgumentException("RiskEvaluation을 찾을 수 없습니다. evaluationId=" + riskCase.getEvaluationId()));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new IllegalArgumentException("출금 요청을 찾을 수 없습니다. withdrawalId=" + evaluation.getRefId()));

        riskCase.approve(request.reviewer(), request.comment());
        withdrawal.approve();
    }

    public void reject(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("RiskCase를 찾을 수 없습니다. caseId=" + caseId));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new IllegalArgumentException("RiskEvaluation을 찾을 수 없습니다. evaluationId=" + riskCase.getEvaluationId()));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new IllegalArgumentException("출금 요청을 찾을 수 없습니다. withdrawalId=" + evaluation.getRefId()));

        riskCase.reject(request.reviewer(), request.comment());
        withdrawal.reject();
    }

    public void markFalsePositive(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("RiskCase를 찾을 수 없습니다. caseId=" + caseId));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new IllegalArgumentException("RiskEvaluation을 찾을 수 없습니다. evaluationId=" + riskCase.getEvaluationId()));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new IllegalArgumentException("출금 요청을 찾을 수 없습니다. withdrawalId=" + evaluation.getRefId()));

        riskCase.markFalsePositive(request.reviewer(), request.comment());
        withdrawal.approve();
    }

    public void markTruePositive(Long caseId, RiskCaseReviewRequest request) {
        RiskCase riskCase = riskCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("RiskCase를 찾을 수 없습니다. caseId=" + caseId));

        RiskEvaluation evaluation = riskEvaluationRepository.findById(riskCase.getEvaluationId())
                .orElseThrow(() -> new IllegalArgumentException("RiskEvaluation을 찾을 수 없습니다. evaluationId=" + riskCase.getEvaluationId()));

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(evaluation.getRefId())
                .orElseThrow(() -> new IllegalArgumentException("출금 요청을 찾을 수 없습니다. withdrawalId=" + evaluation.getRefId()));

        riskCase.markTruePositive(request.reviewer(), request.comment());
        withdrawal.reject();
    }
}
