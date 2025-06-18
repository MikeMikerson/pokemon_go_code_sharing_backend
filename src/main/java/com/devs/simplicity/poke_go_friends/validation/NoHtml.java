package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure that a string does not contain HTML or script content.
 * Useful for preventing XSS attacks through user input.
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {

    String message() default "HTML and script content is not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
