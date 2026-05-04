package com.example.digital_asset_risk_platform.statistics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "risk_rule_statistics",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rule_statistics_code", columnNames = "rule_code")
        }
)
public class RiskRuleStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "hit_count", nullable = false)
    private Long hitCount;

    @Column(name = "last_hit_at", nullable = false)
    private LocalDateTime lastHitAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RiskRuleStatistics(String ruleCode, LocalDateTime hitAt) {
        this.ruleCode = ruleCode;
        this.hitCount = 1L;
        this.lastHitAt = hitAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void increase(LocalDateTime hitAt) {
        this.hitCount++;
        this.lastHitAt = hitAt;
        this.updatedAt = LocalDateTime.now();
    }
}
