package com.devs.simplicity.poke_go_friends.validation;

import com.devs.simplicity.poke_go_friends.entity.Team;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidTeamValidator implements ConstraintValidator<ValidTeam, Team> {
    @Override
    public boolean isValid(Team value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null is allowed (optional field)
        }
        return value == Team.MYSTIC || value == Team.VALOR || value == Team.INSTINCT;
    }
}
