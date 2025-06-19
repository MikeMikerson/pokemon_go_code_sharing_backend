package com.devs.simplicity.poke_go_friends.health;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DatabaseMigrationHealthIndicatorTest {

    @Mock
    private Flyway flyway;

    @InjectMocks
    private DatabaseMigrationHealthIndicator healthIndicator;

    @Test
    void health_whenAllMigrationsSuccessful_shouldReturnUp() {
        MigrationInfoService migrationInfoService = mock(MigrationInfoService.class);
        MigrationInfo[] migrationInfos = new MigrationInfo[2];
        
        MigrationInfo migration1 = mock(MigrationInfo.class);
        MigrationInfo migration2 = mock(MigrationInfo.class);
        
        when(migration1.getState()).thenReturn(MigrationState.SUCCESS);
        when(migration2.getState()).thenReturn(MigrationState.SUCCESS);
        
        migrationInfos[0] = migration1;
        migrationInfos[1] = migration2;
        
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.all()).thenReturn(migrationInfos);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(2, health.getDetails().get("migrations"));
    }

    @Test
    void health_whenMigrationFailed_shouldReturnDown() {
        MigrationInfoService migrationInfoService = mock(MigrationInfoService.class);
        MigrationInfo[] migrationInfos = new MigrationInfo[2];
        
        MigrationInfo migration1 = mock(MigrationInfo.class);
        MigrationInfo migration2 = mock(MigrationInfo.class);
        
        when(migration1.getState()).thenReturn(MigrationState.SUCCESS);
        when(migration2.getState()).thenReturn(MigrationState.FAILED);
        
        migrationInfos[0] = migration1;
        migrationInfos[1] = migration2;
        
        when(flyway.info()).thenReturn(migrationInfoService);
        when(migrationInfoService.all()).thenReturn(migrationInfos);

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(2, health.getDetails().get("migrations"));
        assertEquals(1L, health.getDetails().get("failed"));
    }

    @Test
    void health_whenExceptionOccurs_shouldReturnDown() {
        when(flyway.info()).thenThrow(new RuntimeException("Database connection failed"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Database connection failed", health.getDetails().get("error"));
    }
}
