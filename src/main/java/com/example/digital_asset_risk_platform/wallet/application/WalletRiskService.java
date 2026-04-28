package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.risk.application.WalletRiskSnapshot;
import com.example.digital_asset_risk_platform.wallet.domain.WalletAddressRisk;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCreateRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletRiskService {

    private final WalletAddressRiskRepository walletAddressRiskRepository;

    public Long createWalletRisk(WalletRiskCreateRequest request) {
        WalletAddressRisk risk = new WalletAddressRisk(
                request.chainType(),
                request.address(),
                request.riskLevel(),
                request.riskScore(),
                request.riskCategory(),
                request.provider()
        );

        return walletAddressRiskRepository.save(risk).getId();
    }

    @Transactional(readOnly = true)
    public WalletRiskSnapshot findRisk(String chainType, String address) {
        Optional<WalletAddressRisk> risk = walletAddressRiskRepository.findByChainTypeAndAddress(chainType, address);

        return risk
                .map(value -> new WalletRiskSnapshot(
                        value.getRiskLevel(),
                        value.getRiskScore(),
                        value.getRiskCategory()
                ))
                .orElse(new WalletRiskSnapshot(
                        WalletRiskLevel.LOW,
                        0,
                        "UNKNOWN_ADDRESS"
                ));
    }
}
