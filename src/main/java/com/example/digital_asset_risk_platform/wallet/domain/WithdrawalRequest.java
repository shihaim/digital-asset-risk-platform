package com.example.digital_asset_risk_platform.wallet.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "withdrawal_request",
        indexes = {
                @Index(name = "idx_withdrawal_user_time", columnList = "user_id, requested_at"),
                @Index(name = "idx_withdrawal_status_time", columnList = "status, requested_at")
        }
)
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "asset_symbol", nullable = false, length = 30)
    private String assetSymbol;

    @Column(name = "chain_type", nullable = false, length = 30)
    private String chainType;

    @Column(name = "to_address", nullable = false, length = 200)
    private String toAddress;

    @Column(name = "amount", nullable = false, precision = 36, scale = 18)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WithdrawalStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WithdrawalRequest(
            Long userId,
            String assetSymbol,
            String chainType,
            String toAddress,
            BigDecimal amount
    ) {
        this.userId = userId;
        this.assetSymbol = assetSymbol;
        this.chainType = chainType;
        this.toAddress = toAddress;
        this.amount = amount;
        this.status = WithdrawalStatus.REQUESTED;
        this.requestedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


    ///========================================///
    public void approve() {
        this.status = WithdrawalStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void hold() {
        this.status = WithdrawalStatus.HELD;
        this.updatedAt = LocalDateTime.now();
    }

    public void block() {
        this.status = WithdrawalStatus.BLOCKED;
        this.rejectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
