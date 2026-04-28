package com.example.digital_asset_risk_platform.admin.application;

import com.example.digital_asset_risk_platform.account.repository.AccountLoginEventRepository;
import com.example.digital_asset_risk_platform.account.repository.AccountSecurityEventRepository;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineEventResponse;
import com.example.digital_asset_risk_platform.admin.dto.RiskTimelineResponse;
import com.example.digital_asset_risk_platform.wallet.repository.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RiskTimelineService {

    private final AccountLoginEventRepository loginEventRepository;
    private final AccountSecurityEventRepository securityEventRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;

    @Transactional(readOnly = true)
    public RiskTimelineResponse getUserTimeline(Long userId) {
        LocalDateTime from = LocalDateTime.now().minusDays(7);

        List<RiskTimelineEventResponse> loginEvents = loginEventRepository.findByUserIdAndLoginAtAfterOrderByLoginAtDesc(userId, from).stream()
                .map(RiskTimelineEventResponse::toLoginTimeline)
                .toList();

        List<RiskTimelineEventResponse> securityEvents = securityEventRepository.findByUserIdAndEventAtAfterOrderByEventAtDesc(userId, from).stream()
                .map(RiskTimelineEventResponse::toSecurityTimeline)
                .toList();

        List<RiskTimelineEventResponse> withdrawalEvents = withdrawalRequestRepository.findByUserIdAndRequestedAtAfterOrderByRequestedAtDesc(userId, from).stream()
                .map(RiskTimelineEventResponse::toWithdrawalTimeline)
                .toList();

        List<RiskTimelineEventResponse> events = Stream.of(loginEvents.stream(), securityEvents.stream(), withdrawalEvents.stream())
                .flatMap(stream -> stream)
                .sorted(Comparator.comparing(RiskTimelineEventResponse::eventAt))
                .toList();

        return new RiskTimelineResponse(userId, events);
    }
}
