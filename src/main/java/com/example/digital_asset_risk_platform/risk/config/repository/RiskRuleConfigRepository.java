package com.example.digital_asset_risk_platform.risk.config.repository;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskRuleConfigRepository extends JpaRepository<RiskRuleConfig, Long> {

    Optional<RiskRuleConfig> findByRuleCode(String ruleCode);

    boolean existsByRuleCode(String ruleCode);
}
