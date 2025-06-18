package com.devs.simplicity.poke_go_friends.validation;

import com.devs.simplicity.poke_go_friends.util.HtmlSanitizer;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NoHtmlValidator.
 * Tests the validation logic for HTML content detection.
 */
@DisplayName("NoHtmlValidator Tests")
@ExtendWith(MockitoExtension.class)
class NoHtmlValidatorTest {

    @Mock
    private HtmlSanitizer htmlSanitizer;

    @Mock
    private ConstraintValidatorContext context;

    private NoHtmlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NoHtmlValidator(htmlSanitizer);
    }

    @Test
    @DisplayName("Null value should pass validation")
    void isValid_nullValue_shouldReturnTrue() {
        // When
        boolean result = validator.isValid(null, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Clean text should pass validation")
    void isValid_cleanText_shouldReturnTrue() {
        // Given
        String cleanText = "Hello world";
        when(htmlSanitizer.containsHarmfulContent(cleanText)).thenReturn(false);

        // When
        boolean result = validator.isValid(cleanText, context);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Text with HTML should fail validation")
    void isValid_htmlContent_shouldReturnFalse() {
        // Given
        String htmlText = "Hello <b>world</b>";
        when(htmlSanitizer.containsHarmfulContent(htmlText)).thenReturn(true);

        // When
        boolean result = validator.isValid(htmlText, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Text with script should fail validation")
    void isValid_scriptContent_shouldReturnFalse() {
        // Given
        String scriptText = "Hello <script>alert('xss')</script>";
        when(htmlSanitizer.containsHarmfulContent(scriptText)).thenReturn(true);

        // When
        boolean result = validator.isValid(scriptText, context);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Text with JavaScript URL should fail validation")
    void isValid_javascriptUrl_shouldReturnFalse() {
        // Given
        String jsUrl = "javascript:alert('xss')";
        when(htmlSanitizer.containsHarmfulContent(jsUrl)).thenReturn(true);

        // When
        boolean result = validator.isValid(jsUrl, context);

        // Then
        assertThat(result).isFalse();
    }
}
