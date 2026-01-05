package com.princely.shopmanager.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.auth.service.UserManagementService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base class for integration tests using API key authentication (no Keycloak mocks).
 *
 * This class is designed for testing endpoints that use API key authentication instead of JWT/Keycloak.
 * Examples include the Cloud Aggregator API endpoints (/api/registration/*).
 *
 * Key differences from AbstractIntegrationTest:
 * - NO KeycloakUserService mock (avoiding Keycloak mock conflicts)
 * - Same TestContainers PostgreSQL setup
 * - Same test data loading mechanism
 * - Same database reset behavior
 *
 * Usage:
 * <pre>
 * class AggregatorControllerIT extends AbstractApiKeyIntegrationTest {
 *     {@literal @}Test
 *     void testPublicEndpoint() throws Exception {
 *         mockMvc.perform(post("/api/registration/tenants")
 *             .contentType(MediaType.APPLICATION_JSON)
 *             .content(jsonPayload))
 *             .andExpect(status().isCreated());
 *     }
 *
 *     {@literal @}Test
 *     void testApiKeyProtectedEndpoint() throws Exception {
 *         mockMvc.perform(post("/api/registration/shops")
 *             .header("X-API-Key", "rhq_test_key")
 *             .contentType(MediaType.APPLICATION_JSON)
 *             .content(jsonPayload))
 *             .andExpect(status().isCreated());
 *     }
 * }
 * </pre>
 */
@Slf4j
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@Sql(
    scripts = "/test-data.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
public abstract class AbstractApiKeyIntegrationTest {

    /**
     * PostgreSQL container shared across all integration tests.
     * Uses singleton pattern for container reuse and performance.
     */
    @Container
    public static final PostgresTestContainer postgresContainer = PostgresTestContainer.getInstance();

    /**
     * Random port for the embedded web server.
     */
    @LocalServerPort
    protected int port;

    /**
     * ObjectMapper for JSON serialization/deserialization in tests.
     */
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * MockMvc for testing Spring MVC controllers.
     * Automatically configured with Spring Security.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * EntityManager for persisting entities with manually-set IDs.
     */
    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * JdbcTemplate for raw SQL queries in diagnostic methods.
     */
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Mock KeycloakUserService to avoid requiring actual Keycloak connection.
     * API key endpoints don't use Keycloak, but some beans may have it as a dependency.
     */
    @MockBean
    protected KeycloakUserService keycloakUserService;

    /**
     * Mock UserManagementService to avoid Keycloak dependencies.
     * API key endpoints don't use this service.
     */
    @MockBean
    protected UserManagementService userManagementService;

    /**
     * Dynamically register TestContainer database properties.
     * This is called BEFORE Spring context initialization.
     */
    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        // Flyway configuration
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> true);
        registry.add("spring.flyway.clean-disabled", () -> false);

        // JPA configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.show-sql", () -> false);
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> true);
    }

    /**
     * Clear tenant context before each test.
     * Prevents test cross-contamination.
     */
    @BeforeEach
    void clearTenantContext() {
        TenantContext.clear();
        log.debug("Cleared tenant context for test");
    }

    /**
     * Set tenant context for test.
     * @param tenantId Tenant ID to set in context
     */
    protected void setTenantContext(String tenantId) {
        TenantContext.setCurrentTenant(tenantId);
        log.debug("Set tenant context to: {}", tenantId);
    }

    /**
     * Get base URL for REST API calls.
     * @return Base URL with port (e.g., http://localhost:8080)
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Get full URL for API endpoint.
     * @param path API path (e.g., /api/registration/health)
     * @return Full URL
     */
    protected String getUrl(String path) {
        return getBaseUrl() + path;
    }
}
