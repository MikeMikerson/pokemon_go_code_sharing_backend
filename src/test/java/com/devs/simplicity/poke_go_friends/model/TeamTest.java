package com.devs.simplicity.poke_go_friends.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Team enum.
 */
@DisplayName("Team Enum Tests")
class TeamTest {

    @Test
    @DisplayName("Team enum should have correct values and display names")
    void teamEnum_shouldHaveCorrectValuesAndDisplayNames() {
        // Given & When & Then
        assertThat(Team.INSTINCT.getDisplayName()).isEqualTo("Team Instinct");
        assertThat(Team.MYSTIC.getDisplayName()).isEqualTo("Team Mystic");
        assertThat(Team.VALOR.getDisplayName()).isEqualTo("Team Valor");
        
        assertThat(Team.values()).hasSize(3);
        assertThat(Team.valueOf("INSTINCT")).isEqualTo(Team.INSTINCT);
        assertThat(Team.valueOf("MYSTIC")).isEqualTo(Team.MYSTIC);
        assertThat(Team.valueOf("VALOR")).isEqualTo(Team.VALOR);
    }
}
