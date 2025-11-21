package com.princely.shopmanager.test.config;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.KeycloakUserService;

import org.junit.jupiter.api.BeforeEach;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Abstract base class for integration tests using TestContainers with PostgreSQL.
 * This class provides:
 * - Real PostgreSQL database via TestContainers (same as production)
 * - Test data loaded from test-data.sql before each test
 * - Flyway migrations executed automatically
 * - Container reuse for fast test execution
 * - MockMvc autoconfigured for controller tests
 * - Dynamic property source for TestContainer configuration
 * Key Design Decisions:
 * - PostgreSQL container is singleton and shared across all test classes
 * - Database is reset via Flyway + @Sql before each test method
 * - Active profile "test" loads application-test.yml configuration
 * - @DynamicPropertySource registers TestContainer properties before Spring context loads
 * Usage:
 * <pre>
 * class TenantAdminControllerIT extends AbstractIntegrationTest {
 *     {@literal @}Autowired
 *     private MockMvc mockMvc;
 *
 *     {@literal @}Test
 *     {@literal @}WithMockPermissions(role = "SUPER_ADMIN")
 *     void testEndpoint() throws Exception {
 *         // Test using real PostgreSQL and test-data.sql data
 *         mockMvc.perform(get("/api/admin/tenants/" + TestConstants.TEST_TENANT_001))
 *             .andExpect(status().isOk())
 *             .andExpect(jsonPath("$.tenantName").value(TestConstants.TENANT_NAME_TEST_RETAIL));
 *     }
 * }
 * </pre>
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL container shared across all integration tests.
     * Uses singleton pattern for container reuse and performance.
     */
    @Container
    public static PostgresTestContainer postgresContainer = PostgresTestContainer.getInstance();

    /**
     * Dynamically register TestContainer database properties.
     * This is called BEFORE Spring context initialization, ensuring proper DataSource configuration.
     */
    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    }

    /**
     * MockMvc for testing Spring MVC controllers.
     * Automatically configured with Spring Security.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * Mock KeycloakUserService to avoid requiring actual Keycloak connection in tests.
     * Configured with default stub behavior for common operations.
     */
    @MockBean
    protected KeycloakUserService keycloakUserService;

    /**
     * Setup default mock behavior for KeycloakUserService before each test.
     * Tests can override this behavior using Mockito.when() in their setup methods.
     */
    @BeforeEach
    void setupKeycloakMocks() {
        // Mock user creation - return random UUID
        when(keycloakUserService.createUser(any(CreateKeycloakUserRequest.class)))
                .thenAnswer(invocation -> UUID.randomUUID().toString());

        // Mock user existence checks - return false to allow user creation
        when(keycloakUserService.userExistsByEmail(anyString())).thenReturn(false);
        when(keycloakUserService.userExistsByUsername(anyString())).thenReturn(false);

        // Mock password generation
        when(keycloakUserService.generatePassword()).thenReturn("TestPassword123!");

        // Mock user status updates - do nothing
        doNothing().when(keycloakUserService).updateUserStatus(anyString(), anyBoolean());

        // Mock role assignment - do nothing
        doNothing().when(keycloakUserService).assignRolesToUser(anyString(), anyList());

        // Mock get user by ID
        when(keycloakUserService.getUserById(anyString())).thenAnswer(invocation -> {
            String userId = invocation.getArgument(0);
            UserRepresentation user = new UserRepresentation();
            user.setId(userId);
            user.setUsername("test-user");
            user.setEmail("test@example.com");
            user.setEnabled(true);
            return Optional.of(user);
        });
    }
}
