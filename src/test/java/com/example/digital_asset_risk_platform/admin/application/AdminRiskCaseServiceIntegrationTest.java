package com.example.digital_asset_risk_platform.admin.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseDetailResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseReviewRequest;
import com.example.digital_asset_risk_platform.admin.dto.RiskCaseSummaryResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineEventResponse;
import com.example.digital_asset_risk_platform.admin.dto.RuleHitResponse;
import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.application.WithdrawalService;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalStatus;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateRequest;
import com.example.digital_asset_risk_platform.wallet.dto.WithdrawalCreateResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
public class AdminRiskCaseServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    AdminRiskCaseService adminRiskCaseService;

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    RiskCaseRepository riskCaseRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @BeforeEach
    void setUp() {
        riskCaseRepository.deleteAll();
        riskRuleHitRepository.deleteAll();
        riskEvaluationRepository.deleteAll();
        withdrawalRequestRepository.deleteAll();
        walletAddressRiskRepository.deleteAll();
        accountSecurityEventRepository.deleteAll();
        accountLoginEventRepository.deleteAll();
    }

    @Test
    @DisplayName("RiskCase 목록을 상태별로 조회할 수 있다")
    void case1() {
        //given
        WithdrawalCreateResponse response = createHighRiskWithdrawal(10001L);

        //when
        List<RiskCaseSummaryResponse> cases = adminRiskCaseService.getRiskCases(RiskCaseStatus.REVIEW_REQUIRED);

        //then
        Assertions.assertThat(cases).hasSize(1);

        RiskCaseSummaryResponse summary = cases.get(0);
        Assertions.assertThat(summary.caseId()).isEqualTo(response.caseId());
        Assertions.assertThat(summary.status()).isEqualTo(RiskCaseStatus.REVIEW_REQUIRED);
        Assertions.assertThat(summary.caseType()).isEqualTo(RiskCaseType.AML_REVIEW);
    }

    @Test
    @DisplayName("RiskCase 상세 조회 시 출금 정보, 평가 정보, RuleHit, Timeline을 함께 조회한다")
    void case2() {
        //given
        WithdrawalCreateResponse response = createHighRiskWithdrawal(10001L);

        //when
        RiskCaseDetailResponse detail = adminRiskCaseService.getRiskCaseDetail(response.caseId());

        //then
        Assertions.assertThat(detail.caseId()).isEqualTo(response.caseId());
        Assertions.assertThat(detail.status()).isEqualTo(RiskCaseStatus.REVIEW_REQUIRED);
        Assertions.assertThat(detail.caseType()).isEqualTo(RiskCaseType.AML_REVIEW);

        Assertions.assertThat(detail.withdrawal()).isNotNull();
        Assertions.assertThat(detail.withdrawal().withdrawalId()).isEqualTo(response.withdrawalId());
        Assertions.assertThat(detail.withdrawal().status()).isEqualTo(WithdrawalStatus.BLOCKED);

        Assertions.assertThat(detail.evaluation()).isNotNull();
        Assertions.assertThat(detail.evaluation().totalScore()).isGreaterThanOrEqualTo(100);

        Assertions.assertThat(detail.ruleHits())
                .extracting(RuleHitResponse::ruleCode)
                .contains("HIGH_RISK_WALLET");

        Assertions.assertThat(detail.timeline()).isNotEmpty();
        Assertions.assertThat(detail.timeline())
                .extracting(RiskTimelineEventResponse::eventType)
                .contains("LOGIN", "OTP_RESET", "WITHDRAWAL_REQUESTED");
    }

    @Test
    @DisplayName("Case 승인 시 RiskCase는 APPROVED, 출금은 APPROVED로 변경된다")
    void case3() {
        //given
        WithdrawalCreateResponse response = createHeldWithdrawal(20001L);

        //when
        adminRiskCaseService.approve(response.caseId(), new RiskCaseReviewRequest("admin01", "본인 확인 완료"));

        //then
        Optional<RiskCase> riskCaseOpt = riskCaseRepository.findById(response.caseId());
        Assertions.assertThat(riskCaseOpt).isPresent();
        RiskCase riskCase = riskCaseOpt.get();

        Optional<WithdrawalRequest> withdrawalOpt = withdrawalRequestRepository.findById(response.withdrawalId());
        Assertions.assertThat(withdrawalOpt).isPresent();
        WithdrawalRequest withdrawal = withdrawalOpt.get();

        Assertions.assertThat(response.totalScore()).isEqualTo(90);
        Assertions.assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.APPROVED);
        Assertions.assertThat(riskCase.getAssignedTo()).isEqualTo("admin01");
        Assertions.assertThat(riskCase.getReviewComment()).isEqualTo("본인 확인 완료");
        Assertions.assertThat(riskCase.getClosedAt()).isNotNull();
        Assertions.assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
    }

    @Test
    @DisplayName("Case 거절 시 RiskCase는 REJECTED, 출금은 REJECTED로 변경된다")
    void case4() {
        //given
        WithdrawalCreateResponse response = createHeldWithdrawal(30001L);

        //when
        adminRiskCaseService.reject(response.caseId(), new RiskCaseReviewRequest("admin01", "피싱 피해 의심"));

        //then
        Optional<RiskCase> riskCaseOpt = riskCaseRepository.findById(response.caseId());
        Assertions.assertThat(riskCaseOpt).isPresent();
        RiskCase riskCase = riskCaseOpt.get();

        Optional<WithdrawalRequest> withdrawalOpt = withdrawalRequestRepository.findById(response.withdrawalId());
        Assertions.assertThat(withdrawalOpt).isPresent();
        WithdrawalRequest withdrawal = withdrawalOpt.get();

        Assertions.assertThat(response.totalScore()).isEqualTo(90);
        Assertions.assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.REJECTED);
        Assertions.assertThat(riskCase.getAssignedTo()).isEqualTo("admin01");
        Assertions.assertThat(riskCase.getReviewComment()).isEqualTo("피싱 피해 의심");
        Assertions.assertThat(riskCase.getClosedAt()).isNotNull();
        Assertions.assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);
    }

    @Test
    @DisplayName("오탐 처리 시 RiskCase는 FALSE_POSITIVE, 출금은 APPROVED로 변경된다")
    void case5() {
        //given
        WithdrawalCreateResponse response = createHighRiskWithdrawal(40001L);

        //when
        adminRiskCaseService.markFalsePositive(response.caseId(), new RiskCaseReviewRequest("admin01", "정상 사용자로 확인"));

        //then
        Optional<RiskCase> riskCaseOpt = riskCaseRepository.findById(response.caseId());
        Assertions.assertThat(riskCaseOpt).isPresent();
        RiskCase riskCase = riskCaseOpt.get();

        Optional<WithdrawalRequest> withdrawalOpt = withdrawalRequestRepository.findById(response.withdrawalId());
        Assertions.assertThat(withdrawalOpt).isPresent();
        WithdrawalRequest withdrawal = withdrawalOpt.get();

        Assertions.assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.FALSE_POSITIVE);
        Assertions.assertThat(riskCase.getAssignedTo()).isEqualTo("admin01");
        Assertions.assertThat(riskCase.getReviewComment()).isEqualTo("정상 사용자로 확인");
        Assertions.assertThat(riskCase.getClosedAt()).isNotNull();
        Assertions.assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.APPROVED);
    }

    @Test
    @DisplayName("정탐 처리 시 RiskCase는 TRUE_POSITIVE, 출금은 REJECTED로 변경된다")
    void case6() {
        //given
        WithdrawalCreateResponse response = createHighRiskWithdrawal(50001L);

        //when
        adminRiskCaseService.markTruePositive(response.caseId(), new RiskCaseReviewRequest("admin01", "해킹 자금 연관 주소로 확인"));

        //then
        Optional<RiskCase> riskCaseOpt = riskCaseRepository.findById(response.caseId());
        Assertions.assertThat(riskCaseOpt).isPresent();
        RiskCase riskCase = riskCaseOpt.get();

        Optional<WithdrawalRequest> withdrawalOpt = withdrawalRequestRepository.findById(response.withdrawalId());
        Assertions.assertThat(withdrawalOpt).isPresent();
        WithdrawalRequest withdrawal = withdrawalOpt.get();

        Assertions.assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.TRUE_POSITIVE);
        Assertions.assertThat(riskCase.getAssignedTo()).isEqualTo("admin01");
        Assertions.assertThat(riskCase.getReviewComment()).isEqualTo("해킹 자금 연관 주소로 확인");
        Assertions.assertThat(riskCase.getClosedAt()).isNotNull();
        Assertions.assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.REJECTED);
    }

    private WithdrawalCreateResponse createHighRiskWithdrawal(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String address = "THACKED" + userId;

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                address,
                WalletRiskLevel.HIGH,
                100,
                "HACKED_FUNDS",
                "MOCK_KYT"
        ));

        accountEventService.createLoginEvent(new LoginEventCreateRequest(
                userId,
                "new-device-" + userId,
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-" + userId,
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        return withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                "TRON",
                address,
                new BigDecimal("10000.000000000000000000")
        ));
    }

    private WithdrawalCreateResponse createHeldWithdrawal(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String address = "TNEW" + userId;

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                address,
                WalletRiskLevel.LOW,
                0,
                "NORMAL",
                "MOCK_KYT"
        ));

        accountEventService.createLoginEvent(new LoginEventCreateRequest(
                userId,
                "new-device-" + userId,
                "185.220.101.10",
                "RU",
                "Mozilla/5.0",
                now.minusMinutes(30)
        ));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(
                userId,
                SecurityEventType.OTP_RESET,
                "new-device-" + userId,
                "185.220.101.10",
                now.minusMinutes(20)
        ));

        return withdrawalService.createWithdrawal(new WithdrawalCreateRequest(
                userId,
                "USDT",
                "TRON",
                address,
                new BigDecimal("10000.000000000000000000")
        ));
    }
}
