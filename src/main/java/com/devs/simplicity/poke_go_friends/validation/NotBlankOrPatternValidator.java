package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator for the NotBlankOrPattern annotation.
 * Returns true if the string is blank (null, empty, or whitespace only) 
 * or if the string matches the specified regex pattern.
 */
public class NotBlankOrPatternValidator implements ConstraintValidator<NotBlankOrPattern, String> {
    
    private Pattern pattern;
    private String patternMessage;
    
    @Override
    public void initialize(NotBlankOrPattern constraintAnnotation) {
        String regex = constraintAnnotation.regexp();
        this.pattern = Pattern.compile(regex);
        this.patternMessage = constraintAnnotation.patternMessage();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Allow null, empty, or whitespace-only strings
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        
        // Apply pattern validation for non-blank strings
        boolean isValid = pattern.matcher(value).matches();
        
        // If validation fails and a custom pattern message is provided, use it
        if (!isValid && !patternMessage.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(patternMessage)
                   .addConstraintViolation();
        }
        
        return isValid;
    }
}
