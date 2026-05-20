package com.example.digital_asset_risk_platform.kyt.application;

import com.example.digital_asset_risk_platform.common.logging.LogMaskingUtils;
import com.example.digital_asset_risk_platform.kyt.domain.KytRiskCategory;
import com.example.digital_asset_risk_platform.kyt.dto.KytLookupRequest;
import com.example.digital_asset_risk_platform.kyt.dto.KytLookupResult;
import com.example.digital_asset_risk_platform.wallet.domain.WalletRiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockKytProvider implements KytProvider {

    private static final String PROVIDER_NAME = "MOCK_KYT";

    @Override
    public KytLookupResult lookup(KytLookupRequest request) {
        String address = request.address();

        log.info(
                "Mock KYT lookup requested. chainType={}, address={}",
                request.chainType(),
                LogMaskingUtils.maskAddress(address)
        );

        if (containsIgnoreCase(address, "SANCTION")) {
            return KytLookupResult.risky(
                    request.chainType(),
                    address,
                    WalletRiskLevel.CRITICAL,
                    100,
                    KytRiskCategory.SANCTIONED_ADDRESS,
                    PROVIDER_NAME
            );
        }

        if (containsIgnoreCase(address, "HACKED")) {
            return KytLookupResult.risky(
                    request.chainType(),
                    address,
                    WalletRiskLevel.HIGH,
                    100,
                    KytRiskCategory.HACKED_FUNDS,
                    PROVIDER_NAME
            );
        }

        if (containsIgnoreCase(address, "MIXER")) {
            return KytLookupResult.risky(
                    request.chainType(),
                    address,
                    WalletRiskLevel.HIGH,
                    90,
                    KytRiskCategory.MIXER,
                    PROVIDER_NAME
            );
        }

        if (containsIgnoreCase(address, "PHISH")) {
            return KytLookupResult.risky(
                    request.chainType(),
                    address,
                    WalletRiskLevel.HIGH,
                    85,
                    KytRiskCategory.PHISHING,
                    PROVIDER_NAME
            );
        }

        return KytLookupResult.normal(
                request.chainType(),
                address,
                PROVIDER_NAME
        );
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toUpperCase().contains(keyword);
    }
}
