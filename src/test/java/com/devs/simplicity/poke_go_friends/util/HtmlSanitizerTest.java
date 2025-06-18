package com.devs.simplicity.poke_go_friends.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HtmlSanitizer.
 * Tests HTML sanitization and XSS prevention functionality.
 */
@DisplayName("HtmlSanitizer Tests")
class HtmlSanitizerTest {

    private HtmlSanitizer htmlSanitizer;

    @BeforeEach
    void setUp() {
        htmlSanitizer = new HtmlSanitizer();
    }

    @Test
    @DisplayName("Null input should return null")
    void sanitize_nullInput_shouldReturnNull() {
        // When
        String result = htmlSanitizer.sanitize(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Clean text should remain unchanged")
    void sanitize_cleanText_shouldRemainUnchanged() {
        // Given
        String cleanText = "Hello, I'm looking for active friends!";

        // When
        String result = htmlSanitizer.sanitize(cleanText);

        // Then
        assertThat(result).isEqualTo(cleanText);
    }

    @Test
    @DisplayName("Script tags should be removed")
    void sanitize_scriptTags_shouldBeRemoved() {
        // Given
        String maliciousText = "Hello <script>alert('xss')</script> world";

        // When
        String result = htmlSanitizer.sanitize(maliciousText);

        // Then
        assertThat(result).isEqualTo("Hello  world");
    }

    @Test
    @DisplayName("HTML tags should be removed")
    void sanitize_htmlTags_shouldBeRemoved() {
        // Given
        String htmlText = "Hello <b>bold</b> and <i>italic</i> text";

        // When
        String result = htmlSanitizer.sanitize(htmlText);

        // Then
        assertThat(result).isEqualTo("Hello bold and italic text");
    }

    @Test
    @DisplayName("JavaScript URLs should be removed")
    void sanitize_javascriptUrls_shouldBeRemoved() {
        // Given
        String maliciousText = "Click here: javascript:alert('xss')";

        // When
        String result = htmlSanitizer.sanitize(maliciousText);

        // Then
        assertThat(result).isEqualTo("Click here: alert('xss')");
    }

    @Test
    @DisplayName("Event handlers should be removed")
    void sanitize_eventHandlers_shouldBeRemoved() {
        // Given
        String maliciousText = "Image onload=alert('xss') here";

        // When
        String result = htmlSanitizer.sanitize(maliciousText);

        // Then
        assertThat(result).isEqualTo("Image alert('xss') here");
    }

    @Test
    @DisplayName("Data URLs should be removed")
    void sanitize_dataUrls_shouldBeRemoved() {
        // Given
        String maliciousText = "Image data:text/html,<script>alert('xss')</script> here";

        // When
        String result = htmlSanitizer.sanitize(maliciousText);

        // Then
        assertThat(result).isEqualTo("Image text/html, here");
    }

    @Test
    @DisplayName("Style tags should be removed")
    void sanitize_styleTags_shouldBeRemoved() {
        // Given
        String maliciousText = "Text <style>body{background:url('javascript:alert(1)')}</style> here";

        // When
        String result = htmlSanitizer.sanitize(maliciousText);

        // Then
        assertThat(result).isEqualTo("Text  here");
    }

    @Test
    @DisplayName("Complex XSS payload should be neutralized")
    void sanitize_complexXssPayload_shouldBeNeutralized() {
        // Given
        String maliciousText = "<script>alert('xss')</script><img src=x onerror=alert('xss')><a href=\"javascript:alert('xss')\">click</a>";

        // When
        String result = htmlSanitizer.sanitize(maliciousText);

        // Then
        assertThat(result).isEqualTo("click");
        assertThat(result).doesNotContain("script");
        assertThat(result).doesNotContain("javascript");
        assertThat(result).doesNotContain("onerror");
    }

    @Test
    @DisplayName("containsHarmfulContent should detect script tags")
    void containsHarmfulContent_scriptTags_shouldReturnTrue() {
        // Given
        String maliciousText = "Hello <script>alert('xss')</script> world";

        // When
        boolean result = htmlSanitizer.containsHarmfulContent(maliciousText);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("containsHarmfulContent should detect HTML tags")
    void containsHarmfulContent_htmlTags_shouldReturnTrue() {
        // Given
        String htmlText = "Hello <b>bold</b> text";

        // When
        boolean result = htmlSanitizer.containsHarmfulContent(htmlText);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("containsHarmfulContent should detect JavaScript URLs")
    void containsHarmfulContent_javascriptUrls_shouldReturnTrue() {
        // Given
        String maliciousText = "Click javascript:alert('xss')";

        // When
        boolean result = htmlSanitizer.containsHarmfulContent(maliciousText);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("containsHarmfulContent should return false for clean text")
    void containsHarmfulContent_cleanText_shouldReturnFalse() {
        // Given
        String cleanText = "Hello, I'm looking for active friends!";

        // When
        boolean result = htmlSanitizer.containsHarmfulContent(cleanText);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("containsHarmfulContent should return false for null input")
    void containsHarmfulContent_nullInput_shouldReturnFalse() {
        // When
        boolean result = htmlSanitizer.containsHarmfulContent(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Whitespace should be trimmed")
    void sanitize_whitespace_shouldBeTrimmed() {
        // Given
        String textWithWhitespace = "  Hello world  ";

        // When
        String result = htmlSanitizer.sanitize(textWithWhitespace);

        // Then
        assertThat(result).isEqualTo("Hello world");
    }
}
