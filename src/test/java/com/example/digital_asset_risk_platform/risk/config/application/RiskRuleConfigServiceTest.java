package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.risk.support.RiskRuleConfigFixture;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskRuleConfigServiceTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleConfigService service;

    @Autowired
    RiskRuleConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        RiskRuleConfigFixture.resetDefaultConfigs(repository);
    }

    @Test
    @DisplayName("ruleCode로 Rule 설정을 조회한다")
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
        RiskRuleConfig config = service.getConfig(RiskRuleCodes.HIGH_RISK_WALLET);

        //then
        assertThat(config.getRuleCode()).isEqualTo(RiskRuleCodes.HIGH_RISK_WALLET);
        assertThat(config.getScore()).isEqualTo(100);
        assertThat(config.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Rule 설정이 없으면 예외가 발생한다")
    void case2() {
        //when&then
        assertThatThrownBy(() -> service.getConfig(RiskRuleCodes.HIGH_RISK_WALLET))
                .isInstanceOf(BusinessException.class);
    }
}
