package com.example.digital_asset_risk_platform.kyt.application;

import com.example.digital_asset_risk_platform.common.logging.LogMaskingUtils;
import com.example.digital_asset_risk_platform.kyt.dto.KytLookupRequest;
import com.example.digital_asset_risk_platform.kyt.dto.KytLookupResult;
import com.example.digital_asset_risk_platform.wallet.domain.WalletAddressRisk;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCacheResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KytLookupService {

    private final KytProvider kytProvider;
    private final WalletAddressRiskRepository walletAddressRiskRepository;

    public WalletRiskCacheResponse lookupAndSync(String chainType, String address) {
        KytLookupResult result = kytProvider.lookup(new KytLookupRequest(chainType, address));

        if (!result.risky()) {
            log.info(
                    "KYT lookup returned normal wallet. chainType={}, address={}, provider={}",
                    chainType,
                    LogMaskingUtils.maskAddress(address),
                    result.provider()
            );

            return new WalletRiskCacheResponse(
                    result.chainType(),
                    result.address(),
                    result.riskLevel(),
                    result.riskScore(),
                    result.riskCategory(),
                    result.provider()
            );
        }

        WalletAddressRisk walletRisk = walletAddressRiskRepository.findByChainTypeAndAddress(chainType, address)
                .orElseGet(() -> walletAddressRiskRepository.save(new WalletAddressRisk(
                        result.chainType(),
                        result.address(),
                        result.riskLevel(),
                        result.riskScore(),
                        result.riskCategory(),
                        result.provider()
                )));

        log.info(
                "KYT risky wallet synced. chainType={}, address={}, riskLevel={}, riskScore={}, riskCategory={}, provider={}",
                result.chainType(),
                LogMaskingUtils.maskAddress(result.address()),
                result.riskLevel(),
                result.riskScore(),
                result.riskCategory(),
                result.provider()
        );

        return WalletRiskCacheResponse.from(walletRisk);
    }
}
