package com.princely.shopmanager.test.config;

import org.testcontainers.containers.PostgreSQLContainer;

import static java.util.Objects.isNull;

/**
 * Singleton PostgreSQL TestContainer for integration tests.
 * This container is shared across all integration tests to improve performance.
 * The container starts once and is reused, with the database being cleaned/reset
 * by Flyway and @Sql annotations between tests.
 * Database properties are registered dynamically via @DynamicPropertySource
 * in AbstractIntegrationTest.
 * Pattern inspired by: Axual mgmt-api PostgresTestContainer
 */
public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";
    private static PostgresTestContainer container;

    private PostgresTestContainer() {
        super(POSTGRES_IMAGE);
        withDatabaseName("shopmanager_test");
        withUsername("test");
        withPassword("test");
        withReuse(true);  // Enable container reuse for faster test execution
    }

    public static PostgresTestContainer getInstance() {
        if (isNull(container)) {
            container = new PostgresTestContainer();
        }
        return container;
    }

    @Override
    public void stop() {
        // Do nothing - JVM handles shutdown
        // Container reuse across test classes for performance
    }
}
