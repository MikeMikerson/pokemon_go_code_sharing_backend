package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator for country codes.
 * Validates that the provided country is in the list of supported countries.
 */
public class CountryCodeValidator implements ConstraintValidator<CountryCode, String> {

    private static final Set<String> VALID_COUNTRIES = Set.of(
            "United States",
            "Canada",
            "United Kingdom",
            "Australia",
            "Germany",
            "France",
            "Spain",
            "Italy",
            "Netherlands",
            "Belgium",
            "Sweden",
            "Norway",
            "Denmark",
            "Finland",
            "Poland",
            "Czech Republic",
            "Austria",
            "Switzerland",
            "Portugal",
            "Ireland",
            "Japan",
            "South Korea",
            "Taiwan",
            "Singapore",
            "Hong Kong",
            "New Zealand",
            "Brazil",
            "Mexico",
            "Argentina",
            "Chile",
            "Colombia",
            "Peru",
            "India",
            "Thailand",
            "Indonesia",
            "Malaysia",
            "Philippines",
            "Vietnam",
            "South Africa",
            "Israel",
            "Turkey",
            "Russia",
            "Ukraine",
            "Romania",
            "Bulgaria",
            "Hungary",
            "Croatia",
            "Slovenia",
            "Slovakia",
            "Lithuania",
            "Latvia",
            "Estonia",
            "Greece",
            "Cyprus",
            "Malta",
            "Luxembourg",
            "Iceland",
            "Other"
    );

    @Override
    public void initialize(CountryCode constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String country, ConstraintValidatorContext context) {
        if (country == null) {
            return true; // Let @NotNull handle null validation if required
        }

        return VALID_COUNTRIES.contains(country);
    }
}
