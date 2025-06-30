package com.devs.simplicity.poke_go_friends.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Service for sanitizing and cleaning user input to prevent XSS and other attacks.
 */
@Service
public class InputSanitizationService {
    
    // Pattern to remove potentially dangerous HTML/script tags
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    
    // Pattern to remove JavaScript protocol URLs
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    
    // Pattern to normalize whitespace
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    
    // Pattern for SQL injection prevention (basic)
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT|SELECT|UNION|UPDATE)\\b)|('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * Sanitizes text input by removing HTML tags, normalizing whitespace,
     * and preventing basic injection attacks.
     *
     * @param input The input text to sanitize
     * @return Sanitized text, or null if input was null
     */
    public String sanitizeText(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        String sanitized = input;
        
        // Remove HTML tags
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove JavaScript protocol URLs
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Normalize whitespace
        sanitized = WHITESPACE_PATTERN.matcher(sanitized).replaceAll(" ");
        
        // Trim leading and trailing whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }
    
    /**
     * Sanitizes trainer name input with specific rules for Pokemon Go trainer names.
     *
     * @param trainerName The trainer name to sanitize
     * @return Sanitized trainer name
     */
    public String sanitizeTrainerName(String trainerName) {
        if (!StringUtils.hasText(trainerName)) {
            return trainerName;
        }
        
        String sanitized = sanitizeText(trainerName);
        
        // Additional trainer name specific cleaning
        // Remove any remaining special characters that aren't allowed
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s._-]", "");
        
        // Ensure no leading/trailing spaces or special characters
        sanitized = sanitized.replaceAll("^[\\s._-]+|[\\s._-]+$", "");
        
        return sanitized;
    }
    
    /**
     * Sanitizes location input.
     *
     * @param location The location to sanitize
     * @return Sanitized location
     */
    public String sanitizeLocation(String location) {
        if (!StringUtils.hasText(location)) {
            return location;
        }
        
        String sanitized = sanitizeText(location);
        
        // Allow common location characters (letters, numbers, spaces, commas, periods, hyphens)
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s,.'-]", "");
        
        return sanitized;
    }
    
    /**
     * Sanitizes description input.
     *
     * @param description The description to sanitize
     * @return Sanitized description
     */
    public String sanitizeDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return description;
        }
        
        String sanitized = sanitizeText(description);
        
        // Allow most printable characters but remove potential injection patterns
        if (SQL_INJECTION_PATTERN.matcher(sanitized).find()) {
            // Remove suspicious SQL-like patterns
            sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        }
        
        return sanitized;
    }
    
    /**
     * Validates that the sanitized input is still meaningful after cleaning.
     *
     * @param original The original input
     * @param sanitized The sanitized input
     * @return true if the sanitized input is still valid
     */
    public boolean isValidAfterSanitization(String original, String sanitized) {
        if (!StringUtils.hasText(original)) {
            return true; // Null/empty input is fine
        }
        
        if (!StringUtils.hasText(sanitized)) {
            return false; // Input became empty after sanitization
        }
        
        // Check if too much content was removed (more than 50% of original length)
        double retentionRatio = (double) sanitized.length() / original.length();
        return retentionRatio >= 0.5;
    }
}
