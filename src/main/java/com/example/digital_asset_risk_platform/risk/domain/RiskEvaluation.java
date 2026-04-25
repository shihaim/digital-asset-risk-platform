package com.example.digital_asset_risk_platform.risk.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "risk_evaluation",
        indexes = {
                @Index(name = "idx_risk_eval_ref", columnList = "ref_type, ref_id"),
                @Index(name = "idx_risk_eval_user_time", columnList = "user_id, evaluated_at")
        }
)
public class RiskEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_type", nullable = false, length = 50)
    private String refType;

    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 50)
    private RiskDecisionType decision;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RiskEvaluation(
            String refType,
            Long refId,
            Long userId,
            int totalScore,
            RiskLevel riskLevel,
            RiskDecisionType decision
    ) {
        this.refType = refType;
        this.refId = refId;
        this.userId = userId;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.evaluatedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
