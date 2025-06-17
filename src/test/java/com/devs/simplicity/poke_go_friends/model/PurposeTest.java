package com.devs.simplicity.poke_go_friends.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Purpose enum.
 */
@DisplayName("Purpose Enum Tests")
class PurposeTest {

    @Test
    @DisplayName("Purpose enum should have correct values and display names")
    void purposeEnum_shouldHaveCorrectValuesAndDisplayNames() {
        // Given & When & Then
        assertThat(Purpose.GIFTS.getDisplayName()).isEqualTo("Gift Exchange");
        assertThat(Purpose.RAIDS.getDisplayName()).isEqualTo("Raid Battles");
        assertThat(Purpose.BOTH.getDisplayName()).isEqualTo("Gifts and Raids");
        
        assertThat(Purpose.values()).hasSize(3);
        assertThat(Purpose.valueOf("GIFTS")).isEqualTo(Purpose.GIFTS);
        assertThat(Purpose.valueOf("RAIDS")).isEqualTo(Purpose.RAIDS);
        assertThat(Purpose.valueOf("BOTH")).isEqualTo(Purpose.BOTH);
    }
}
