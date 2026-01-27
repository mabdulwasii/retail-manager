package com.princely.shopmanager.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Custom Flyway configuration to avoid circular dependency.
 * Runs migrations early in the bean lifecycle using @PostConstruct with highest priority.
 */
@Slf4j
@Configuration
@Order(Integer.MIN_VALUE)
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void runMigrations() {
        log.info("🗃️  Starting Flyway migrations via @PostConstruct...");

        try {
            log.info("📊 DataSource available, configuring Flyway...");

            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .cleanDisabled(true)
                    .load();

            int migrationsApplied = flyway.migrate().migrationsExecuted;
            log.info("✅ Flyway migrations completed via @PostConstruct. Applied {} migrations.", migrationsApplied);

        } catch (Exception e) {
            log.error("❌ Flyway migration failed in @PostConstruct: {}", e.getMessage(), e);
            throw new RuntimeException("Database migration failed during @PostConstruct", e);
        }
    }
}
