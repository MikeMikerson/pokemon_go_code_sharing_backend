package com.devs.simplicity.poke_go_friends.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test JSON deserialization of Team enum with Jackson annotations.
 */
class TeamDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeLowercaseTeamValues() throws JsonProcessingException {
        // Test lowercase values
        assertEquals(Team.VALOR, objectMapper.readValue("\"valor\"", Team.class));
        assertEquals(Team.MYSTIC, objectMapper.readValue("\"mystic\"", Team.class));
        assertEquals(Team.INSTINCT, objectMapper.readValue("\"instinct\"", Team.class));
    }

    @Test
    void shouldDeserializeUppercaseTeamValues() throws JsonProcessingException {
        // Test uppercase values
        assertEquals(Team.VALOR, objectMapper.readValue("\"VALOR\"", Team.class));
        assertEquals(Team.MYSTIC, objectMapper.readValue("\"MYSTIC\"", Team.class));
        assertEquals(Team.INSTINCT, objectMapper.readValue("\"INSTINCT\"", Team.class));
    }

    @Test
    void shouldDeserializeMixedCaseTeamValues() throws JsonProcessingException {
        // Test mixed case values
        assertEquals(Team.VALOR, objectMapper.readValue("\"VaLoR\"", Team.class));
        assertEquals(Team.MYSTIC, objectMapper.readValue("\"MyStIc\"", Team.class));
        assertEquals(Team.INSTINCT, objectMapper.readValue("\"InStInCt\"", Team.class));
    }

    @Test
    void shouldThrowExceptionForInvalidTeamValue() {
        Exception exception = assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readValue("\"invalid_team\"", Team.class);
        });
        
        assertTrue(exception.getMessage().contains("Invalid team value: invalid_team"));
    }

    @Test
    void shouldSerializeToLowercaseValues() throws JsonProcessingException {
        // Test serialization uses @JsonValue
        assertEquals("\"valor\"", objectMapper.writeValueAsString(Team.VALOR));
        assertEquals("\"mystic\"", objectMapper.writeValueAsString(Team.MYSTIC));
        assertEquals("\"instinct\"", objectMapper.writeValueAsString(Team.INSTINCT));
    }
}
