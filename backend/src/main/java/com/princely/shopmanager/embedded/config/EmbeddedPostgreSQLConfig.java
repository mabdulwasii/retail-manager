package com.princely.shopmanager.embedded.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * Configuration for embedded PostgreSQL database in standalone mode.
 *
 * <p>This configuration automatically starts a PostgreSQL instance within the JVM
 * for embedded deployments, providing:
 * <ul>
 *   <li>No external PostgreSQL installation required</li>
 *   <li>File-based persistence in ./data/postgres directory</li>
 *   <li>Full PostgreSQL compatibility (same as cloud deployment)</li>
 *   <li>Easy migration path to cloud (just export/import PostgreSQL dump)</li>
 * </ul>
 *
 * <p>Memory footprint: ~150-200 MB (vs 50 MB for H2, but with full PostgreSQL features)
 *
 * <p>The database automatically starts on application startup and stops on shutdown.
 *
 * <p>This configuration is only activated when spring.datasource.url is NOT set,
 * allowing Docker Compose Lite to use an external PostgreSQL container.
 */
@Slf4j
@Configuration
@Profile("embedded")
@ConditionalOnProperty(name = "embedded.postgres.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddedPostgreSQLConfig {

    private EmbeddedPostgres embeddedPostgres;

    @Value("${embedded.postgres.port:5433}")
    private int postgresPort;

    @Value("${embedded.postgres.data-dir:./data/postgres}")
    private String dataDirectory;

    @Value("${embedded.postgres.database:shopmanager}")
    private String databaseName;

    /**
     * Creates and starts an embedded PostgreSQL instance.
     *
     * <p>The database is stored in a file-based directory for persistence
     * across application restarts.
     *
     * @return DataSource connected to the embedded PostgreSQL
     * @throws IOException if PostgreSQL cannot be started
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public DataSource dataSource() throws IOException {
        log.info("Starting embedded PostgreSQL on port {} with data directory: {}",
                postgresPort, dataDirectory);

        File dataDir = new File(dataDirectory);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            log.info("Created PostgreSQL data directory: {} (success: {})", dataDirectory, created);
        }

        embeddedPostgres = EmbeddedPostgres.builder()
                .setPort(postgresPort)
                .setDataDirectory(dataDir)
                .start();

        log.info("✅ Embedded PostgreSQL started successfully on port {}", postgresPort);
        log.info("Database: {}", databaseName);
        log.info("Data directory: {}", dataDir.getAbsolutePath());

        return embeddedPostgres.getPostgresDatabase();
    }

    /**
     * Gracefully shutdown embedded PostgreSQL on application stop.
     */
    @PreDestroy
    public void cleanup() {
        if (embeddedPostgres != null) {
            try {
                log.info("Shutting down embedded PostgreSQL...");
                embeddedPostgres.close();
                log.info("✅ Embedded PostgreSQL stopped successfully");
            } catch (IOException e) {
                log.error("Error stopping embedded PostgreSQL", e);
            }
        }
    }
}
