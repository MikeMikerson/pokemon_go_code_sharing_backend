package com.devs.simplicity.poke_go_friends.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing HTML content and potentially harmful input.
 * Removes HTML tags, script content, and other potentially dangerous elements.
 */
@Component
public class HtmlSanitizer {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern STYLE_PATTERN = Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern ON_EVENT_PATTERN = Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_URL_PATTERN = Pattern.compile("data:", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitizes the input string by removing HTML tags and potentially harmful content.
     * 
     * @param input the input string to sanitize
     * @return the sanitized string, or null if input was null
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String sanitized = input;

        // Remove script tags and their content
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // Remove style tags and their content
        sanitized = STYLE_PATTERN.matcher(sanitized).replaceAll("");

        // Remove all HTML tags
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");

        // Remove on* event handlers
        sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");

        // Remove javascript: URLs
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // Remove vbscript: URLs
        sanitized = VBSCRIPT_PATTERN.matcher(sanitized).replaceAll("");

        // Remove data: URLs (can be used for XSS)
        sanitized = DATA_URL_PATTERN.matcher(sanitized).replaceAll("");

        // Trim whitespace and normalize
        sanitized = sanitized.trim();

        return sanitized;
    }

    /**
     * Checks if the input contains potentially harmful content.
     * 
     * @param input the input string to check
     * @return true if the input contains potentially harmful content, false otherwise
     */
    public boolean containsHarmfulContent(String input) {
        if (input == null) {
            return false;
        }

        return HTML_TAG_PATTERN.matcher(input).find() ||
               SCRIPT_PATTERN.matcher(input).find() ||
               STYLE_PATTERN.matcher(input).find() ||
               ON_EVENT_PATTERN.matcher(input).find() ||
               JAVASCRIPT_PATTERN.matcher(input).find() ||
               VBSCRIPT_PATTERN.matcher(input).find() ||
               DATA_URL_PATTERN.matcher(input).find();
    }
}
