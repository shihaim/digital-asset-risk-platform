package com.example.digital_asset_risk_platform;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public abstract class IntegrationTestSupport {

    @Container
    @ServiceConnection
    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:11")
            .withDatabaseName("risk_platform_test")
            .withUsername("test")
            .withPassword("test");
}
