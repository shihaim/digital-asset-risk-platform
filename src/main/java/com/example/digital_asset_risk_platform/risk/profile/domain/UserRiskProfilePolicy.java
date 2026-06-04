package com.example.digital_asset_risk_platform.risk.profile.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRiskProfilePolicy {

    public static int toProfileScore(int evaluationScore) {
        if (evaluationScore >= 100) {
            return 50;
        }

        if (evaluationScore >= 70) {
            return 30;
        }

        if (evaluationScore >= 30) {
            return 10;
        }

        return 0;
    }
}
