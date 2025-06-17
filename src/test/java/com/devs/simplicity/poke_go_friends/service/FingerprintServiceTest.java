package com.devs.simplicity.poke_go_friends.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FingerprintService")
class FingerprintServiceTest {

    private FingerprintService fingerprintService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        fingerprintService = new FingerprintService();
        
        request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
    }

    @Test
    @DisplayName("should generate consistent fingerprint for same IP and User-Agent")
    void generateFingerprint_sameIpAndUserAgent_shouldReturnConsistentFingerprint() {
        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        String fingerprint2 = fingerprintService.generateFingerprint(request);

        // Assert
        assertThat(fingerprint1).isNotNull();
        assertThat(fingerprint1).isNotEmpty();
        assertThat(fingerprint1).isEqualTo(fingerprint2);
    }

    @Test
    @DisplayName("should generate different fingerprints for different IP addresses")
    void generateFingerprint_differentIpAddresses_shouldReturnDifferentFingerprints() {
        // Arrange
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("192.168.1.101"); // Different IP
        request2.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        String fingerprint2 = fingerprintService.generateFingerprint(request2);

        // Assert
        assertThat(fingerprint1).isNotEqualTo(fingerprint2);
    }

    @Test
    @DisplayName("should generate different fingerprints for different User-Agents")
    void generateFingerprint_differentUserAgents_shouldReturnDifferentFingerprints() {
        // Arrange
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("192.168.1.100"); // Same IP
        request2.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        String fingerprint2 = fingerprintService.generateFingerprint(request2);

        // Assert
        assertThat(fingerprint1).isNotEqualTo(fingerprint2);
    }

    @Test
    @DisplayName("should handle missing User-Agent header gracefully")
    void generateFingerprint_missingUserAgent_shouldHandleGracefully() {
        // Arrange
        request.removeHeader("User-Agent");

        // Act
        String fingerprint = fingerprintService.generateFingerprint(request);

        // Assert
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
    }

    @Test
    @DisplayName("should handle null IP address gracefully")
    void generateFingerprint_nullIpAddress_shouldHandleGracefully() {
        // Arrange
        request.setRemoteAddr(null);

        // Act
        String fingerprint = fingerprintService.generateFingerprint(request);

        // Assert
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).isNotEmpty();
    }

    @Test
    @DisplayName("should generate fingerprint that is privacy-safe")
    void generateFingerprint_privacySafe_shouldNotContainRawIp() {
        // Act
        String fingerprint = fingerprintService.generateFingerprint(request);

        // Assert
        assertThat(fingerprint).doesNotContain("192.168.1.100"); // Should not contain raw IP
        assertThat(fingerprint).hasSize(64); // SHA-256 hex string length
        assertThat(fingerprint).matches("^[a-f0-9]{64}$"); // Should be hex string
    }

    @Test
    @DisplayName("should handle X-Forwarded-For header for proxy scenarios")
    void generateFingerprint_withXForwardedFor_shouldUseOriginalIp() {
        // Arrange
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        
        // Create another request with same X-Forwarded-For
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("192.168.1.200"); // Different proxy IP
        request2.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        request2.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        String fingerprint2 = fingerprintService.generateFingerprint(request2);

        // Assert
        assertThat(fingerprint1).isEqualTo(fingerprint2); // Should be same due to same original IP
    }

    @Test
    @DisplayName("should handle X-Real-IP header for reverse proxy scenarios")
    void generateFingerprint_withXRealIp_shouldUseRealIp() {
        // Arrange
        request.addHeader("X-Real-IP", "203.0.113.195");

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        
        // Create another request with same X-Real-IP
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("192.168.1.200"); // Different proxy IP
        request2.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        request2.addHeader("X-Real-IP", "203.0.113.195");
        String fingerprint2 = fingerprintService.generateFingerprint(request2);

        // Assert
        assertThat(fingerprint1).isEqualTo(fingerprint2); // Should be same due to same real IP
    }

    @Test
    @DisplayName("should normalize User-Agent strings for consistent fingerprints")
    void generateFingerprint_normalizedUserAgent_shouldBeConsistent() {
        // Arrange
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("192.168.1.100");
        request2.addHeader("User-Agent", "   Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36   "); // With whitespace

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request);
        String fingerprint2 = fingerprintService.generateFingerprint(request2);

        // Assert
        assertThat(fingerprint1).isEqualTo(fingerprint2); // Should be same after normalization
    }

    @Test
    @DisplayName("should generate stable fingerprints for load balancer scenarios")
    void generateFingerprint_loadBalancerScenario_shouldBeStable() {
        // Arrange - Simulate requests through different load balancer nodes
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setRemoteAddr("10.0.1.5"); // Load balancer node 1
        request1.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        request1.addHeader("X-Forwarded-For", "203.0.113.195");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRemoteAddr("10.0.1.6"); // Load balancer node 2
        request2.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        request2.addHeader("X-Forwarded-For", "203.0.113.195");

        // Act
        String fingerprint1 = fingerprintService.generateFingerprint(request1);
        String fingerprint2 = fingerprintService.generateFingerprint(request2);

        // Assert
        assertThat(fingerprint1).isEqualTo(fingerprint2); // Should be identical despite different LB nodes
    }
}
