package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfigHistory;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigResponse;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigUpdateRequest;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigHistoryRepository;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskRuleConfigAdminServiceTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleConfigAdminService riskRuleConfigAdminService;

    @Autowired
    RiskRuleConfigRepository riskRuleConfigRepository;

    @Autowired
    RiskRuleConfigHistoryRepository riskRuleConfigHistoryRepository;

    @BeforeEach
    void setUp() {
        riskRuleConfigRepository.deleteAll();
        riskRuleConfigHistoryRepository.deleteAll();
    }

    @Test
    @DisplayName("Rule 설정 목록을 조회한다")
    void case1() {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        ));

        //when
        List<RiskRuleConfigResponse> result = riskRuleConfigAdminService.getRuleConfigs();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).ruleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
    }

    @Test
    @DisplayName("Rule 설정 상세를 조회한다")
    void case2() {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                "OTP 재설정 후 출금",
                true,
                40,
                false,
                "24h",
                "OTP 재설정"
        ));

        //when
        RiskRuleConfigResponse response = riskRuleConfigAdminService.getRuleConfig(RiskRuleCodes.OTP_RESET_WITHDRAWAL);

        //then
        assertThat(response.ruleCode()).isEqualTo(RiskRuleCodes.OTP_RESET_WITHDRAWAL);
        assertThat(response.thresholdValue()).isEqualTo("24h");
    }

    @Test
    @DisplayName("Rule 설정을 수정한다")
    void case3() {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        ));

        RiskRuleConfigUpdateRequest request = new RiskRuleConfigUpdateRequest(
                false,
                80,
                false,
                null,
                "비활성화 테스트",
                "admin",
                "Rule 설정 테스트"
        );

        //when
        RiskRuleConfigResponse response = riskRuleConfigAdminService.updateRuleConfig(RiskRuleCodes.HIGH_RISK_WALLET, request);

        //then
        assertThat(response.enabled()).isFalse();
        assertThat(response.score()).isEqualTo(80);
        assertThat(response.blocking()).isFalse();
        assertThat(response.description()).isEqualTo("비활성화 테스트");
    }

    @Test
    @DisplayName("Rule 설정을 수정하면 변경 전/후 이력이 저장된다")
    void case4() {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 주소로 출금하는 경우 차단"
        ));

        RiskRuleConfigUpdateRequest request = new RiskRuleConfigUpdateRequest(
                true,
                80,
                true,
                null,
                "오탐 감소를 위해 점수를 조정",
                "admin",
                "고위험 지갑 Rule 점수 조정"
        );

        //when
        RiskRuleConfigResponse response = riskRuleConfigAdminService.updateRuleConfig(RiskRuleCodes.HIGH_RISK_WALLET, request);

        //then
        assertThat(response.ruleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
        assertThat(response.score()).isEqualTo(80);
        assertThat(response.description()).isEqualTo("오탐 감소를 위해 점수를 조정");

        List<RiskRuleConfigHistory> histories = riskRuleConfigHistoryRepository.findByRuleCodeOrderByChangedAtDesc(RiskRuleCodes.HIGH_RISK_WALLET);
        assertThat(histories).hasSize(1);

        RiskRuleConfigHistory history = histories.get(0);
        assertThat(history.getRuleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
        assertThat(history.getRuleName()).isEqualTo("고위험 지갑 주소 출금");

        assertThat(history.isBeforeBlocking()).isTrue();
        assertThat(history.isAfterBlocking()).isTrue();

        assertThat(history.getBeforeScore()).isEqualTo(100);
        assertThat(history.getAfterScore()).isEqualTo(80);

        assertThat(history.getBeforeDescription()).isEqualTo("고위험 지갑 주소로 출금하는 경우 차단");
        assertThat(history.getAfterDescription()).isEqualTo("오탐 감소를 위해 점수를 조정");

        assertThat(history.getChangedBy()).isEqualTo("admin");
        assertThat(history.getChangeReason()).isEqualTo("고위험 지갑 Rule 점수 조정");
        assertThat(history.getChangedAt()).isNotNull();
    }

    @Test
    @DisplayName("Rule 설정을 수정하면 변경 전/후 이력이 저장된다")
    void case5() {
        //given
        RiskRuleConfigUpdateRequest request = new RiskRuleConfigUpdateRequest(
                true,
                80,
                true,
                null,
                "설명 변경",
                "admin",
                "존재하지 않는 Rule 수정 시도"
        );

        //when&then
        assertThatThrownBy(() -> riskRuleConfigAdminService.updateRuleConfig("NOT_EXISTS_RULE", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.RISK_RULE_CONFIG_NOT_FOUND.getMessage());

        List<RiskRuleConfigHistory> histories = riskRuleConfigHistoryRepository.findAll();
        assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName("Rule 변경 이력은 최신 변경 시각순으로 조회한다")
    void case6() throws InterruptedException {
        //given
        riskRuleConfigRepository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 주소로 출금하는 경우 차단"
        ));

        RiskRuleConfigUpdateRequest oldRequest = new RiskRuleConfigUpdateRequest(
                true,
                90,
                true,
                null,
                "첫 번째 변경",
                "z-admin",
                "오래된 변경"
        );
        riskRuleConfigAdminService.updateRuleConfig(RiskRuleCodes.HIGH_RISK_WALLET, oldRequest);

        Thread.sleep(5);

        RiskRuleConfigUpdateRequest latestRequest = new RiskRuleConfigUpdateRequest(
                true,
                80,
                true,
                null,
                "두 번째 변경",
                "a-admin",
                "최신 변경"
        );
        riskRuleConfigAdminService.updateRuleConfig(RiskRuleCodes.HIGH_RISK_WALLET, latestRequest);

        //when
        List<RiskRuleConfigHistory> histories = riskRuleConfigHistoryRepository.findByRuleCodeOrderByChangedAtDesc(RiskRuleCodes.HIGH_RISK_WALLET);

        //then
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getChangedBy()).isEqualTo("a-admin");
        assertThat(histories.get(0).getChangeReason()).isEqualTo("최신 변경");
        assertThat(histories.get(1).getChangedBy()).isEqualTo("z-admin");
        assertThat(histories.get(1).getChangeReason()).isEqualTo("오래된 변경");
    }
}
