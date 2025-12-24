package com.princely.shopmanager.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.test.TestConstants;

import org.junit.jupiter.api.BeforeEach;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Unified abstract base class for integration tests using TestContainers with PostgreSQL.
 *
 * This class provides:
 * - Real PostgreSQL database via TestContainers (same as production)
 * - Test data loaded from test-data.sql before each test (@Sql with ISOLATED mode)
 * - Flyway migrations executed automatically
 * - Container reuse for fast test execution
 * - Both MockMvc AND TestRestTemplate for flexible testing
 * - Dynamic property source for TestContainer configuration
 * - Comprehensive utility methods for common test operations
 *
 * Key Design Decisions:
 * - PostgreSQL container is singleton and shared across all test classes
 * - Database is reset via @Sql(test-data.sql) before each test method (ISOLATED transaction mode)
 * - No manual cleanup needed - @Sql re-runs automatically, resetting database state
 * - Active profile "test" loads application-test.yml configuration
 * - @DynamicPropertySource registers TestContainer properties before Spring context loads
 *
 * Usage:
 * <pre>
 * class TenantAdminControllerIT extends AbstractIntegrationTest {
 *     {@literal @}Test
 *     {@literal @}WithMockPermissions(role = "SYSTEM_ADMIN")
 *     void testEndpointWithMockMvc() throws Exception {
 *         mockMvc.perform(get("/api/admin/tenants/" + TestConstants.TEST_TENANT_001))
 *             .andExpect(status().isOk());
 *     }
 *
 *     {@literal @}Test
 *     void testEndpointWithRestTemplate() {
 *         ResponseEntity<String> response = performAuthenticatedGet(
 *             "/tenants/test-tenant-001", "admin", String.class, "SYSTEM_ADMIN"
 *         );
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
public abstract class AbstractIntegrationTest {

    public static final java.lang.String POSTAL_CODE = "12345";
    public static final String TEST_MAIL = "test@example.com";
    public static final String TEST_COUNTRY = "Test Country";
    public static final String TEST_STATE = "Test State";
    public static final String PHONE_NUMBER = "+15551234567";
    public static final String TEST_STREET = "123 Test Street";
    public static final String TEST_CITY = "Test City";
    /**
     * PostgreSQL container shared across all integration tests.
     * Uses singleton pattern for container reuse and performance.
     */
    @Container
    public static final PostgresTestContainer postgresContainer = PostgresTestContainer.getInstance();

    /**
     * Random port for the embedded web server.
     * Used for constructing REST API URLs in TestRestTemplate tests.
     */
    @LocalServerPort
    protected int port;

    /**
     * TestRestTemplate for REST API testing with real HTTP calls.
     * Use this for end-to-end REST testing scenarios.
     */
    @Autowired
    protected TestRestTemplate restTemplate;

    /**
     * ObjectMapper for JSON serialization/deserialization in tests.
     */
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * MockMvc for testing Spring MVC controllers.
     * Automatically configured with Spring Security.
     * Use this for controller layer testing with mocked HTTP.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * ShopRepository for creating test shop data.
     * May be null if not available in test context.
     */
    @Autowired(required = false)
    protected ShopRepository shopRepository;

    /**
     * TenantRepository for creating test tenant data.
     * May be null if not available in test context.
     */
    @Autowired(required = false)
    protected TenantRepository tenantRepository;

    /**
     * EntityManager for persisting entities with manually-set IDs.
     * Used in test data creation.
     */
    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * JdbcTemplate for raw SQL queries in diagnostic methods.
     */
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Mock KeycloakUserService to avoid requiring actual Keycloak connection in tests.
     * Configured with default stub behavior for common operations.
     */
    @MockBean
    protected KeycloakUserService keycloakUserService;

    /**
     * Test token prefix for mock authentication.
     * Used to create consistent test tokens without external dependencies.
     */
    protected static final String TEST_TOKEN_PREFIX = "test-token";

    /**
     * Dynamically register TestContainer database properties and feature flags.
     * This is called BEFORE Spring context initialization, ensuring proper DataSource configuration.
     */
    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        // Flyway 10.x compatible (NO deprecated properties)
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.validate-on-migrate", () -> "false");
        registry.add("spring.flyway.clean-disabled", () -> "false");

        // Feature flags
        registry.add("app.features.investment.enabled", () -> "true");
        registry.add("app.features.analytics.enabled", () -> "true");
        registry.add("app.features.fraud.enabled", () -> "true");

        // Logging
        registry.add("logging.level.com.princely.shopmanager", () -> "DEBUG");
        registry.add("logging.level.org.springframework.security", () -> "INFO");
    }

    /**
     * Setup method executed before each test.
     * Initializes test environment, clears tenant context, and configures Keycloak mocks.
     */
    @BeforeEach
    void setupKeycloakMocks() {
        // Clear tenant context before each test
        TenantContext.clear();

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
            user.setEmail(TEST_MAIL);
            user.setEnabled(true);
            return Optional.of(user);
        });
    }

    // ============================================
    // URL Helper Methods
    // ============================================

    /**
     * Helper method to get the base URL for REST API calls.
     *
     * @return Base URL for the running application
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Helper method to get the full URL for API endpoints.
     *
     * @param endpoint API endpoint path
     * @return Full URL for the endpoint
     */
    protected String getApiUrl(String endpoint) {
        return getBaseUrl() + "/api" + endpoint;
    }

    // ============================================
    // Tenant Context Methods
    // ============================================

    /**
     * Helper method to set tenant context for multi-tenant testing.
     *
     * @param tenantId Tenant identifier to set in context
     */
    protected void setTenantContext(String tenantId) {
        TenantContext.setCurrentTenantId(tenantId);
    }

    // ============================================
    // Authentication Header Methods
    // ============================================

    /**
     * Helper method to create authentication headers for API calls.
     * Creates mock authentication headers for testing purposes.
     *
     * @param username Username for authentication
     * @param roles User roles
     * @return Authentication headers with mock token
     */
    protected HttpHeaders createAuthHeaders(String username, String... roles) {
        // Use current tenant from TenantContext if available, otherwise fall back to test defaults
        String tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = TestConstants.TEST_TENANT_001;
        }
        // Use TEST_SHOP_001 as default shop for backward compatibility
        return createAuthHeadersWithContext(username, tenantId, TestConstants.TEST_SHOP_001, roles);
    }

    /**
     * Helper method to create authentication headers with shop context.
     *
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param roles User roles
     * @return Authentication headers with mock token
     */
    protected HttpHeaders createAuthHeaders(String username, String shopId, String... roles) {
        // Use current tenant from TenantContext
        String tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "test-tenant";
        }
        return createAuthHeadersWithContext(username, tenantId, shopId, roles);
    }

    /**
     * Helper method to create authentication headers with tenant and shop context.
     *
     * @param username Username for authentication
     * @param tenantId Tenant identifier
     * @param shopId Shop identifier
     * @param roles User roles
     * @return Authentication headers with mock token including tenant and shop
     */
    protected HttpHeaders createAuthHeadersWithContext(String username, String tenantId, String shopId, String... roles) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + createMockTokenWithTenantAndShop(username, tenantId, shopId, List.of(roles)));
        headers.set("X-Test-User", username);
        headers.set("X-Test-User-Id", getUserIdForEmail(username));  // Database user UUID for principal
        headers.set("X-Test-Keycloak-Id", getKeycloakIdForEmail(username));  // Keycloak ID for TenantContext
        headers.set("X-Test-Tenant", tenantId);
        if (shopId != null) {
            headers.set("X-Test-Shop", shopId);
        }
        if (roles.length > 0) {
            headers.set("X-Test-Roles", String.join(",", roles));
        }
        return headers;
    }

    // ============================================
    // Mock Token Methods
    // ============================================

    /**
     * Maps common test user emails to their Keycloak IDs from test-data.sql.
     * Used to set the X-Test-Keycloak-Id header for TenantContext.
     *
     * @param email User email
     * @return Keycloak ID
     */
    private String getKeycloakIdForEmail(String email) {
        return switch (email) {
            case "admin@testretail.com" -> "kc-admin-001";
            case "owner@testretail.com" -> "kc-owner-001";
            case "manager@testretail.com" -> "kc-manager-001";
            case "employee@testretail.com" -> "kc-employee-001";
            case "investor@testretail.com" -> "kc-investor-001";
            default -> email; // Fallback to email for unknown users
        };
    }

    /**
     * Maps common test user emails to their database UUIDs from test-data.sql.
     * Used to set the X-Test-User-Id header for proper authentication in tests.
     *
     * @param email User email
     * @return User UUID
     */
    private String getUserIdForEmail(String email) {
        // Map test users to their IDs from test-data.sql
        return switch (email) {
            case "admin@testretail.com" -> "750e8400-e29b-41d4-a716-446655440001";
            case "owner@testretail.com" -> "750e8400-e29b-41d4-a716-446655440002";
            case "manager@testretail.com" -> "750e8400-e29b-41d4-a716-446655440003";
            case "employee@testretail.com" -> "750e8400-e29b-41d4-a716-446655440004";
            case "investor@testretail.com" -> "750e8400-e29b-41d4-a716-446655440005";
            default -> email; // Fallback to email for unknown users
        };
    }

    /**
     * Creates a mock token with tenant and shop context.
     *
     * @param subject Token subject (username)
     * @param tenantId Tenant identifier
     * @param shopId Shop identifier (optional)
     * @param roles List of user roles
     * @return Mock token string
     */
    protected String createMockTokenWithTenantAndShop(String subject, String tenantId, String shopId, List<String> roles) {
        // Create a simple JSON-like structure for the token payload with tenant and shop
        String payload;
        if (shopId != null) {
            payload = String.format(
                "{\"sub\":\"%s\",\"iss\":\"shop-manager\",\"roles\":%s,\"preferred_username\":\"%s\",\"email\":\"%s\",\"tenant_id\":\"%s\",\"shop_id\":\"%s\",\"iat\":%d,\"exp\":%d}",
                subject,
                roles.toString(),
                subject,
                subject,
                tenantId,
                shopId,
                System.currentTimeMillis() / 1000,
                (System.currentTimeMillis() / 1000) + 3600 // 1 hour expiry
            );
        } else {
            payload = String.format(
                "{\"sub\":\"%s\",\"iss\":\"shop-manager\",\"roles\":%s,\"preferred_username\":\"%s\",\"email\":\"%s\",\"tenant_id\":\"%s\",\"iat\":%d,\"exp\":%d}",
                subject,
                roles.toString(),
                subject,
                subject,
                tenantId,
                System.currentTimeMillis() / 1000,
                (System.currentTimeMillis() / 1000) + 3600 // 1 hour expiry
            );
        }

        // Encode as Base64 for a simple mock token format
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());

        return TEST_TOKEN_PREFIX + "." + encodedPayload + ".signature";
    }

    // ============================================
    // HTTP Testing Utility Methods
    // ============================================

    /**
     * Performs a POST request with authentication.
     *
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param username Username for authentication
     * @param roles User roles
     * @param responseType Expected response type
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedPost(
        String endpoint, Object body, String username, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, roles);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.POST,
            entity,
            responseType
        );
    }

    /**
     * Performs a POST request with authentication and shop context.
     *
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param responseType Expected response type
     * @param roles User roles
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedPostWithShop(
        String endpoint, Object body, String username, String shopId, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, shopId, roles);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.POST,
            entity,
            responseType
        );
    }

    /**
     * Performs a GET request with authentication.
     *
     * @param endpoint API endpoint path
     * @param username Username for authentication
     * @param roles User roles
     * @param responseType Expected response type
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedGet(
        String endpoint, String username, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, roles);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.GET,
            entity,
            responseType
        );
    }

    /**
     * Performs a GET request with authentication and shop context.
     *
     * @param endpoint API endpoint path
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param responseType Expected response type
     * @param roles User roles
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedGetWithShop(
        String endpoint, String username, String shopId, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, shopId, roles);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.GET,
            entity,
            responseType
        );
    }

    /**
     * Performs a GET request with authentication and pagination.
     *
     * @param endpoint API endpoint path
     * @param page Page number
     * @param size Page size
     * @param username Username for authentication
     * @param roles User roles
     * @return Response entity with paginated results
     */
    protected ResponseEntity<String> performAuthenticatedGetWithPagination(
        String endpoint, int page, int size, String username, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, roles);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromUriString(getApiUrl(endpoint))
            .queryParam("page", page)
            .queryParam("size", size)
            .build()
            .toUriString();

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    /**
     * Performs a GET request with authentication, shop context, and pagination.
     *
     * @param endpoint API endpoint path
     * @param page Page number
     * @param size Page size
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param roles User roles
     * @return Response entity with paginated results
     */
    protected ResponseEntity<String> performAuthenticatedGetWithPaginationAndShop(
        String endpoint, int page, int size, String username, String shopId, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, shopId, roles);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromUriString(getApiUrl(endpoint))
            .queryParam("page", page)
            .queryParam("size", size)
            .build()
            .toUriString();

        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    /**
     * Performs a PUT request with authentication.
     *
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param username Username for authentication
     * @param roles User roles
     * @param responseType Expected response type
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedPut(
        String endpoint, Object body, String username, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, roles);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.PUT,
            entity,
            responseType
        );
    }

    /**
     * Performs a PUT request with authentication and shop context.
     *
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param responseType Expected response type
     * @param roles User roles
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedPutWithShop(
        String endpoint, Object body, String username, String shopId, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, shopId, roles);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.PUT,
            entity,
            responseType
        );
    }

    /**
     * Performs a PATCH request with authentication.
     *
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param username Username for authentication
     * @param roles User roles
     * @param responseType Expected response type
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedPatch(
        String endpoint, Object body, String username, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, roles);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.PATCH,
            entity,
            responseType
        );
    }

    /**
     * Performs a PATCH request with authentication and shop context.
     *
     * @param endpoint API endpoint path
     * @param body Request body object
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param responseType Expected response type
     * @param roles User roles
     * @return Response entity
     */
    protected <T> ResponseEntity<T> performAuthenticatedPatchWithShop(
        String endpoint, Object body, String username, String shopId, Class<T> responseType, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, shopId, roles);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.PATCH,
            entity,
            responseType
        );
    }

    /**
     * Performs a DELETE request with authentication.
     *
     * @param endpoint API endpoint path
     * @param username Username for authentication
     * @param roles User roles
     * @return Response entity
     */
    protected ResponseEntity<Void> performAuthenticatedDelete(
        String endpoint, String username, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, roles);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.DELETE,
            entity,
            Void.class
        );
    }

    /**
     * Performs a DELETE request with authentication and shop context.
     *
     * @param endpoint API endpoint path
     * @param username Username for authentication
     * @param shopId Shop identifier
     * @param roles User roles
     * @return Response entity
     */
    protected ResponseEntity<Void> performAuthenticatedDeleteWithShop(
        String endpoint, String username, String shopId, String... roles) {

        HttpHeaders headers = createAuthHeaders(username, shopId, roles);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
            getApiUrl(endpoint),
            HttpMethod.DELETE,
            entity,
            Void.class
        );
    }

    // ============================================
    // Shop Test Data Helpers
    // ============================================

    /**
     * Creates a sample ShopCreateRequest for testing.
     *
     * @param name Shop name
     * @return ShopCreateRequest with test data
     */
    protected ShopCreateRequest createSampleShopCreateRequest(String name) {
        return ShopCreateRequest.builder()
            .name(name + "-" + UUID.randomUUID().toString().substring(0, 8))
            .description("Test shop description for " + name)
            .address(TEST_STREET)
            .city(TEST_CITY)
            .state(TEST_STATE)
            .country(TEST_COUNTRY)
            .postalCode(POSTAL_CODE)
            .phoneNumber(PHONE_NUMBER)
            .email("test-" + name.toLowerCase().replaceAll("[^a-z0-9]", "") + "@example.com")
            .taxId("TAX" + System.currentTimeMillis())
            .openingDate(LocalDateTime.now().plusDays(1))
            .build();
    }

    /**
     * Creates a sample ShopUpdateRequest for testing.
     *
     * @param description New description
     * @return ShopUpdateRequest with test data
     */
    protected ShopUpdateRequest createSampleShopUpdateRequest(String description) {
        return ShopUpdateRequest.builder()
            .description(description)
            .city("Updated City")
            .phoneNumber("+15559876543")
            .build();
    }
}
