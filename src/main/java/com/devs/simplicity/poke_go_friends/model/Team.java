package com.devs.simplicity.poke_go_friends.model;

/**
 * Represents the three teams in Pokémon Go.
 */
public enum Team {
    INSTINCT("Team Instinct"),
    MYSTIC("Team Mystic"),
    VALOR("Team Valor");

    private final String displayName;

    Team(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
