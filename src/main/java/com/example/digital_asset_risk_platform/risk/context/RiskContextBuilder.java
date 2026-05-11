package com.example.digital_asset_risk_platform.risk.context;

import com.example.digital_asset_risk_platform.account.domain.AccountLoginEvent;
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
import java.util.List;

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

        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);

        List<AccountLoginEvent> recentLogins = loginEventRepository.findByUserIdAndLoginAtAfter(userId, oneHourAgo);

        boolean newDeviceLoginWithin1h = recentLogins.stream().anyMatch(AccountLoginEvent::isNewDevice);

        boolean passwordChangedWithin24h = securityEventRepository.existsByUserIdAndEventTypeAndEventAtAfter(userId, SecurityEventType.PHONE_CHANGED, twentyFourHoursAgo);
        boolean otpResetWithin24h = securityEventRepository.existsByUserIdAndEventTypeAndEventAtAfter(userId, SecurityEventType.OTP_RESET, twentyFourHoursAgo);

        boolean usedAddressBefore = withdrawalRequestRepository.existsByUserIdAndChainTypeAndToAddressAndIdNot(userId, withdrawal.getChainType(), withdrawal.getToAddress(), withdrawal.getId());

        BigDecimal averageAmount = withdrawalRequestRepository.averageAmountByUserId(userId);

        long withdrawalCountLast24h = withdrawalRequestRepository.countByUserIdAndRequestedAt(userId, twentyFourHoursAgo);

        WalletRiskSnapshot walletRisk = walletRiskService.findRisk(withdrawal.getChainType(), withdrawal.getToAddress());

        AccountRiskSnapshot accountRisk = new AccountRiskSnapshot(newDeviceLoginWithin1h, passwordChangedWithin24h, otpResetWithin24h);

        return new RiskContext(
                withdrawal,
                accountRisk,
                walletRisk,
                !usedAddressBefore,
                averageAmount,
                withdrawalCountLast24h
        );
    }
}
