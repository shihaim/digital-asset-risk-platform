package com.example.digital_asset_risk_platform.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestPropertySource(properties = {
        "spring.kafka.listener.auto-startup=true",
        "fds.evaluation.mode=async",
        "app.kafka.consumer.group.fds-withdrawal=fds-withdrawal-consumer-${random.uuid}",
        "app.kafka.consumer.group.audit-log=audit-log-consumer-${random.uuid}",
        "app.kafka.consumer.group.admin-notification=admin-notification-consumer-${random.uuid}",
        "app.kafka.consumer.group.rule-statistics=rule-statistics-consumer-${random.uuid}"})
@Testcontainers
public abstract class KafkaIntegrationTestSupport extends IntegrationTestSupport {

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    static {
        kafka.start();
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}
