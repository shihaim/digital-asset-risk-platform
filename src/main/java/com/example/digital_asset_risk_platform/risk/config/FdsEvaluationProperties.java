package com.example.digital_asset_risk_platform.risk.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "fds.evaluation")
public class FdsEvaluationProperties {

    private FdsEvaluationMode mode = FdsEvaluationMode.SYNC;

    public boolean isSyncMode() {
        return mode == FdsEvaluationMode.SYNC;
    }

    public boolean isAsyncMode() {
        return mode == FdsEvaluationMode.ASYNC;
    }
}
