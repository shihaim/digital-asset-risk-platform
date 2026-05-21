package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskRuleConfigService {

    private final RiskRuleConfigRepository riskRuleConfigRepository;

    @Transactional(readOnly = true)
    public RiskRuleConfig getConfig(String ruleCode) {
        return riskRuleConfigRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RISK_RULE_CONFIG_NOT_FOUND));
    }
}
