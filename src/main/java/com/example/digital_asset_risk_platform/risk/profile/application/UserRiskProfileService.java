package com.example.digital_asset_risk_platform.risk.profile.application;

import com.example.digital_asset_risk_platform.risk.profile.domain.UserRiskProfile;
import com.example.digital_asset_risk_platform.risk.profile.dto.UserRiskProfileResponse;
import com.example.digital_asset_risk_platform.risk.profile.repository.UserRiskProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRiskProfileService {

    private final UserRiskProfileRepository userRiskProfileRepository;

    @Transactional
    public void updateFromEvaluate(Long userId, int evaluationScore, boolean blocked) {
        UserRiskProfile profile = userRiskProfileRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> UserRiskProfile.create(userId));

        profile.applyEvaluationResult(evaluationScore, blocked);

        userRiskProfileRepository.save(profile);

        log.info(
                "User risk profile updated from evaluation. userId={}, evaluationScore={}, blocked={}, currentRiskScore={}, riskLevel={}",
                userId,
                evaluationScore,
                blocked,
                profile.getCurrentRiskScore(),
                profile.getRiskLevel()
        );
    }

    @Transactional
    public void increaseCaseCount(Long userId) {
        UserRiskProfile profile = userRiskProfileRepository.findByUserIdForUpdate(userId)
                .orElseGet(() -> UserRiskProfile.create(userId));

        profile.increaseCaseCount();

        userRiskProfileRepository.save(profile);

        log.info(
                "User risk profile case count increased. userId={}, totalCaseCount={}",
                userId,
                profile.getTotalCaseCount()
        );
    }

    @Transactional(readOnly = true)
    public UserRiskProfileResponse getProfile(Long userId) {
        UserRiskProfile profile = userRiskProfileRepository.findByUserId(userId)
                .orElseGet(() -> UserRiskProfile.create(userId));

        return UserRiskProfileResponse.from(profile);
    }
}
