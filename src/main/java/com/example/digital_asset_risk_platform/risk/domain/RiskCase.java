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

    @Column(name = "assigned_to", length = 100)
    private String assignedTo;

    @Column(name = "review_result", length = 50)
    private String reviewResult;

    @Column(name = "review_comment", length = 2000)
    private String reviewComment;

    @Column(name = "closed_at", length = 2000)
    private LocalDateTime closedAt;

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


    ///========================================///
    public void startReview(String reviewer) {
        if (this.status != RiskCaseStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException("심사를 시작할 수 없는 Case 상태입니다: " + this.status);
        }

        this.status = RiskCaseStatus.IN_REVIEW;
        this.assignedTo = reviewer;
        this.updatedAt = LocalDateTime.now();
    }

    public void approve(String reviewer, String comment) {
        this.status = RiskCaseStatus.APPROVED;
        this.assignedTo = reviewer;
        this.reviewResult = "APPROVED";
        this.reviewComment = comment;
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reject(String reviewer, String comment) {
        this.status = RiskCaseStatus.REJECTED;
        this.assignedTo = reviewer;
        this.reviewResult = "REJECTED";
        this.reviewComment = comment;
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markFalsePositive(String reviewer, String comment) {
        this.status = RiskCaseStatus.FALSE_POSITIVE;
        this.assignedTo = reviewer;
        this.reviewResult = "FALSE_POSITIVE";
        this.reviewComment = comment;
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markTruePositive(String reviewer, String comment) {
        this.status = RiskCaseStatus.TRUE_POSITIVE;
        this.assignedTo = reviewer;
        this.reviewResult = "TRUE_POSITIVE";
        this.reviewComment = comment;
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
