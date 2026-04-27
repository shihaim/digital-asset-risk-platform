package com.example.digital_asset_risk_platform.risk.repository;

import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskCaseRepository extends JpaRepository<RiskCase, Long> {
    Optional<RiskCase> findByEvaluationId(Long evaluationId);
}
