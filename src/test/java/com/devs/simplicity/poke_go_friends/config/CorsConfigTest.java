package com.devs.simplicity.poke_go_friends.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CORS configuration.
 * Tests proper CORS setup in different environments.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CORS Configuration Tests")
class CorsConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("CORS configuration should be properly initialized")
    void corsConfiguration_shouldBeProperlyInitialized() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // When
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).isNotEmpty();
        assertThat(config.getAllowedMethods()).isNotEmpty();
        assertThat(config.getAllowedHeaders()).isNotEmpty();
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Test environment should not have wildcards in origins")
    void testEnvironment_shouldNotHaveWildcardOrigins() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // When
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertThat(config.getAllowedOrigins())
                .noneMatch(origin -> origin.contains("*"));
    }

    @Test
    @DisplayName("CORS configuration should include common HTTP methods")
    void corsConfiguration_shouldIncludeCommonMethods() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // When
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertThat(config.getAllowedMethods())
                .contains("GET", "POST", "OPTIONS");
    }

    @Test
    @DisplayName("CORS configuration should include required headers")
    void corsConfiguration_shouldIncludeRequiredHeaders() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");

        // When
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);

        // Then
        assertThat(config.getAllowedHeaders())
                .contains("Content-Type");
    }
}
