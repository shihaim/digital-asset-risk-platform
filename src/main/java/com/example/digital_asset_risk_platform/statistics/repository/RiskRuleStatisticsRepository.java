package com.example.digital_asset_risk_platform.statistics.repository;

import com.example.digital_asset_risk_platform.statistics.domain.RiskRuleStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskRuleStatisticsRepository extends JpaRepository<RiskRuleStatistics, Long> {

    Optional<RiskRuleStatistics> findByRuleCode(String ruleCode);

    Page<RiskRuleStatistics> findAllByOrderByHitCountDescLastHitAtDesc(Pageable pageable);
}
