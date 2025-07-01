package com.devs.simplicity.poke_go_friends.entity;

/**
 * Enum representing Pokemon Go friendship goals.
 * Based on the activities players want to participate in.
 */
public enum Goal {
    GIFTS("gifts"),
    EXP("exp"),
    RAIDS("raids"),
    ALL("all");

    private final String value;

    Goal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Convert string value to Goal enum.
     * Used for deserialization from JSON.
     *
     * @param value The string value
     * @return The corresponding Goal enum
     * @throws IllegalArgumentException if value is not valid
     */
    public static Goal fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (Goal goal : Goal.values()) {
            if (goal.value.equalsIgnoreCase(value)) {
                return goal;
            }
        }
        
        throw new IllegalArgumentException("Invalid goal value: " + value + 
            ". Valid values are: gifts, exp, raids, all");
    }

    @Override
    public String toString() {
        return value;
    }
}
