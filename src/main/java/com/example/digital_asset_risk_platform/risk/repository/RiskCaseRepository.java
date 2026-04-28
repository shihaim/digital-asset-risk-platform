package com.example.digital_asset_risk_platform.risk.repository;

import com.example.digital_asset_risk_platform.risk.domain.RiskCase;
import com.example.digital_asset_risk_platform.risk.domain.RiskCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskCaseRepository extends JpaRepository<RiskCase, Long> {
    List<RiskCase> findByStatusOrderByCreatedAtDesc(RiskCaseStatus status);

    List<RiskCase> findAllByOrderByCreatedAtDesc();

    Optional<RiskCase> findByEvaluationId(Long evaluationId);

    long countByStatus(RiskCaseStatus status);
}
