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
        name = "risk_rule_hit",
        indexes = {
                @Index(name = "idx_rule_hit_eval", columnList = "evaluation_id"),
                @Index(name = "idx_rule_hit_code", columnList = "rule_code")
        }
)
public class RiskRuleHit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "blocking_yn", nullable = false, length = 1)
    private String blockingYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RiskRuleHit(
            Long evaluationId,
            String ruleCode,
            String ruleName,
            int score,
            String reason,
            boolean blocking
    ) {
        this.evaluationId = evaluationId;
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.score = score;
        this.reason = reason;
        this.blockingYn = blocking ? "Y" : "N";
        this.createdAt = LocalDateTime.now();
    }
}
