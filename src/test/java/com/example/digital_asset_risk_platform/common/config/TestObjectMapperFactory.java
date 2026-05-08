package com.example.digital_asset_risk_platform.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public final class TestObjectMapperFactory {
    private TestObjectMapperFactory() {
    }

    public static ObjectMapper create() {
        return Jackson2ObjectMapperBuilder.json()
                .modulesToInstall(JavaTimeModule.class)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
