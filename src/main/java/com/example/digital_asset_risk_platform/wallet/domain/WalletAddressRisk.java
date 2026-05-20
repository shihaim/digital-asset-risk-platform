package com.example.digital_asset_risk_platform.wallet.domain;

import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "wallet_address_risk",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wallet_address",
                        columnNames = {"chain_type", "address"}
                )
        }
)
public class WalletAddressRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chain_type", nullable = false, length = 30)
    private String chainType;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 30)
    private WalletRiskLevel riskLevel;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_category", length = 100)
    private KytRiskCategory riskCategory;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "last_checked_at", nullable = false)
    private LocalDateTime lastCheckedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public WalletAddressRisk(
            String chainType,
            String address,
            WalletRiskLevel riskLevel,
            int riskScore,
            KytRiskCategory riskCategory,
            String provider
    ) {
        this.chainType = chainType;
        this.address = address;
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.riskCategory = riskCategory;
        this.provider = provider;
        this.lastCheckedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public WalletAddressRisk(
            String chainType,
            String address,
            WalletRiskLevel riskLevel,
            int riskScore,
            String riskCategory,
            String provider
    ) {
        this(
                chainType,
                address,
                riskLevel,
                riskScore,
                KytRiskCategory.valueOf(riskCategory),
                provider
        );
    }
}
