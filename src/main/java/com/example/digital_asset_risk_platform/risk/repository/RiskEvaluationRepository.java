package com.example.digital_asset_risk_platform.risk.repository;

import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskEvaluationRepository extends JpaRepository<RiskEvaluation, Long> {
}
