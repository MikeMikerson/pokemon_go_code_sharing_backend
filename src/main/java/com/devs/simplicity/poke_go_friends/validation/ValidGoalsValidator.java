package com.devs.simplicity.poke_go_friends.validation;

import com.devs.simplicity.poke_go_friends.entity.Goal;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class ValidGoalsValidator implements ConstraintValidator<ValidGoals, Set<Goal>> {
    @Override
    public boolean isValid(Set<Goal> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null is allowed (optional field)
        }
        for (Goal goal : value) {
            if (goal != Goal.GIFTS && goal != Goal.EXP && goal != Goal.RAIDS && goal != Goal.ALL) {
                return false;
            }
        }
        return true;
    }
}
