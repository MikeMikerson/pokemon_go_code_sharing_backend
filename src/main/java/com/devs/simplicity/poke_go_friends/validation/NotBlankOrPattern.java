package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom validation annotation that allows blank strings but validates non-blank strings against a regex pattern.
 * This is useful for optional fields that should follow a specific format when provided.
 */
@Documented
@Constraint(validatedBy = NotBlankOrPatternValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface NotBlankOrPattern {
    String message() default "Field must be blank or match the specified pattern";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * The regex pattern that non-blank values must match.
     */
    String regexp();
    
    /**
     * Optional custom message for pattern validation failures.
     */
    String patternMessage() default "";
}
