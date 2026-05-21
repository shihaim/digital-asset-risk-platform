package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigResponse;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigUpdateRequest;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskRuleConfigAdminService {

    private final RiskRuleConfigRepository riskRuleConfigRepository;

    @Transactional(readOnly = true)
    public List<RiskRuleConfigResponse> getRuleConfigs() {
        return riskRuleConfigRepository.findAll()
                .stream()
                .map(RiskRuleConfigResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RiskRuleConfigResponse getRuleConfig(String ruleCode) {
        RiskRuleConfig config = riskRuleConfigRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_RULE_CONFIG_NOT_FOUND));

        return RiskRuleConfigResponse.from(config);
    }

    @Transactional
    public RiskRuleConfigResponse updateRuleConfig(String ruleCode, RiskRuleConfigUpdateRequest request) {
        RiskRuleConfig config = riskRuleConfigRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_RULE_CONFIG_NOT_FOUND));

        config.update(
                request.enabled(),
                request.score(),
                request.blocking(),
                request.thresholdValue(),
                request.description()
        );

        log.info(
                "Risk rule config updated. ruleCode={}, enabled={}, score={}, blocking={}, thresholdValue={}",
                config.getRuleCode(),
                config.isEnabled(),
                config.getScore(),
                config.isBlocking(),
                config.getThresholdValue()
        );

        return RiskRuleConfigResponse.from(config);
    }
}
