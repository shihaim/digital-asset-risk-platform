package com.example.digital_asset_risk_platform.admin.application;

import com.example.digital_asset_risk_platform.admin.dto.RiskDashboardSummaryResponse;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskDashboardService {

    private final RiskCaseRepository riskCaseRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    public RiskDashboardSummaryResponse getSummary() {
        long reviewRequiredCaseCount = riskCaseRepository.countByStatus(RiskCaseStatus.REVIEW_REQUIRED);
        long inReviewCaseCount = riskCaseRepository.countByStatus(RiskCaseStatus.IN_REVIEW);
        long approvedCaseCount = riskCaseRepository.countByStatus(RiskCaseStatus.APPROVED);
        long rejectedCaseCount = riskCaseRepository.countByStatus(RiskCaseStatus.REJECTED);
        long falsePositiveCaseCount = riskCaseRepository.countByStatus(RiskCaseStatus.FALSE_POSITIVE);
        long truePositiveCaseCount = riskCaseRepository.countByStatus(RiskCaseStatus.TRUE_POSITIVE);
        long heldWithdrawalCount = withdrawalRequestRepository.countByStatus(WithdrawalStatus.HELD);
        long blockedWithdrawalCount = withdrawalRequestRepository.countByStatus(WithdrawalStatus.BLOCKED);

        return new RiskDashboardSummaryResponse(
                reviewRequiredCaseCount,
                inReviewCaseCount,
                approvedCaseCount,
                rejectedCaseCount,
                falsePositiveCaseCount,
                truePositiveCaseCount,
                heldWithdrawalCount,
                blockedWithdrawalCount
        );
    }
}
