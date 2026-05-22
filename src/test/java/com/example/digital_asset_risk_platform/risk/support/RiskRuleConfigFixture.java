package com.example.digital_asset_risk_platform.risk.support;

import com.example.digital_asset_risk_platform.risk.config.domain.RiskRuleConfig;
import com.example.digital_asset_risk_platform.risk.config.repository.RiskRuleConfigRepository;
import com.example.digital_asset_risk_platform.risk.rule.RiskRuleCodes;

import java.util.List;

public class RiskRuleConfigFixture {

    private RiskRuleConfigFixture() {
    }

    public static void ensureDefaultConfigs(RiskRuleConfigRepository repository) {
        for (RiskRuleConfig config : defaultConfigs()) {
            if (!repository.existsByRuleCode(config.getRuleCode())) {
                repository.save(config);
            }
        }
        repository.flush();
    }

    public static void resetDefaultConfigs(RiskRuleConfigRepository repository) {
        repository.deleteAll();
        repository.saveAll(defaultConfigs());
        repository.flush();
    }

    public static List<RiskRuleConfig> defaultConfigs() {
        return List.of(
                new RiskRuleConfig(
                        RiskRuleCodes.NEW_DEVICE_WITHDRAWAL,
                        "신규 기기 로그인 후 출금",
                        true,
                        30,
                        false,
                        "60m",
                        "최근 60분 이내 신규 기기 로그인 후 출금하는 경우"
                ),
                new RiskRuleConfig(
                        RiskRuleCodes.OTP_RESET_WITHDRAWAL,
                        "OTP 재설정 후 출금",
                        true,
                        40,
                        false,
                        "24h",
                        "최근 24시간 이내 OTP 재설정 후 출금하는 경우"
                ),
                new RiskRuleConfig(
                        RiskRuleCodes.PASSWORD_CHANGED_WITHDRAWAL,
                        "비밀번호 변경 후 출금",
                        true,
                        30,
                        false,
                        "24h",
                        "최근 24시간 이내 비밀번호 변경 후 출금하는 경우"
                ),
                new RiskRuleConfig(
                        RiskRuleCodes.NEW_WALLET_ADDRESS,
                        "신규 지갑 주소 출금",
                        true,
                        20,
                        false,
                        null,
                        "과거 출금 이력이 없는 신규 지갑 주소로 출금하는 경우"
                ),
                new RiskRuleConfig(
                        RiskRuleCodes.HIGH_AMOUNT_WITHDRAWAL,
                        "평균 대비 고액 출금",
                        true,
                        40,
                        false,
                        "10x",
                        "이번 출금액이 사용자 평균 출금액의 10배 이상인 경우"
                ),
                new RiskRuleConfig(
                        RiskRuleCodes.FREQUENT_WITHDRAWAL_24H,
                        "24시간 내 반복 출금",
                        true,
                        30,
                        false,
                        "5",
                        "최근 24시간 내 출금 요청 횟수가 기준 이상인 경우"
                ),
                new RiskRuleConfig(
                        RiskRuleCodes.HIGH_RISK_WALLET,
                        "고위험 지갑 주소 출금",
                        true,
                        100,
                        true,
                        null,
                        "KYT 또는 내부 위험 주소 DB에서 고위험으로 분류된 지갑으로 출금하는 경우"
                )
        );
    }
}
