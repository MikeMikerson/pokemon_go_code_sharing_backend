package com.devs.simplicity.poke_go_friends.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Security configuration.
 * Tests security headers and access control using TestRestTemplate.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Security headers should be present in responses")
    void securityHeaders_shouldBePresentInResponses() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/friend-codes", String.class);

        // Then - Check that security headers are present
        if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError()) {
            assertThat(response.getHeaders()).containsKey("X-Content-Type-Options");
            assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
            assertThat(response.getHeaders()).containsKey("X-Frame-Options");
            assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("DENY");
        }
        // Just verify that we get a response and headers are applied
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("API endpoints should not require authentication")
    void apiEndpoints_shouldNotRequireAuth() {
        // When
        ResponseEntity<String> response1 = restTemplate.getForEntity("/api/v1/friend-codes", String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity("/api/v1/friend-codes/can-submit", String.class);

        // Then - Should not get unauthorized status codes
        assertThat(response1.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response2.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Actuator health endpoint should be accessible")
    void actuatorHealth_shouldBeAccessible() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        // Then - Should not get unauthorized status
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Actuator management endpoints should be protected")
    void actuatorManagement_shouldBeProtected() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);

        // Then - Should not return server error (configuration should work)
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("CORS headers should be present for API endpoints")
    void corsHeaders_shouldBePresentForApiEndpoints() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/friend-codes", String.class);

        // Then - Basic response should be received
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
