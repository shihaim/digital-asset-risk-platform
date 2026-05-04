package com.example.digital_asset_risk_platform.statistics.application;

import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import com.example.digital_asset_risk_platform.statistics.repository.RiskRuleStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RiskRuleStatisticsService {

    private final RiskRuleStatisticsRepository riskRuleStatisticsRepository;

    public void increaseRuleHit(String ruleCode, LocalDateTime hitAt) {
        riskRuleStatisticsRepository.findByRuleCode(ruleCode)
                .ifPresentOrElse(
                        statistics -> statistics.increase(hitAt),
                        () -> riskRuleStatisticsRepository.save(new RiskRuleStatistics(ruleCode, hitAt))
                );
    }
}
