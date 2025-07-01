package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidGoalsValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidGoals {
    String message() default "Each goal must be one of: gifts, exp, raids, all";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
