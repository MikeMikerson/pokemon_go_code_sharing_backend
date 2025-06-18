package com.devs.simplicity.poke_go_friends.validation;

import com.devs.simplicity.poke_go_friends.util.HtmlSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link NoHtml} annotation.
 * Checks that the input string does not contain HTML or script content.
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

    private final HtmlSanitizer htmlSanitizer;

    public NoHtmlValidator() {
        this.htmlSanitizer = new HtmlSanitizer();
    }

    // Package-private constructor for testing
    NoHtmlValidator(HtmlSanitizer htmlSanitizer) {
        this.htmlSanitizer = htmlSanitizer;
    }

    @Override
    public void initialize(NoHtml constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation if required
        }

        return !htmlSanitizer.containsHarmfulContent(value);
    }
}
