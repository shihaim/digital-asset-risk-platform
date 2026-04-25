package com.example.digital_asset_risk_platform.wallet.repository;

import com.example.digital_asset_risk_platform.wallet.domain.WalletAddressRisk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletAddressRiskRepository extends JpaRepository<WalletAddressRisk, Long> {
    Optional<WalletAddressRisk> findByChainTypeAndAddress(String chainType, String address);
}
