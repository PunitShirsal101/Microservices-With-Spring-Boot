package com.enterprise.ecommerce.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Test class for Config Server Application
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "encrypt.key-store.location=",
    "spring.cloud.config.server.encrypt.enabled=false",
    "spring.cloud.vault.enabled=false",
    "spring.cloud.config.server.git.uri=file:///tmp/config-repo",
    "spring.cloud.config.server.git.skip-ssl-validation=true"
})
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}