package com.devs.simplicity.poke_go_friends.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing Pokemon Go teams.
 * Based on the three teams available in Pokemon Go.
 */
public enum Team {
    MYSTIC("mystic"),
    VALOR("valor"),
    INSTINCT("instinct");

    private final String value;

    Team(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert string value to Team enum.
     * Used for deserialization from JSON.
     *
     * @param value The string value
     * @return The corresponding Team enum
     * @throws IllegalArgumentException if value is not valid
     */
    @JsonCreator
    public static Team fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (Team team : Team.values()) {
            if (team.value.equalsIgnoreCase(value)) {
                return team;
            }
        }
        
        throw new IllegalArgumentException("Invalid team value: " + value + 
            ". Valid values are: mystic, valor, instinct");
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
