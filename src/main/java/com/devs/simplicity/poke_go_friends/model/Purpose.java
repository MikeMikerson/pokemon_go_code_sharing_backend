package com.devs.simplicity.poke_go_friends.model;

/**
 * Represents the purpose for adding friends in Pokémon Go.
 */
public enum Purpose {
    GIFTS("Gift Exchange"),
    RAIDS("Raid Battles"),
    BOTH("Gifts and Raids");

    private final String displayName;

    Purpose(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
