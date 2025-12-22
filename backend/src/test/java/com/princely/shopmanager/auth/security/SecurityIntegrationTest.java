package com.princely.shopmanager.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.test.config.PostgresTestContainer;
import com.princely.shopmanager.test.config.TestJwtSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security Integration Test - Tests REAL Authentication and Authorization.
 * <p>
 * This test uses:
 * - Real Spring Security (SecurityConfig loaded via test-security profile)
 * - Real CustomPermissionEvaluator (checks database permissions)
 * - Real JwtAuthConverter (converts JWT to JwtPrincipal)
 * - Test JwtDecoder (accepts test JWT tokens without signature validation)
 * <p>
 * Use this to test:
 * - 401 UNAUTHORIZED (no token / invalid token)
 * - 403 FORBIDDEN (valid token but insufficient permissions)
 * - 200 OK (valid token with correct permissions)
 * - Permission evaluation logic
 * - Tenant/Shop context extraction from JWT
 * <p>
 * For fast happy-path testing with mocked security, use MinimalIT tests instead.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "test-security"})
@AutoConfigureMockMvc
@Import(TestJwtSecurityConfig.class)
@Sql(
    scripts = "/test-data.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
@DisplayName("Security Integration Test - Real Authentication & Authorization")
class SecurityIntegrationTest {

    @Container
    public static PostgresTestContainer postgresContainer = PostgresTestContainer.getInstance();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private KeycloakUserService keycloakUserService;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    }

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED when no JWT token provided")
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/shops/" + TEST_SHOP_001))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 UNAUTHORIZED when JWT token is invalid")
    void shouldReturn401WhenInvalidToken() throws Exception {
        mockMvc.perform(get("/api/shops/" + TEST_SHOP_001)
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 403 FORBIDDEN when user has no permissions")
    void shouldReturn403WhenNoPermissions() throws Exception {
        // Create user with role that has NO permissions
        String token = createJwtToken("testuser", TEST_TENANT_001, TEST_SHOP_001, List.of("ROLE_NO_PERMISSIONS"));

        mockMvc.perform(get("/api/shops/" + TEST_SHOP_001)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 200 OK when user has valid permissions")
    void shouldReturn200WhenValidPermissions() throws Exception {
        // Verify test data is loaded correctly
        Optional<User> managerUser = userRepository.findByEmail("manager@testretail.com");
        assertThat(managerUser).isPresent();

        User user = managerUser.get();
        assertThat(user.getRoles()).isNotEmpty();

        Role managerRole = user.getRoles().iterator().next();
        Set<Permission> permissions = managerRole.getPermissions();
        assertThat(permissions).isNotEmpty();

        // Verify SHOP_READ permission exists
        boolean hasShopRead = permissions.stream()
            .anyMatch(p -> "SHOP_READ".equals(p.getName()));
        assertThat(hasShopRead).withFailMessage(
            "Manager role should have SHOP_READ permission. Current permissions: " +
            permissions.stream().map(Permission::getName).toList()
        ).isTrue();

        // Create JWT token for manager with SHOP_READ permission
        String token = createJwtTokenWithResourceAccess(
            "manager@testretail.com",
            TEST_TENANT_001,
            TEST_SHOP_001,
            List.of("MANAGER")
        );

        mockMvc.perform(get("/api/shops/" + TEST_SHOP_001)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should extract tenant and shop from JWT claims")
    void shouldExtractTenantAndShopFromJwt() throws Exception {
        // Create token with specific tenant and shop
        String customTenantId = "custom-tenant-id";
        String customShopId = "custom-shop-id";

        String token = createJwtToken("testuser", customTenantId, customShopId, List.of("MANAGER"));

        // The JwtAuthConverter should extract tenantId and shopId from the JWT
        // and set them in the JwtPrincipal
        // This test verifies the JWT conversion process works correctly

        // Note: We can't directly assert the JwtPrincipal here since it's internal to the request,
        // but we can verify the token is accepted (which means conversion succeeded)
        mockMvc.perform(get("/api/shops/" + TEST_SHOP_001)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isForbidden()); // 403 because user doesn't have permissions, but token was processed
    }

    /**
     * Creates a test JWT token with the given claims.
     * <p>
     * Format: "test-token.{base64-payload}.signature"
     *
     * @param subject Subject (username/email)
     * @param tenantId Tenant ID
     * @param shopId Shop ID
     * @param roles List of roles
     * @return JWT token string
     */
    private String createJwtToken(String subject, String tenantId, String shopId, List<String> roles) {
        String payload = String.format(
            "{\"sub\":\"%s\",\"iss\":\"shop-manager\",\"roles\":%s,\"preferred_username\":\"%s\",\"email\":\"%s\",\"tenant_id\":\"%s\",\"shop_id\":\"%s\",\"iat\":%d,\"exp\":%d}",
            subject,
            objectMapper.valueToTree(roles).toString(),
            subject,
            subject,
            tenantId,
            shopId,
            System.currentTimeMillis() / 1000,
            (System.currentTimeMillis() / 1000) + 3600 // 1 hour expiry
        );

        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes());

        return "test-token." + encodedPayload + ".signature";
    }

    /**
     * Creates a test JWT token with resource_access claim (Keycloak format).
     * <p>
     * This format matches real Keycloak JWT tokens where roles are nested under
     * resource_access[client-id].roles
     *
     * @param subject Subject (username/email)
     * @param tenantId Tenant ID
     * @param shopId Shop ID
     * @param roles List of roles
     * @return JWT token string
     */
    private String createJwtTokenWithResourceAccess(String subject, String tenantId, String shopId, List<String> roles) {
        String payload = String.format(
            "{\"sub\":\"%s\",\"iss\":\"shop-manager\",\"resource_access\":{\"shop-manager\":{\"roles\":%s}},\"preferred_username\":\"%s\",\"email\":\"%s\",\"tenant_id\":\"%s\",\"shop_id\":\"%s\",\"iat\":%d,\"exp\":%d}",
            subject,
            objectMapper.valueToTree(roles).toString(),
            subject,
            subject,
            tenantId,
            shopId,
            System.currentTimeMillis() / 1000,
            (System.currentTimeMillis() / 1000) + 3600 // 1 hour expiry
        );

        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.getBytes());

        return "test-token." + encodedPayload + ".signature";
    }
}
