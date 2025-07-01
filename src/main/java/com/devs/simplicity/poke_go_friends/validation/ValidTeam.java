package com.devs.simplicity.poke_go_friends.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidTeamValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidTeam {
    String message() default "Team must be one of: mystic, valor, instinct";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
