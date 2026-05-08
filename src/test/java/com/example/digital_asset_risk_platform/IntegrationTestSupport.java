package com.example.digital_asset_risk_platform;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestSupport.TestcontainersConfig.class)
public abstract class IntegrationTestSupport {

    @TestConfiguration(proxyBeanMethods = false)
    static class TestcontainersConfig {

        @Bean
        @ServiceConnection
        MariaDBContainer<?> mariaDBContainer() {
            return new MariaDBContainer<>(DockerImageName.parse("mariadb:11"))
                    .withDatabaseName("risk_platform_test")
                    .withUsername("test")
                    .withPassword("test");
        }

        @Bean
        @ServiceConnection
        KafkaContainer kafkaContainer() {
            return new KafkaContainer(
                    DockerImageName.parse("apache/kafka-native:3.8.0")
            );
        }
    }
}
