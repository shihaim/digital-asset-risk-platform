package com.example.digital_asset_risk_platform.risk.repository;

import com.example.digital_asset_risk_platform.risk.domain.RiskRuleHit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskRuleHitRepository extends JpaRepository<RiskRuleHit, Long> {
}
