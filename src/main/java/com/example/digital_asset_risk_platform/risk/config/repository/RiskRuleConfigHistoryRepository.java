package com.example.digital_asset_risk_platform.risk.config.repository;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfigHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRuleConfigHistoryRepository extends JpaRepository<RiskRuleConfigHistory, Long> {

    List<RiskRuleConfigHistory> findByRuleCodeOrderByChangedAtDesc(String ruleCode);
}
