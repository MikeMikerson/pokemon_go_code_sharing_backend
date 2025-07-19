package com.devs.simplicity.poke_go_friends.config;

import com.devs.simplicity.poke_go_friends.entity.Team;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Custom converter for Team enum from URL parameters.
 * Handles case-insensitive conversion of team names.
 */
@Component
public class TeamConverter implements Converter<String, Team> {
    
    @Override
    public Team convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Team.fromValue(source);
        } catch (IllegalArgumentException e) {
            // Re-throw with a more specific message for the parameter handler
            throw new IllegalArgumentException(String.format(
                "Invalid value '%s' for parameter 'team'. Valid values are: mystic, valor, instinct", 
                source));
        }
    }
}
