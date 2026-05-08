package com.example.digital_asset_risk_platform;

import com.example.digital_asset_risk_platform.risk.config.FdsEvaluationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(FdsEvaluationProperties.class)
public class DigitalAssetRiskPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalAssetRiskPlatformApplication.class, args);
	}

}
