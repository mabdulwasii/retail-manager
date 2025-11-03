package com.princely.shopmanager.test.config;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests using TestContainers with PostgreSQL.
 *
 * This class provides:
 * - Real PostgreSQL database (same as production)
 * - Test data loaded from test-data.sql before each test
 * - Flyway migrations executed (V1-V14)
 * - Container reuse for fast test execution
 * - MockMvc autoconfigured for controller tests
 *
 * Usage:
 * <pre>
 * class MyControllerTest extends AbstractIntegrationTest {
 *     {@literal @}Autowired
 *     private MockMvc mockMvc;
 *
 *     {@literal @}Test
 *     {@literal @}WithMockPermissions(role = "MANAGER")
 *     void testEndpoint() throws Exception {
 *         // Test using real PostgreSQL and test-data.sql data
 *         mockMvc.perform(get("/api/products/" + TestConstants.PROD_WIRELESS_MOUSE))
 *             .andExpect(status().isOk());
 *     }
 * }
 * </pre>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate",
    "app.security.tenant-isolation=false" // Disable for testing
})
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL container shared across all integration tests.
     * Uses container reuse for fast test execution after first startup.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("shopmanager_test")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    /**
     * Configure Spring Boot to use the TestContainers PostgreSQL instance.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Flyway configuration
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }
}
