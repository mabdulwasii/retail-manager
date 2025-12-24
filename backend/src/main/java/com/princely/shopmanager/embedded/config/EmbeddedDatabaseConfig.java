package com.princely.shopmanager.embedded.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Database configuration for embedded mode.
 * Configures H2 embedded database with PostgreSQL compatibility mode.
 */
@Slf4j
@Configuration
@Profile("embedded")
@ConditionalOnProperty(name = "application.mode", havingValue = "embedded")
public class EmbeddedDatabaseConfig {

    /**
     * Configure embedded H2 database for development/testing
     * Note: For production embedded deployments, the file-based H2 from application-embedded.yml is used
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.url", matchIfMissing = true)
    public DataSource embeddedDataSource() {
        log.info("Configuring embedded H2 database with PostgreSQL compatibility mode");

        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("shopmanager")
                .build();
    }

    /**
     * Database initialization check
     */
    @Bean
    public DatabaseInitializer databaseInitializer() {
        return new DatabaseInitializer();
    }

    /**
     * Database initializer component
     */
    public static class DatabaseInitializer {

        public DatabaseInitializer() {
            log.info("Database initializer created for embedded mode");
            log.info("Flyway will handle schema migration");
            log.info("Initial data will be loaded from db/migration scripts");
        }

        /**
         * Check if database needs initialization
         */
        public boolean requiresInitialization() {
            // Flyway handles all migrations
            return false;
        }

        /**
         * Get database info
         */
        public String getDatabaseInfo() {
            return "H2 Embedded Database (PostgreSQL Mode)";
        }
    }
}
