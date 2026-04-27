package com.example.digital_asset_risk_platform.risk.repository;

import com.example.digital_asset_risk_platform.risk.domain.RiskEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskEvaluationRepository extends JpaRepository<RiskEvaluation, Long> {
    Optional<RiskEvaluation> findByRefTypeAndRefId(String refType, Long refId);
}
