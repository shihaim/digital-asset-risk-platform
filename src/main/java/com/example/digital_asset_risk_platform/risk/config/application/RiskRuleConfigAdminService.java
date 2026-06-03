package com.example.digital_asset_risk_platform.risk.config.application;

import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfigHistory;
import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfigSnapshot;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigHistoryResponse;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigResponse;
import com.example.digital_asset_risk_platform.risk.config.dto.RiskRuleConfigUpdateRequest;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigHistoryRepository;
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
    private final RiskRuleConfigHistoryRepository riskRuleConfigHistoryRepository;

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

        RiskRuleConfigSnapshot before = RiskRuleConfigSnapshot.from(config);

        config.update(
                request.enabled(),
                request.score(),
                request.blocking(),
                request.thresholdValue(),
                request.description()
        );

        RiskRuleConfigSnapshot after = RiskRuleConfigSnapshot.from(config);

        RiskRuleConfigHistory history = RiskRuleConfigHistory.of(
                before,
                after,
                request.changedBy(),
                request.changeReason()
        );

        riskRuleConfigHistoryRepository.save(history);

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

    @Transactional(readOnly = true)
    public List<RiskRuleConfigHistoryResponse> getRuleConfigHistories(String ruleCode) {
        return riskRuleConfigHistoryRepository.findByRuleCodeOrderByChangedAtDesc(ruleCode)
                .stream()
                .map(RiskRuleConfigHistoryResponse::from)
                .toList();
    }
}
