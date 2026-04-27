package com.example.digital_asset_risk_platform.risk.repository;

import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskRuleHitRepository extends JpaRepository<RiskRuleHit, Long> {
    List<RiskRuleHit> findByEvaluationId(Long evaluationId);
}
