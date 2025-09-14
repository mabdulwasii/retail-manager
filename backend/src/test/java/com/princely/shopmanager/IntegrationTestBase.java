package com.princely.shopmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.repository.ShopRepository;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for comprehensive integration tests using TestContainers.
 *
 * This base class provides:
 * - Full Spring Boot application context
 * - Real database integration with PostgreSQL
 * - Kafka messaging integration
 * - Keycloak authentication integration
 * - Multi-tenant context management
 * - REST API testing infrastructure
 *
 * Integration tests validate:
 * - End-to-end API functionality
 * - Database persistence and transactions
 * - Security and authentication flows
 * - Multi-tenant isolation
 * - Business rule enforcement
 * - Event-driven messaging
 *
 * TestContainers automatically managed:
 * - PostgreSQL database instance
 * - Kafka message broker
 * - Keycloak authentication server
 * - Network configuration and cleanup
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
public abstract class IntegrationTestBase {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired(required = false)
    protected ShopRepository shopRepository;

    /**
     * Test token prefix for mock authentication.
     * Used to create consistent test tokens without external dependencies.
     */
    protected static final String TEST_TOKEN_PREFIX = "test-token";

    /**
     * PostgreSQL TestContainer for database integration.
     * Provides isolated database instance for each test run.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
        .withDatabaseName("shopdb_test")
        .withUsername("shop_test")
        .withPassword("shop_test")
        .withReuse(true);

    /**
     * Kafka TestContainer for messaging integration.
     * Provides isolated Kafka broker for event-driven testing.
     */
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
        .withReuse(true);

    /**
     * Keycloak TestContainer for authentication integration.
     * Provides isolated OAuth2/OpenID Connect server for security testing.
     * Uses a more stable version and increased startup timeout.
     */
    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:23.0.1")
        .withRealmImportFile("keycloak/realm-shop-manager-test.json")
        .withStartupTimeout(java.time.Duration.ofMinutes(3))
        .withReuse(false);

    /**
     * Dynamic configuration for TestContainer integration.
     * Configures Spring Boot to use TestContainer services.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // JPA and Flyway configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");
        registry.add("spring.flyway.clean-on-validation-error", () -> "true");

        // Kafka configuration
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "shop-manager-test");

        // Keycloak configuration
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
            () -> keycloak.getAuthServerUrl() + "/realms/shop-manager");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
            () -> keycloak.getAuthServerUrl() + "/realms/shop-manager/protocol/openid-connect/certs");
        registry.add("app.keycloak.auth-server-url", keycloak::getAuthServerUrl);
        registry.add("app.keycloak.realm", () -> "shop-manager");
        registry.add("app.keycloak.client-id", () -> "shop-manager-backend");

        // Test authentication configuration
        registry.add("app.test.auth.enabled", () -> "true");
        registry.add("app.test.auth.bypass-security", () -> "true");

        // Test-specific configurations
        registry.add("spring.profiles.active", () -> "test");
        registry.add("logging.level.com.princely.shopmanager", () -> "DEBUG");
        registry.add("logging.level.org.springframework.security", () -> "DEBUG");

        // Feature flags for testing
        registry.add("app.features.investment.enabled", () -> "true");
        registry.add("app.features.analytics.enabled", () -> "true");
        registry.add("app.features.fraud.enabled", () -> "true");

        // Disable security for some tests
        registry.add("app.swagger.security.enabled", () -> "false");
    }

    /**
     * Setup method executed before each test.
     * Initializes test environment and clears tenant context.
     */
    @BeforeEach
    void setUp() {
        // Clear tenant context before each test
        TenantContext.clear();

        // Clean up any leftover test data
        cleanupTestData();

        // Additional test setup can be added here
        setupTestData();
    }

    /**
     * Template method for test data setup.
     * Override in subclasses to provide specific test data.
     */
    protected void setupTestData() {
        // Override in subclasses for specific test data setup
    }

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

    /**
     * Helper method to set tenant context for multi-tenant testing.
     *
     * @param tenantId Tenant identifier to set in context
     */
    protected void setTenantContext(String tenantId) {
        TenantContext.setCurrentTenantId(tenantId);
    }

    /**
     * Helper method to clear tenant context.
     */
    protected void clearTenantContext() {
        TenantContext.clear();
    }

    /**
     * Helper method to create authentication headers for API calls.
     * Creates mock authentication headers for testing purposes.
     *
     * @param username Username for authentication
     * @param roles User roles
     * @return Authentication headers with mock token
     */
    protected org.springframework.http.HttpHeaders createAuthHeaders(String username, String... roles) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + createTestToken(username, roles));
        headers.set("X-Test-User", username);
        if (roles.length > 0) {
            headers.set("X-Test-Roles", String.join(",", roles));
        }
        return headers;
    }

    /**
     * Helper method to create test authentication tokens.
     * Creates a mock token with the specified username and roles for testing.
     *
     * @param username Username
     * @param roles User roles
     * @return Mock authentication token
     */
    private String createTestToken(String username, String... roles) {
        return createMockToken(username, "shop-manager", List.of(roles));
    }

    /**
     * Creates a mock token for testing with the given claims.
     * This creates a simple Base64-encoded token that can be used for testing
     * without requiring JWT dependencies.
     *
     * @param subject Token subject (username)
     * @param issuer Token issuer
     * @param roles List of user roles
     * @return Mock token string
     */
    protected String createMockToken(String subject, String issuer, List<String> roles) {
        // Create a simple JSON-like structure for the token payload
        String payload = String.format(
            "{\"sub\":\"%s\",\"iss\":\"%s\",\"roles\":%s,\"preferred_username\":\"%s\",\"email\":\"%s@test.com\",\"iat\":%d,\"exp\":%d}",
            subject,
            issuer,
            roles.toString(),
            subject,
            subject,
            System.currentTimeMillis() / 1000,
            (System.currentTimeMillis() / 1000) + 3600 // 1 hour expiry
        );

        // Encode as Base64 for a simple mock token format
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());

        return TEST_TOKEN_PREFIX + "." + encodedPayload + ".signature";
    }

    /**
     * Creates a mock token with tenant context.
     *
     * @param subject Token subject (username)
     * @param tenantId Tenant identifier
     * @param roles List of user roles
     * @return Mock token string
     */
    protected String createMockTokenWithTenant(String subject, String tenantId, List<String> roles) {
        // Create a simple JSON-like structure for the token payload with tenant
        String payload = String.format(
            "{\"sub\":\"%s\",\"iss\":\"shop-manager\",\"roles\":%s,\"preferred_username\":\"%s\",\"email\":\"%s@test.com\",\"tenant_id\":\"%s\",\"iat\":%d,\"exp\":%d}",
            subject,
            roles.toString(),
            subject,
            subject,
            tenantId,
            System.currentTimeMillis() / 1000,
            (System.currentTimeMillis() / 1000) + 3600 // 1 hour expiry
        );

        // Encode as Base64 for a simple mock token format
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());

        return TEST_TOKEN_PREFIX + "." + encodedPayload + ".signature";
    }

    /**
     * Helper method to create a simple authentication token for basic testing.
     * This method provides a fallback when more complex token generation is not needed.
     *
     * @param username Username
     * @param tenantId Tenant ID (optional)
     * @return Simple test token
     */
    protected String createSimpleTestToken(String username, String tenantId) {
        if (tenantId != null) {
            return createMockTokenWithTenant(username, tenantId, List.of("USER"));
        } else {
            return createMockToken(username, "shop-manager", List.of("USER"));
        }
    }

    /**
     * Helper method to wait for async operations to complete.
     * Useful for testing event-driven functionality.
     *
     * @param milliseconds Time to wait in milliseconds
     */
    protected void waitForAsync(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for async operation", e);
        }
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

    // ============================================
    // Test Data Factory Methods
    // ============================================

    /**
     * Creates a sample ShopCreateRequest for testing.
     *
     * @param name Shop name (will be made unique)
     * @return ShopCreateRequest with test data
     */
    protected ShopCreateRequest createSampleShopCreateRequest(String name) {
        return ShopCreateRequest.builder()
            .name(name + "-" + UUID.randomUUID().toString().substring(0, 8))
            .description("Test shop description for " + name)
            .address("123 Test Street")
            .city("Test City")
            .state("Test State")
            .country("Test Country")
            .postalCode("12345")
            .phoneNumber("+1-555-123-4567")
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
            .phoneNumber("+1-555-987-6543")
            .build();
    }

    /**
     * Creates and persists a test shop entity.
     *
     * @param name Shop name
     * @param tenantId Tenant ID
     * @return Created shop entity
     */
    protected Tenant createTenant(String tenantId) {
        return Tenant.builder()
            .id(tenantId)
            .name("Test Tenant " + tenantId)
            .contactEmail("test@" + tenantId.replace("-", "") + ".com")
            .status(Tenant.TenantStatus.ACTIVE)
            .build();
    }

    protected Shop createTestShop(String name, String tenantId) {
        if (shopRepository == null) {
            throw new IllegalStateException("ShopRepository not available - ensure proper test context");
        }

        Shop shop = Shop.builder()
            .id("shop-" + UUID.randomUUID().toString())
            .name(name + "-" + UUID.randomUUID().toString().substring(0, 8))
            .tenant(createTenant(tenantId))
            .description("Test shop for integration testing")
            .address("123 Test Street")
            .city("Test City")
            .state("Test State")
            .country("Test Country")
            .postalCode("12345")
            .phoneNumber("+1-555-123-4567")
            .email("test@example.com")
            .taxId("TAX" + System.currentTimeMillis())
            .status(Shop.ShopStatus.ACTIVE)
            .openingDate(LocalDateTime.now())
            .build();

        return shopRepository.save(shop);
    }

    // ============================================
    // Assertion Helper Methods
    // ============================================

    /**
     * Asserts that the response contains valid shop data.
     *
     * @param response Shop response to validate
     * @param expectedName Expected shop name (optional)
     */
    protected void assertValidShopResponse(ShopResponse response, String expectedName) {
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getTenantId()).isNotBlank();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        if (expectedName != null) {
            assertThat(response.getName()).contains(expectedName);
        }
    }

    /**
     * Asserts that the response contains a valid error message.
     *
     * @param response HTTP response entity
     * @param expectedStatus Expected HTTP status
     */
    protected void assertErrorResponse(ResponseEntity<?> response, HttpStatus expectedStatus) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        // Additional error response validation can be added here
    }

    /**
     * Asserts that a paginated response is valid.
     *
     * @param responseBody Response body as string
     * @param expectedSize Expected minimum number of elements
     */
    protected void assertValidPagedResponse(String responseBody, int expectedSize) {
        assertThat(responseBody).isNotBlank();
        assertThat(responseBody).contains("\"content\"");
        assertThat(responseBody).contains("\"totalElements\"");
        assertThat(responseBody).contains("\"totalPages\"");

        // Can add more specific assertions based on your Page structure
    }

    // ============================================
    // Database and Cleanup Utilities
    // ============================================

    /**
     * Cleans up test data from the database.
     * Override in subclasses for specific cleanup needs.
     */
    protected void cleanupTestData() {
        if (shopRepository != null) {
            // Clean up shops created during tests
            try {
                shopRepository.deleteAll(
                    shopRepository.findAll().stream()
                        .filter(shop -> shop.getName().contains("Test") || 
                               shop.getDescription().contains("Test"))
                        .toList()
                );
            } catch (Exception e) {
                // Log but don't fail tests on cleanup errors
                System.err.println("Warning: Error during test data cleanup: " + e.getMessage());
            }
        }
    }

    /**
     * Sets up test data with tenant isolation.
     *
     * @param tenantId Tenant ID to set up data for
     * @return Map of created test entities
     */
    protected Map<String, Object> setupTenantTestData(String tenantId) {
        setTenantContext(tenantId);

        // Create test data specific to the tenant
        Shop testShop = createTestShop("IntegrationTest", tenantId);

        return Map.of(
            "testShop", testShop,
            "tenantId", tenantId
        );
    }

    /**
     * Helper method to assert that an operation eventually succeeds.
     * Useful for testing eventual consistency scenarios.
     *
     * @param condition Condition to check
     * @param timeoutMs Maximum time to wait in milliseconds
     * @param intervalMs Interval between checks in milliseconds
     */
    protected void eventually(Runnable condition, long timeoutMs, long intervalMs) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                condition.run();
                return; // Success
            } catch (AssertionError | Exception e) {
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting", ie);
                }
            }
        }
        // Final attempt
        condition.run();
    }
}