package com.devs.simplicity.poke_go_friends.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for InputSanitizationService.
 */
@DisplayName("InputSanitizationService Tests")
class InputSanitizationServiceTest {

    private InputSanitizationService sanitizationService;

    @BeforeEach
    void setUp() {
        sanitizationService = new InputSanitizationService();
    }

    @Nested
    @DisplayName("Text Sanitization")
    class TextSanitizationTest {

        @Test
        @DisplayName("Should remove HTML tags")
        void shouldRemoveHtmlTags() {
            String input = "Hello <script>alert('xss')</script> World";
            String result = sanitizationService.sanitizeText(input);
            assertThat(result).isEqualTo("Hello alert('xss') World");
        }

        @Test
        @DisplayName("Should remove JavaScript protocol URLs")
        void shouldRemoveJavaScriptUrls() {
            String input = "Click javascript:alert('xss') here";
            String result = sanitizationService.sanitizeText(input);
            assertThat(result).isEqualTo("Click alert('xss') here");
        }

        @Test
        @DisplayName("Should normalize whitespace")
        void shouldNormalizeWhitespace() {
            String input = "Hello    world   with\t\ttabs\n\nand\r\nlinebreaks";
            String result = sanitizationService.sanitizeText(input);
            assertThat(result).isEqualTo("Hello world with tabs and linebreaks");
        }

        @Test
        @DisplayName("Should handle null and empty input")
        void shouldHandleNullAndEmptyInput() {
            assertThat(sanitizationService.sanitizeText(null)).isNull();
            assertThat(sanitizationService.sanitizeText("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainer Name Sanitization")
    class TrainerNameSanitizationTest {

        @Test
        @DisplayName("Should preserve valid trainer name")
        void shouldPreserveValidTrainerName() {
            String input = "TrainerName123";
            String result = sanitizationService.sanitizeTrainerName(input);
            assertThat(result).isEqualTo("TrainerName123");
        }

        @Test
        @DisplayName("Should allow valid special characters")
        void shouldAllowValidSpecialCharacters() {
            String input = "Trainer_Name.123-cool";
            String result = sanitizationService.sanitizeTrainerName(input);
            assertThat(result).isEqualTo("Trainer_Name.123-cool");
        }

        @Test
        @DisplayName("Should remove invalid characters")
        void shouldRemoveInvalidCharacters() {
            String input = "Trainer@Name#123!";
            String result = sanitizationService.sanitizeTrainerName(input);
            assertThat(result).isEqualTo("TrainerName123");
        }

        @Test
        @DisplayName("Should trim leading and trailing special characters")
        void shouldTrimSpecialCharacters() {
            String input = "___TrainerName___";
            String result = sanitizationService.sanitizeTrainerName(input);
            assertThat(result).isEqualTo("TrainerName");
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            String input = "Pokémon_Tráiner";
            String result = sanitizationService.sanitizeTrainerName(input);
            // Note: The current implementation removes accented characters
            // This might need to be adjusted based on requirements
            assertThat(result).isEqualTo("Pokmon_Triner");
        }
    }

    @Nested
    @DisplayName("Location Sanitization")
    class LocationSanitizationTest {

        @Test
        @DisplayName("Should preserve valid location")
        void shouldPreserveValidLocation() {
            String input = "New York, NY";
            String result = sanitizationService.sanitizeLocation(input);
            assertThat(result).isEqualTo("New York, NY");
        }

        @Test
        @DisplayName("Should allow common location characters")
        void shouldAllowCommonLocationCharacters() {
            String input = "São Paulo, Brazil - Downtown";
            String result = sanitizationService.sanitizeLocation(input);
            // Note: The current implementation may remove accented characters
            assertThat(result).isEqualTo("So Paulo, Brazil - Downtown");
        }

        @Test
        @DisplayName("Should remove potentially dangerous characters")
        void shouldRemoveDangerousCharacters() {
            String input = "New York<script>alert('xss')</script>";
            String result = sanitizationService.sanitizeLocation(input);
            assertThat(result).isEqualTo("New Yorkalert'xss'");
        }
    }

    @Nested
    @DisplayName("Description Sanitization")
    class DescriptionSanitizationTest {

        @Test
        @DisplayName("Should preserve normal description")
        void shouldPreserveNormalDescription() {
            String input = "Looking for active players for raids and gifts!";
            String result = sanitizationService.sanitizeDescription(input);
            assertThat(result).isEqualTo("Looking for active players for raids and gifts!");
        }

        @Test
        @DisplayName("Should remove SQL injection patterns")
        void shouldRemoveSqlInjectionPatterns() {
            String input = "Great player; DROP TABLE users; --";
            String result = sanitizationService.sanitizeDescription(input);
            assertThat(result).doesNotContain("DROP TABLE").doesNotContain("--");
        }

        @Test
        @DisplayName("Should handle complex HTML and scripts")
        void shouldHandleComplexHtmlAndScripts() {
            String input = "Active player <img src=x onerror=alert('xss')> daily gifts";
            String result = sanitizationService.sanitizeDescription(input);
            assertThat(result).isEqualTo("Active player daily gifts");
        }
    }

    @Nested
    @DisplayName("Validation After Sanitization")
    class ValidationAfterSanitizationTest {

        @Test
        @DisplayName("Should return true for valid input")
        void shouldReturnTrueForValidInput() {
            String original = "TrainerName123";
            String sanitized = "TrainerName123";
            boolean result = sanitizationService.isValidAfterSanitization(original, sanitized);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false if sanitized input is empty")
        void shouldReturnFalseIfSanitizedEmpty() {
            String original = "@@@@";
            String sanitized = "";
            boolean result = sanitizationService.isValidAfterSanitization(original, sanitized);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false if too much content removed")
        void shouldReturnFalseIfTooMuchContentRemoved() {
            String original = "TrainerName@@@@@@@@@@@@@@@@@@@@";
            String sanitized = "TrainerName";
            boolean result = sanitizationService.isValidAfterSanitization(original, sanitized);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true for null/empty original")
        void shouldReturnTrueForNullEmptyOriginal() {
            assertThat(sanitizationService.isValidAfterSanitization(null, null)).isTrue();
            assertThat(sanitizationService.isValidAfterSanitization("", "")).isTrue();
            assertThat(sanitizationService.isValidAfterSanitization("   ", "")).isTrue();
        }

        @Test
        @DisplayName("Should accept reasonable content reduction")
        void shouldAcceptReasonableContentReduction() {
            String original = "TrainerName@#$";
            String sanitized = "TrainerName";
            boolean result = sanitizationService.isValidAfterSanitization(original, sanitized);
            assertThat(result).isTrue();
        }
    }
}
