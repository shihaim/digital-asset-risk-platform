package com.example.digital_asset_risk_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DigitalAssetRiskPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalAssetRiskPlatformApplication.class, args);
	}

}
