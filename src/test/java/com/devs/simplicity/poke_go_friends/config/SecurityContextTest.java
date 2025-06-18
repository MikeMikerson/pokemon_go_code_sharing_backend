package com.devs.simplicity.poke_go_friends.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simple test to verify the application context loads correctly with security configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Security Context Loading Tests")
class SecurityContextTest {

    @Test
    @DisplayName("Application context should load successfully with security configuration")
    void applicationContext_shouldLoadSuccessfully() {
        // This test passes if the Spring context loads without errors
        // If there are configuration issues, this test will fail
    }
}
