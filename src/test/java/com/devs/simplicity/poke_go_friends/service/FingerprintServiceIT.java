package com.devs.simplicity.poke_go_friends.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("FingerprintService Integration Test")
class FingerprintServiceIT {

    @Autowired
    private FingerprintService fingerprintService;

    @Test
    @DisplayName("should be injectable as Spring bean")
    void fingerprintService_springContext_shouldBeInjectable() {
        // Assert
        assertThat(fingerprintService).isNotNull();
    }

    @Test
    @DisplayName("should generate consistent fingerprints in Spring context")
    void generateFingerprint_springContext_shouldBeConsistent() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.195");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        String fingerprint2 = fingerprintService.generateFingerprint(request);

        // Assert
        assertThat(fingerprint1).isNotNull();
        assertThat(fingerprint1).isNotEmpty();
        assertThat(fingerprint1).isEqualTo(fingerprint2);
        assertThat(fingerprint1).hasSize(64); // SHA-256 hex string
        assertThat(fingerprint1).matches("^[a-f0-9]{64}$");
    }

    @Test
    @DisplayName("should handle real HTTP request characteristics")
    void generateFingerprint_realHttpRequest_shouldHandleCorrectly() {
        // Arrange - Simulate a real browser request
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.195");
        request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.addHeader("Accept-Language", "en-US,en;q=0.5");

        // Act
        String fingerprint = fingerprintService.generateFingerprint(request);

        // Assert
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
        assertThat(fingerprint).hasSize(64);
        assertThat(fingerprint).matches("^[a-f0-9]{64}$");
    }
}
