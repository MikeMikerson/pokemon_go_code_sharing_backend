package com.devs.simplicity.poke_go_friends.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Service for generating user fingerprints from HTTP requests.
 * Uses IP address and User-Agent information to create a consistent,
 * privacy-safe identifier for rate limiting purposes.
 */
@Slf4j
@Service
public class FingerprintService {

    private static final String UNKNOWN_IP = "unknown";
    private static final String UNKNOWN_USER_AGENT = "unknown";

    /**
     * Generates a consistent fingerprint from an HTTP request.
     * The fingerprint is based on the client's IP address and User-Agent,
     * with proper handling of proxy headers and privacy considerations.
     * 
     * @param request the HTTP request to generate fingerprint from
     * @return a SHA-256 hash string representing the user's fingerprint
     */
    public String generateFingerprint(HttpServletRequest request) {
        log.debug("Generating fingerprint for request from: {}", getClientIpAddress(request));
        
        String clientIp = getClientIpAddress(request);
        String userAgent = getUserAgent(request);
        
        // Create combined string for hashing
        String combinedString = clientIp + "|" + userAgent;
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combinedString.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String fingerprint = hexString.toString();
            log.debug("Generated fingerprint: {} for IP: {} and User-Agent: {}", 
                     fingerprint, maskIpForLogging(clientIp), maskUserAgentForLogging(userAgent));
            
            return fingerprint;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            // Fallback to a simple hash if SHA-256 is not available
            return String.valueOf(combinedString.hashCode());
        }
    }

    /**
     * Extracts the real client IP address from the request,
     * considering various proxy headers.
     * 
     * @param request the HTTP request
     * @return the client's IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (standard for HTTP proxies and load balancers)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (isValidIpAddress(xForwardedFor)) {
            // Take the first IP in the chain (original client)
            String clientIp = xForwardedFor.split(",")[0].trim();
            if (isValidIpAddress(clientIp)) {
                return clientIp;
            }
        }
        
        // Check X-Real-IP header (common with Nginx reverse proxy)
        String xRealIp = request.getHeader("X-Real-IP");
        if (isValidIpAddress(xRealIp)) {
            return xRealIp.trim();
        }
        
        // Check X-Forwarded header
        String xForwarded = request.getHeader("X-Forwarded");
        if (isValidIpAddress(xForwarded)) {
            return xForwarded.trim();
        }
        
        // Check Forwarded-For header
        String forwardedFor = request.getHeader("Forwarded-For");
        if (isValidIpAddress(forwardedFor)) {
            return forwardedFor.trim();
        }
        
        // Check Forwarded header
        String forwarded = request.getHeader("Forwarded");
        if (isValidIpAddress(forwarded)) {
            return forwarded.trim();
        }
        
        // Fall back to remote address
        String remoteAddr = request.getRemoteAddr();
        if (isValidIpAddress(remoteAddr)) {
            return remoteAddr;
        }
        
        return UNKNOWN_IP;
    }

    /**
     * Extracts and normalizes the User-Agent from the request.
     * 
     * @param request the HTTP request
     * @return the normalized User-Agent string
     */
    private String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return UNKNOWN_USER_AGENT;
        }
        
        // Normalize by trimming whitespace
        return userAgent.trim();
    }

    /**
     * Validates if a string represents a valid IP address.
     * 
     * @param ip the IP address string to validate
     * @return true if the IP address is valid and not a known proxy identifier
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        ip = ip.trim();
        
        // Check for known invalid values
        if ("unknown".equalsIgnoreCase(ip) || 
            "localhost".equalsIgnoreCase(ip) ||
            "127.0.0.1".equals(ip) ||
            "0:0:0:0:0:0:0:1".equals(ip) ||
            "::1".equals(ip)) {
            return false;
        }
        
        return true;
    }

    /**
     * Masks IP address for secure logging.
     * 
     * @param ip the IP address to mask
     * @return masked IP address for logging
     */
    private String maskIpForLogging(String ip) {
        if (ip == null || ip.equals(UNKNOWN_IP)) {
            return ip;
        }
        
        // Mask the last octet for IPv4
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + "." + parts[2] + ".***";
            }
        }
        
        // For IPv6 or other formats, just show first few characters
        if (ip.length() > 8) {
            return ip.substring(0, 8) + "***";
        }
        
        return "***";
    }

    /**
     * Masks User-Agent for secure logging.
     * 
     * @param userAgent the User-Agent to mask
     * @return masked User-Agent for logging
     */
    private String maskUserAgentForLogging(String userAgent) {
        if (userAgent == null || userAgent.equals(UNKNOWN_USER_AGENT)) {
            return userAgent;
        }
        
        // Show first 20 characters and mask the rest
        if (userAgent.length() > 20) {
            return userAgent.substring(0, 20) + "***";
        }
        
        return userAgent;
    }
}
