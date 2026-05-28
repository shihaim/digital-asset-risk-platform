package com.example.digital_asset_risk_platform.risk.config.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "risk_rule_config_history",
        indexes = {
                @Index(
                        name = "idx_rule_config_history_rule_code_changed_at",
                        columnList = "rule_code, changed_at"
                )
        }
)
public class RiskRuleConfigHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    @Column(name = "before_enabled_yn", nullable = false, length = 1)
    private String beforeEnabledYn;

    @Column(name = "after_enabled_yn", nullable = false, length = 1)
    private String afterEnabledYn;

    @Column(name = "before_score", nullable = false)
    private int beforeScore;

    @Column(name = "after_score", nullable = false)
    private int afterScore;

    @Column(name = "before_blocking_yn", nullable = false, length = 1)
    private String beforeBlockingYn;

    @Column(name = "after_blocking_yn", nullable = false, length = 1)
    private String afterBlockingYn;

    @Column(name = "before_threshold_value", length = 100)
    private String beforeThresholdValue;

    @Column(name = "after_threshold_value", length = 100)
    private String afterThresholdValue;

    @Column(name = "before_description", length = 1000)
    private String beforeDescription;

    @Column(name = "after_description", length = 1000)
    private String afterDescription;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "change_reason", nullable = false, length = 500)
    private String changeReason;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public static RiskRuleConfigHistory of(
            RiskRuleConfigSnapshot before,
            RiskRuleConfigSnapshot after,
            String changedBy,
            String changeReason
    ) {
        RiskRuleConfigHistory history = new RiskRuleConfigHistory();

        history.ruleCode = before.ruleCode();
        history.ruleName = before.ruleName();

        history.beforeEnabledYn = before.enabledYn();
        history.afterEnabledYn = after.enabledYn();

        history.beforeScore = before.score();
        history.afterScore = after.score();

        history.beforeBlockingYn = before.blockingYn();
        history.afterBlockingYn = after.blockingYn();

        history.beforeThresholdValue = before.thresholdValue();
        history.afterThresholdValue = after.thresholdValue();

        history.beforeDescription = before.description();
        history.afterDescription = after.description();

        history.changedBy = changedBy;
        history.changeReason = changeReason;
        history.changedAt = LocalDateTime.now();

        return history;
    }

    public boolean isBeforeEnabled() {
        return "Y".equals(beforeEnabledYn);
    }

    public boolean isAfterEnabled() {
        return "Y".equals(afterEnabledYn);
    }

    public boolean isBeforeBlocking() {
        return "Y".equals(beforeBlockingYn);
    }

    public boolean isAfterBlocking() {
        return "Y".equals(afterBlockingYn);
    }
}
