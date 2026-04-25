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
        name = "risk_case",
        indexes = {
                @Index(name = "idx_risk_case_status_time", columnList = "status, created_at"),
                @Index(name = "idx_risk_case_user", columnList = "user_id")
        }
)
public class RiskCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false, length = 50)
    private RiskCaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private RiskCaseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private RiskLevel riskLevel;

    @Column(name = "review_comment", length = 2000)
    private String reviewComment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RiskCase(
            Long evaluationId,
            Long userId,
            RiskCaseType caseType,
            RiskLevel riskLevel
    ) {
        this.evaluationId = evaluationId;
        this.userId = userId;
        this.caseType = caseType;
        this.riskLevel = riskLevel;
        this.status = RiskCaseStatus.REVIEW_REQUIRED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
