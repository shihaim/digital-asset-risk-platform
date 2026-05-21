package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThat;

class RiskRuleConfigInitializerTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleConfigInitializer initializer;
    @Autowired
    RiskRuleConfigRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("기본 Rule 설정 7개를 초기화한다")
    void case1() throws Exception {
        //when
        initializer.run(new DefaultApplicationArguments());

        //then
        assertThat(repository.findAll())
                .extracting(RiskRuleConfig::getRuleCode)
                .containsExactly(
                        RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                        RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                        RiskRuleCodes.PASSWORD_CHANGED_WITHDRAWAL,
                        RiskRuleCodes.NEW_WALLET_ADDRESS,
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        RiskRuleCodes.FREQUENT_WITHDRAWAL_24H,
                        RiskRuleCodes.HIGH_RISK_WALLET
                );
    }

    @Test
    @DisplayName("이미 존재하는 Rule 설정은 덮어쓰지 않는다")
    void case2() throws Exception {
        //given
        repository.save(new RiskRuleConfig(
                RiskRuleCodes.HIGH_RISK_WALLET,
                "고위험 지갑 주소 출금",
                true,
                777,
                true,
                null,
                "사용자 수정값"
        ));

        //when
        initializer.run(new DefaultApplicationArguments());

        RiskRuleConfig config = repository.findByRuleCode(RiskRuleCodes.HIGH_RISK_WALLET)
                .orElseThrow();

        //then
        assertThat(config.getScore()).isEqualTo(777);
        assertThat(config.getDescription()).isEqualTo("사용자 수정값");
        assertThat(repository.findAll()).hasSize(7);
    }
}