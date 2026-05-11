package com.example.digital_asset_risk_platform.statistics.application;

import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@ActiveProfiles("test")
class RiskRuleStatisticsServiceTest extends IntegrationTestSupport {

    @Autowired
    RiskRuleStatisticsService riskRuleStatisticsService;

    @Autowired
    RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    @BeforeEach
    void setUp() {
        riskRuleStatisticsRepository.deleteAll();
    }

    @Test
    @DisplayName("처음 적중한 Rule이면 통계를 생성한다")
    void case1() {
        //given
        String ruleCode = "HIGH_RISK_WALLET";

        //when
        riskRuleStatisticsService.increaseRuleHit(ruleCode, LocalDateTime.now());

        //then
        RiskRuleStatistics statistics = riskRuleStatisticsRepository.findByRuleCode(ruleCode).orElseThrow();

        Assertions.assertThat(statistics.getRuleCode()).isEqualTo(ruleCode);
        Assertions.assertThat(statistics.getHitCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("이미 존재하는 Rule 통계는 hitCount를 증가시킨다")
    void case2() {
        //given
        String ruleCode = "HIGH_RISK_WALLET";

        //when
        riskRuleStatisticsService.increaseRuleHit(ruleCode, LocalDateTime.now());
        riskRuleStatisticsService.increaseRuleHit(ruleCode, LocalDateTime.now());

        //then
        RiskRuleStatistics statistics = riskRuleStatisticsRepository.findByRuleCode(ruleCode).orElseThrow();

        Assertions.assertThat(statistics.getHitCount()).isEqualTo(2L);
    }
}