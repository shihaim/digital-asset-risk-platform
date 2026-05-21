package com.example.digital_asset_risk_platform.risk.config.domain;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "risk_rule_config",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_risk_rule_config_code", columnNames = "rule_code")
        },
        indexes = {
                @Index(name = "idx_risk_rule_config_enabled", columnList = "enabled_yn")
        }
)
public class RiskRuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "enabled_yn", nullable = false, length = 1)
    private String enabledYn;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "blocking_yn", nullable = false, length = 1)
    private String blockingYn;

    @Column(name = "threshold_value", length = 100)
    private String thresholdValue;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RiskRuleConfig(
            String ruleCode,
            String ruleName,
            boolean enabled,
            int score,
            boolean blocking,
            String thresholdValue,
            String description
    ) {
        validateScore(score);

        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.enabledYn = enabled ? "Y" : "N";
        this.score = score;
        this.blockingYn = blocking ? "Y" : "N";
        this.thresholdValue = thresholdValue;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void update(Boolean enabled, Integer score, Boolean blocking, String thresholdValue, String description) {
        if (enabled != null) {
            this.enabledYn = enabled ? "Y" : "N";
        }

        if (score != null) {
            validateScore(score);
            this.score = score;
        }

        if (blocking != null) {
            this.blockingYn = blocking ? "Y" : "N";
        }

        if (thresholdValue != null) {
            this.thresholdValue = thresholdValue;
        }

        if (description != null) {
            this.description = description;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public boolean isEnabled() {
        return "Y".equals(enabledYn);
    }

    public boolean isBlocking() {
        return "Y".equals(this.blockingYn);
    }

    public int getThresholdAsInt(int defaultValue) {
        if (thresholdValue == null || thresholdValue.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(thresholdValue.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public BigDecimal getThresholdAsBigDecimal(BigDecimal defaultValue) {
        if (thresholdValue == null || thresholdValue.isBlank()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(thresholdValue.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Duration getThresholdAsDuration(Duration defaultValue) {
        if (thresholdValue == null || thresholdValue.isBlank()) {
            return defaultValue;
        }

        String value = thresholdValue.trim().toLowerCase();
        try {
            if (value.endsWith("m")) {
                return Duration.ofMinutes(Long.parseLong(value.substring(0, value.length() - 1)));
            }

            if (value.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(value.substring(0, value.length() - 1)));
            }

            return Duration.ofHours(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public BigDecimal getMultiplierThreshold(BigDecimal defaultValue) {
        if (thresholdValue == null || thresholdValue.isBlank()) {
            return defaultValue;
        }

        String value = thresholdValue.trim().toLowerCase();
        if (!value.endsWith("x")) {
            return defaultValue;
        }

        try {
            return new BigDecimal(value.substring(0, value.length() - 1));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void validateScore(int score) {
        if (score < 0) {
            throw new BusinessException(ErrorCode.INVALID_RISK_RULE_CONFIG);
        }
    }
}
