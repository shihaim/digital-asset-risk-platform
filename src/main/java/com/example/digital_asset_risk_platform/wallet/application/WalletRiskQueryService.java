package com.example.digital_asset_risk_platform.wallet.application;

import com.example.digital_asset_risk_platform.common.cache.CacheNames;
import com.example.digital_asset_risk_platform.common.logging.LogMaskingUtils;
import com.example.digital_asset_risk_platform.wallet.dto.WalletRiskCacheResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WalletAddressRiskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WalletRiskQueryService {

    private final WalletAddressRiskRepository walletAddressRiskRepository;

    @Cacheable(
            cacheNames = CacheNames.WALLET_RISK,
            key = "#chainType + ':' + #address",
            unless = "#result == null"
    )
    public WalletRiskCacheResponse getWalletRisk(String chainType, String address) {
        log.debug(
                "Wallet risk cache miss. chainType={}, address={}",
                chainType,
                LogMaskingUtils.maskAddress(address)
        );

        return walletAddressRiskRepository.findByChainTypeAndAddress(chainType, address)
                .map(WalletRiskCacheResponse::from)
                .orElse(null);
    }
}
