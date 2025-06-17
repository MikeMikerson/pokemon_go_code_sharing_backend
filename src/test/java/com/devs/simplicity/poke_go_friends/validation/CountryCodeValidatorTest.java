package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CountryCodeValidator.
 * Tests validation logic for supported countries.
 */
@DisplayName("CountryCodeValidator Tests")
@ExtendWith(MockitoExtension.class)
class CountryCodeValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private CountryCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CountryCodeValidator();
        validator.initialize(null); // No initialization needed
    }

    @Test
    @DisplayName("Valid countries should pass validation")
    void validCountries_shouldPassValidation() {
        // Given
        String[] validCountries = {
                "United States",
                "Canada",
                "United Kingdom",
                "Australia",
                "Germany",
                "France",
                "Japan",
                "Other"
        };

        // When & Then
        for (String country : validCountries) {
            boolean isValid = validator.isValid(country, context);
            assertThat(isValid).as("Country '%s' should be valid", country).isTrue();
        }
    }

    @Test
    @DisplayName("Invalid countries should fail validation")
    void invalidCountries_shouldFailValidation() {
        // Given
        String[] invalidCountries = {
                "Invalid Country",
                "NonExistentPlace",
                "United states", // Case sensitive
                "CANADA", // Case sensitive
                "usa",
                "uk"
        };

        // When & Then
        for (String country : invalidCountries) {
            boolean isValid = validator.isValid(country, context);
            assertThat(isValid).as("Country '%s' should be invalid", country).isFalse();
        }
    }

    @Test
    @DisplayName("Null country should pass validation")
    void nullCountry_shouldPassValidation() {
        // Given & When
        boolean isValid = validator.isValid(null, context);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Empty string should fail validation")
    void emptyString_shouldFailValidation() {
        // Given & When
        boolean isValid = validator.isValid("", context);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Whitespace only should fail validation")
    void whitespaceOnly_shouldFailValidation() {
        // Given & When
        boolean isValid = validator.isValid("   ", context);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("All major regions should be supported")
    void allMajorRegions_shouldBeSupported() {
        // Given
        String[] majorCountries = {
                // North America
                "United States",
                "Canada",
                "Mexico",
                
                // Europe
                "United Kingdom",
                "Germany",
                "France",
                "Spain",
                "Italy",
                "Netherlands",
                
                // Asia
                "Japan",
                "South Korea",
                "Singapore",
                "India",
                
                // Oceania
                "Australia",
                "New Zealand",
                
                // South America
                "Brazil",
                "Argentina",
                
                // Other
                "Other"
        };

        // When & Then
        for (String country : majorCountries) {
            boolean isValid = validator.isValid(country, context);
            assertThat(isValid).as("Major country '%s' should be supported", country).isTrue();
        }
    }

    @Test
    @DisplayName("Validator should be case sensitive")
    void validator_shouldBeCaseSensitive() {
        // Given
        String correctCase = "United States";
        String wrongCase = "united states";

        // When
        boolean correctCaseValid = validator.isValid(correctCase, context);
        boolean wrongCaseValid = validator.isValid(wrongCase, context);

        // Then
        assertThat(correctCaseValid).isTrue();
        assertThat(wrongCaseValid).isFalse();
    }

    @Test
    @DisplayName("Special characters in country name should fail validation")
    void specialCharacters_shouldFailValidation() {
        // Given
        String[] invalidCountries = {
                "United States!",
                "Canada@",
                "Germany#",
                "France$",
                "123Country"
        };

        // When & Then
        for (String country : invalidCountries) {
            boolean isValid = validator.isValid(country, context);
            assertThat(isValid).as("Country with special characters '%s' should be invalid", country).isFalse();
        }
    }
}
