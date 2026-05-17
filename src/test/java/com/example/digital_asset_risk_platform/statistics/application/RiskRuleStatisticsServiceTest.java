package com.example.digital_asset_risk_platform.statistics.application;

import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import com.example.digital_asset_risk_platform.statistics.dto.RiskRuleStatisticsResponse;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import com.example.digital_asset_risk_platform.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(statistics.getRuleCode()).isEqualTo(ruleCode);
        assertThat(statistics.getHitCount()).isEqualTo(1L);
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

        assertThat(statistics.getHitCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Rule 통계를 hitCount 내림차순으로 조회한다")
    void case3() {
        //given
        LocalDateTime now = LocalDateTime.now();

        riskRuleStatisticsService.increaseRuleHit("OTP_RESET_WITHDRAWAL", now.minusMinutes(10));
        riskRuleStatisticsService.increaseRuleHit("HIGH_RISK_WALLET", now.minusMinutes(5));
        riskRuleStatisticsService.increaseRuleHit("HIGH_RISK_WALLET", now.minusMinutes(1));

        //when
        Page<RiskRuleStatisticsResponse> result = riskRuleStatisticsService.getStatistics(PageRequest.of(0, 10));

        //then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(RiskRuleStatisticsResponse::ruleCode)
                .containsExactly("HIGH_RISK_WALLET", "OTP_RESET_WITHDRAWAL");
        assertThat(result.getContent().get(0).hitCount()).isEqualTo(2L);
        assertThat(result.getContent().get(1).hitCount()).isEqualTo(1L);
    }
}