package com.example.digital_asset_risk_platform;

import org.springframework.boot.SpringApplication;

public class TestDigitalAssetRiskPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(DigitalAssetRiskPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
