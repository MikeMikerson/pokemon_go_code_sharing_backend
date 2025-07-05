package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotBlankOrPatternValidator.
 * Tests validation logic for blank and patterned strings.
 */
@DisplayName("NotBlankOrPatternValidator Tests")
class NotBlankOrPatternValidatorTest {

    private NotBlankOrPatternValidator validator;

    @Mock
    private NotBlankOrPattern annotation;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new NotBlankOrPatternValidator();
        
        // Set up annotation mock with default values
        when(annotation.regexp()).thenReturn("^[a-zA-Z0-9]*$");
        when(annotation.patternMessage()).thenReturn(""); // Default to empty message
        
        validator.initialize(annotation);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   ", "\t", "\n", "  \t  "})
    @DisplayName("Should return true for null, empty, or whitespace-only strings")
    void shouldReturnTrueForBlankStrings(String value) {
        // When & Then
        assertTrue(validator.isValid(value, context));
        verifyNoInteractions(context);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "ABC", "123", "abc123", "ABC123", "a1b2c3"})
    @DisplayName("Should return true for valid alphanumeric strings")
    void shouldReturnTrueForValidAlphanumericStrings(String value) {
        // When & Then
        assertTrue(validator.isValid(value, context));
        verifyNoInteractions(context);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc!", "hello@world", "test#123", "name with space", "special$char"})
    @DisplayName("Should return false for strings with invalid characters")
    void shouldReturnFalseForInvalidStrings(String value) {
        // When & Then
        assertFalse(validator.isValid(value, context));
        // Don't verify context interactions since we don't use custom message by default
    }

    @Test
    @DisplayName("Should use custom pattern message when validation fails")
    void shouldUseCustomPatternMessageWhenValidationFails() {
        // Given
        String customMessage = "Custom validation error message";
        when(annotation.patternMessage()).thenReturn(customMessage);
        when(context.buildConstraintViolationWithTemplate(customMessage)).thenReturn(violationBuilder);
        
        validator.initialize(annotation);
        String invalidValue = "invalid@value";

        // When
        boolean result = validator.isValid(invalidValue, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(customMessage);
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    @DisplayName("Should not use custom message when patternMessage is empty")
    void shouldNotUseCustomMessageWhenPatternMessageIsEmpty() {
        // Given
        when(annotation.patternMessage()).thenReturn("");
        validator.initialize(annotation);
        String invalidValue = "invalid@value";

        // When
        boolean result = validator.isValid(invalidValue, context);

        // Then
        assertFalse(result);
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Should work with different regex patterns")
    void shouldWorkWithDifferentRegexPatterns() {
        // Given - email pattern
        when(annotation.regexp()).thenReturn("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        when(annotation.patternMessage()).thenReturn("");
        validator.initialize(annotation);

        // When & Then
        assertTrue(validator.isValid("test@example.com", context));
        assertTrue(validator.isValid("", context));
        assertTrue(validator.isValid(null, context));
        assertFalse(validator.isValid("invalid-email", context));
        assertFalse(validator.isValid("test@", context));
    }
}
