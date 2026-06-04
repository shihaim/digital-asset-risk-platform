package com.example.digital_asset_risk_platform.risk.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "user_risk_profile",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_risk_profile_user_id",
                        columnNames = "user_id"
                )
        },
        indexes = {
                @Index(name = "idx_user_risk_profile_risk_level", columnList = "risk_level")
        }
)
public class UserRiskProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "current_risk_score", nullable = false)
    private int currentRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private UserRiskLevel riskLevel;

    @Column(name = "total_case_count", nullable = false)
    private int totalCaseCount;

    @Column(name = "total_blocked_withdrawal_count", nullable = false)
    private int totalBlockedWithdrawalCount;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserRiskProfile create(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        UserRiskProfile profile = new UserRiskProfile();
        profile.userId = userId;
        profile.currentRiskScore = 0;
        profile.riskLevel = UserRiskLevel.NORMAL;
        profile.totalCaseCount = 0;
        profile.totalBlockedWithdrawalCount = 0;
        profile.lastEvaluatedAt = null;
        profile.createdAt = now;
        profile.updatedAt = now;

        return profile;
    }

    public void applyEvaluationResult(int evaluationScore, boolean blocked) {
        int profileScore = UserRiskProfilePolicy.toProfileScore(evaluationScore);

        this.currentRiskScore += profileScore;

        if (blocked) {
            this.totalBlockedWithdrawalCount += 1;
        }

        this.riskLevel = UserRiskLevel.fromScore(this.currentRiskScore);
        this.lastEvaluatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseCaseCount() {
        this.totalCaseCount += 1;
        this.updatedAt = LocalDateTime.now();
    }
}
