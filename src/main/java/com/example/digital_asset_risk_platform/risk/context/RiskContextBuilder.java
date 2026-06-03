package com.example.digital_asset_risk_platform.risk.context;

import com.example.digital_asset_risk_platform.account.domain.AccountLoginEvent;
import com.example.digital_asset_risk_platform.account.domain.AccountSecurityEvent;
import com.example.digital_asset_risk_platform.account.domain.SecurityEventType;
import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.risk.application.WalletRiskSnapshot;
import com.example.digital_asset_risk_platform.wallet.application.WalletRiskService;
import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RiskContextBuilder {

    private final AccountLoginEventRepository loginEventRepository;
    private final AccountSecurityEventRepository securityEventRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final WalletRiskService walletRiskService;

    public RiskContext build(WithdrawalRequest withdrawal) {
        Long userId = withdrawal.getUserId();
        LocalDateTime now = withdrawal.getRequestedAt();

        LocalDateTime twentyFourHoursAgo = now.minusHours(24);

        LocalDateTime latestNewDeviceLoginAt = loginEventRepository
                .findTopByUserIdAndNewDeviceYnOrderByLoginAtDesc(userId, "Y")
                .map(AccountLoginEvent::getLoginAt)
                .orElse(null);

        LocalDateTime latestPasswordChangedAt = securityEventRepository
                .findTopByUserIdAndEventTypeOrderByEventAtDesc(userId, SecurityEventType.PASSWORD_CHANGED)
                .map(AccountSecurityEvent::getEventAt)
                .orElse(null);

        LocalDateTime latestOtpResetAt = securityEventRepository
                .findTopByUserIdAndEventTypeOrderByEventAtDesc(userId, SecurityEventType.OTP_RESET)
                .map(AccountSecurityEvent::getEventAt)
                .orElse(null);

        boolean usedAddressBefore = isUsedAddressBefore(withdrawal);

        BigDecimal averageAmount = withdrawalRequestRepository.averageAmountByUserId(userId);

        long withdrawalCountLast24h = withdrawalRequestRepository.countByUserIdAndRequestedAt(userId, twentyFourHoursAgo);

        WalletRiskSnapshot walletRisk = WalletRiskSnapshot.from(walletRiskService.getWalletRisk(withdrawal.getChainType(), withdrawal.getToAddress()));

        AccountRiskSnapshot accountRisk = new AccountRiskSnapshot(latestNewDeviceLoginAt, latestPasswordChangedAt, latestOtpResetAt);

        return new RiskContext(
                withdrawal,
                accountRisk,
                walletRisk,
                !usedAddressBefore,
                averageAmount,
                withdrawalCountLast24h
        );
    }

    private boolean isUsedAddressBefore(WithdrawalRequest withdrawal) {
        if (withdrawal.getId() == null) {
            return withdrawalRequestRepository.existsByUserIdAndChainTypeAndToAddress(
                    withdrawal.getUserId(),
                    withdrawal.getChainType(),
                    withdrawal.getToAddress()
            );
        }

        return withdrawalRequestRepository.existsByUserIdAndChainTypeAndToAddressAndIdNot(
                withdrawal.getUserId(),
                withdrawal.getChainType(),
                withdrawal.getToAddress(),
                withdrawal.getId()
        );
    }
}
