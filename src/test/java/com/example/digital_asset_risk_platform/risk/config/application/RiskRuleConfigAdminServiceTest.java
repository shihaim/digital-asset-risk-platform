package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigResponse;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigUpdateRequest;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskRuleConfigAdminServiceTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleConfigAdminService adminService;

    @Autowired
    RiskRuleConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Rule 설정 목록을 조회한다")
    void case1() {
        //given
        repository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                100,
                true,
                null,
                "고위험 지갑 차단"
        ));

        //when
        List<RiskRuleConfigResponse> result = adminService.getRuleConfigs();

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).ruleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
    }

    @Test
    @DisplayName("Rule 설정 상세를 조회한다")
    void case2() {
        //given
        repository.save(new RiskRuleConfig(
                RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                "OTP 재설정 후 출금",
                true,
                40,
                false,
                "24h",
                "OTP 재설정"
        ));

        //when
        RiskRuleConfigResponse response = adminService.getRuleConfig(RiskRuleCodes.OTP_RESET_WITHDRAWAL);

        //then
        assertThat(response.ruleCode()).isEqualTo(RiskRuleCodes.OTP_RESET_WITHDRAWAL);
        assertThat(response.thresholdValue()).isEqualTo("24h");
    }

    @Test
    @DisplayName("Rule 설정을 수정한다")
    void case3() {
        //given
        repository.save(new RiskRuleConfig(
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
                "비활성화 테스트"
        );

        //when
        RiskRuleConfigResponse response = adminService.updateRuleConfig(RiskRuleCodes.HIGH_RISK_WALLET, request);

        //then
        assertThat(response.enabled()).isFalse();
        assertThat(response.score()).isEqualTo(80);
        assertThat(response.blocking()).isFalse();
        assertThat(response.description()).isEqualTo("비활성화 테스트");
    }
}