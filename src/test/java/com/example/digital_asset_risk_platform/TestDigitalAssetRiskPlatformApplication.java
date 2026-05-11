package com.example.digital_asset_risk_platform;

import com.example.digital_asset_risk_platform.support.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestDigitalAssetRiskPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(DigitalAssetRiskPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
