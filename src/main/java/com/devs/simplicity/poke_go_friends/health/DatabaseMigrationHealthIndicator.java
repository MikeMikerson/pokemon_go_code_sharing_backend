package com.devs.simplicity.poke_go_friends.health;

import lombok.AllArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnBean(Flyway.class)
public class DatabaseMigrationHealthIndicator implements HealthIndicator {

    private final Flyway flyway;

    @Override
    public Health health() {
        try {
            MigrationInfo[] migrations = flyway.info().all();
            
            long successfulMigrations = 0;
            long failedMigrations = 0;
            
            for (MigrationInfo migration : migrations) {
                if (migration.getState() == MigrationState.SUCCESS) {
                    successfulMigrations++;
                } else if (migration.getState() == MigrationState.FAILED) {
                    failedMigrations++;
                }
            }
            
            Health.Builder healthBuilder = Health.up()
                    .withDetail("migrations", migrations.length)
                    .withDetail("successful", successfulMigrations);
            
            if (failedMigrations > 0) {
                healthBuilder = Health.down()
                        .withDetail("migrations", migrations.length)
                        .withDetail("successful", successfulMigrations)
                        .withDetail("failed", failedMigrations);
            }
            
            return healthBuilder.build();
            
        } catch (Exception e) {
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
