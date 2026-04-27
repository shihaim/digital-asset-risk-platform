package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.IntegrationTestSupport;
import com.example.digital_asset_risk_platform.account.application.AccountEventService;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.dto.LoginEventCreateRequest;
import com.example.digital_asset_risk_platform.account.dto.SecurityEventCreateRequest;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseType;
import com.example.digital_asset_risk_platform.risk.domain.RiskDecisionType;
import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import com.example.digital_asset_risk_platform.risk.domain.RiskLevel;
import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import com.example.digital_asset_risk_platform.risk.repository.RiskCaseRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskEvaluationRepository;
import com.example.digital_asset_risk_platform.risk.repository.RiskRuleHitRepository;
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
class WithdrawalServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    WithdrawalService withdrawalService;

    @Autowired
    AccountEventService accountEventService;

    @Autowired
    WalletRiskService walletRiskService;

    @Autowired
    WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    WalletAddressRiskRepository walletAddressRiskRepository;

    @Autowired
    AccountLoginEventRepository accountLoginEventRepository;

    @Autowired
    AccountSecurityEventRepository accountSecurityEventRepository;

    @Autowired
    RiskEvaluationRepository riskEvaluationRepository;

    @Autowired
    RiskRuleHitRepository riskRuleHitRepository;

    @Autowired
    RiskCaseRepository riskCaseRepository;

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
    @DisplayName("위험 요소가 없으면 출금을 승인하고 RiskCase를 생성하지 않는다")
    void case1() {
        //given
        Long userId = 10001L;

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest(
                "TRON",
                "TNORMAL000001",
                WalletRiskLevel.LOW,
                0,
                "NORMAL",
                "MOCK_KYT"
        ));

        WithdrawalRequest previousWithdrawal = new WithdrawalRequest(
                userId,
                "USDT",
                "TRON",
                "TNORMAL000001",
                new BigDecimal("100.000000000000000000")
        );
        previousWithdrawal.approve();
        withdrawalRequestRepository.save(previousWithdrawal);

        //when
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(
                new WithdrawalCreateRequest(
                        userId,
                        "USDT",
                        "TRON",
                        "TNORMAL000001",
                        new BigDecimal("100.000000000000000000")
                )
        );

        //then
        Assertions.assertThat(response.status()).isEqualTo(WithdrawalStatus.APPROVED);
        Assertions.assertThat(response.riskLevel()).isEqualTo(RiskLevel.NORMAL);
        Assertions.assertThat(response.decision()).isEqualTo(RiskDecisionType.ALLOW);
        Assertions.assertThat(response.totalScore()).isZero();
        Assertions.assertThat(response.caseId()).isNull();

        Optional<RiskEvaluation> findEvaluation = riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId());
        Assertions.assertThat(findEvaluation).isPresent();

        RiskEvaluation evaluation = findEvaluation.get();
        Assertions.assertThat(evaluation.getRiskLevel()).isEqualTo(RiskLevel.NORMAL);
        Assertions.assertThat(evaluation.getDecision()).isEqualTo(RiskDecisionType.ALLOW);
        Assertions.assertThat(evaluation.getTotalScore()).isZero();

        List<RiskRuleHit> ruleHits = riskRuleHitRepository.findByEvaluationId(evaluation.getId());
        Assertions.assertThat(ruleHits).isEmpty();

        Optional<RiskCase> riskCase = riskCaseRepository.findByEvaluationId(evaluation.getId());
        Assertions.assertThat(riskCase).isEmpty();
    }

    @Test
    @DisplayName("신규 기기와 OTP 재설정 후 신규 지갑으로 출금하면 출금을 보류하고 RiskCase를 생성한다")
    void case2() {
        //given
        Long userId = 20001L;
        LocalDateTime now = LocalDateTime.now();

        accountEventService.createLoginEvent(new LoginEventCreateRequest(userId, "new-device-001", "185.220.101.10", "RU", "Mozilla/5.0", now.minusMinutes(30)));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(userId, SecurityEventType.OTP_RESET, "new-device-001", "185.220.101.10", now.minusMinutes(20)));

        //when
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(userId, "USDT", "TRON", "TNEW000001", new BigDecimal("10000.000000000000000000")));

        //then
        Assertions.assertThat(response.status()).isEqualTo(WithdrawalStatus.HELD);
        Assertions.assertThat(response.riskLevel()).isEqualTo(RiskLevel.HIGH);
        Assertions.assertThat(response.decision()).isEqualTo(RiskDecisionType.HOLD_WITHDRAWAL);
        Assertions.assertThat(response.totalScore()).isEqualTo(90);
        Assertions.assertThat(response.caseId()).isNotNull();

        Optional<RiskEvaluation> evaluationOpt = riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId());
        Assertions.assertThat(evaluationOpt).isPresent();
        RiskEvaluation evaluation = evaluationOpt.get();

        List<RiskRuleHit> ruleHits = riskRuleHitRepository.findByEvaluationId(evaluation.getId());

        Assertions.assertThat(ruleHits)
                .extracting(RiskRuleHit::getRuleCode)
                .containsExactlyInAnyOrder(
                        "NEW_DEVICE_WITHDRAWAL",
                        "OTP_RESET_WITHDRAWAL",
                        "NEW_WALLET_ADDRESS"
                );

        Optional<RiskCase> riskCaseOpt = riskCaseRepository.findByEvaluationId(evaluation.getId());
        Assertions.assertThat(riskCaseOpt).isPresent();
        RiskCase riskCase = riskCaseOpt.get();

        Assertions.assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.REVIEW_REQUIRED);
        Assertions.assertThat(riskCase.getCaseType()).isEqualTo(RiskCaseType.WITHDRAWAL_FRAUD);
        Assertions.assertThat(riskCase.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
    }

    @Test
    @DisplayName("고위험 지갑으로 출금하면 출금을 차단하고 AML Case를 생성한다")
    void case3() {
        //given
        Long userId = 30001L;
        LocalDateTime now = LocalDateTime.now();

        walletRiskService.createWalletRisk(new WalletRiskCreateRequest("TRON", "THACKED000001", WalletRiskLevel.HIGH, 100, "HACKED_FUNDS", "MOCK_KYT"));

        accountEventService.createLoginEvent(new LoginEventCreateRequest(userId, "new-device-001", "185.220.101.10", "RU", "Mozilla/5.0", now.minusMinutes(30)));

        accountEventService.createSecurityEvent(new SecurityEventCreateRequest(userId, SecurityEventType.OTP_RESET, "new-device-001", "185.220.101.10", now.minusMinutes(20)));

        //when
        WithdrawalCreateResponse response = withdrawalService.createWithdrawal(new WithdrawalCreateRequest(userId, "USDT", "TRON", "THACKED000001", new BigDecimal("10000.000000000000000000")));

        //then
        Assertions.assertThat(response.status()).isEqualTo(WithdrawalStatus.BLOCKED);
        Assertions.assertThat(response.riskLevel()).isEqualTo(RiskLevel.CRITICAL);
        Assertions.assertThat(response.decision()).isEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);
        Assertions.assertThat(response.totalScore()).isEqualTo(190);
        Assertions.assertThat(response.caseId()).isNotNull();

        Optional<WithdrawalRequest> withdrawalOpt = withdrawalRequestRepository.findById(response.withdrawalId());
        Assertions.assertThat(withdrawalOpt).isPresent();
        WithdrawalRequest withdrawal = withdrawalOpt.get();

        Assertions.assertThat(withdrawal.getStatus()).isEqualTo(WithdrawalStatus.BLOCKED);

        Optional<RiskEvaluation> evaluationOpt = riskEvaluationRepository.findByRefTypeAndRefId("WITHDRAWAL", response.withdrawalId());
        Assertions.assertThat(evaluationOpt).isPresent();
        RiskEvaluation evaluation = evaluationOpt.get();

        Assertions.assertThat(evaluation.getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
        Assertions.assertThat(evaluation.getDecision()).isEqualTo(RiskDecisionType.BLOCK_WITHDRAWAL);
        Assertions.assertThat(evaluation.getTotalScore()).isEqualTo(190);

        List<RiskRuleHit> ruleHits = riskRuleHitRepository.findByEvaluationId(evaluation.getId());

        Assertions.assertThat(ruleHits)
                .extracting(RiskRuleHit::getRuleCode)
                .containsExactlyInAnyOrder(
                        "NEW_DEVICE_WITHDRAWAL",
                        "OTP_RESET_WITHDRAWAL",
                        "NEW_WALLET_ADDRESS",
                        "HIGH_RISK_WALLET"
                );

        Assertions.assertThat(ruleHits)
                .filteredOn(hit -> "Y".equals(hit.getBlockingYn()))
                .extracting(RiskRuleHit::getRuleCode)
                .containsExactly("HIGH_RISK_WALLET");

        Optional<RiskCase> riskCaseOpt = riskCaseRepository.findByEvaluationId(evaluation.getId());
        Assertions.assertThat(riskCaseOpt).isPresent();
        RiskCase riskCase = riskCaseOpt.get();

        Assertions.assertThat(riskCase.getStatus()).isEqualTo(RiskCaseStatus.REVIEW_REQUIRED);
        Assertions.assertThat(riskCase.getCaseType()).isEqualTo(RiskCaseType.AML_REVIEW);
        Assertions.assertThat(riskCase.getRiskLevel()).isEqualTo(RiskLevel.CRITICAL);
    }
}