package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.test.config.PostgresTestContainer;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.features.analytics.enabled=false",
    "app.features.investment.enabled=false",
    "app.features.fraud.enabled=false",
    "spring.modulith.events.externalization.enabled=false"
})
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Container
    static PostgresTestContainer postgres = PostgresTestContainer.getInstance();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakUserService keycloakUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private User testUser;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = Tenant.builder()
            .name("Test Company")
            .contactEmail("admin@testcompany.com")
            .primaryAddress("123 Test St")
            .status(Tenant.TenantStatus.ACTIVE)
            .build();
        testTenant = tenantRepository.saveAndFlush(testTenant);

        testUser = User.builder()
            .keycloakId("keycloak-456")
            .username("john.doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .tenant(testTenant)
            .status(User.UserStatus.ACTIVE)
            .build();
    }

    @Test
    @DisplayName("Should return user profile when user exists in database")
    @WithMockPermissions(role = "MANAGER", username = "john.doe", tenantId = "tenant-123", shopId = "shop-456")
    void shouldReturnUserProfileWhenUserExists() throws Exception {
        // Given - Save user to database with keycloak ID matching mock user
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000"); // Match TestConstants.MOCK_USER_ID
        User savedUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(savedUser.getId()))
            .andExpect(jsonPath("$.username").value("john.doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.fullName").value("John Doe"))
            .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.tenantId").value("tenant-123"))
            .andExpect(jsonPath("$.shopId").value("shop-456"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Should return JWT-based profile when user not found in database")
    @WithMockPermissions(role = "INVESTOR", username = "jane.doe", tenantId = "tenant-789", shopId = "shop-999")
    void shouldReturnJwtProfileWhenUserNotInDatabase() throws Exception {
        // Given - No user in database (repository is cleared in @BeforeEach)

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("750e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.username").value("jane.doe"))
            .andExpect(jsonPath("$.email").value("investor@testretail.com"))  // Email comes from role mapping
            .andExpect(jsonPath("$.firstName").value("jane.doe"))
            .andExpect(jsonPath("$.lastName").value("User"))
            .andExpect(jsonPath("$.fullName").value("jane.doe User"))
            .andExpect(jsonPath("$.tenantId").value("tenant-789"))
            // shopId comes from JwtPrincipal.getClaimAsString("shop_id") which doesn't work with UsernamePasswordAuthenticationToken
            // It only works with real JWT, so we use the shopId from the JwtPrincipal directly
            .andExpect(jsonPath("$.shopId").value("shop-999"))
            .andExpect(jsonPath("$.phoneNumber").doesNotExist())
            .andExpect(jsonPath("$.createdAt").doesNotExist());
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow access to any authenticated user regardless of role")
    @WithMockPermissions(username = "john.doe", tenantId = "tenant-123")
    void shouldAllowAnyAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("john.doe"));
    }

    @Test
    @DisplayName("Should accept OWNER role")
    @WithMockPermissions(role = "OWNER", username = "admin.user")
    void shouldAcceptTenantAdminRole() throws Exception {
        // Given - Save user to database
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000");
        User savedUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @DisplayName("Should accept INVESTOR role")
    @WithMockPermissions(role = "INVESTOR", username = "investor.user")
    void shouldAcceptInvestorRole() throws Exception {
        // Given - Create and save investor user
        User investorUser = User.builder()
            .keycloakId("750e8400-e29b-41d4-a716-446655440000")
            .username("investor.user")
            .email("investor@example.com")
            .firstName("Investor")
            .lastName("User")
            .phoneNumber("+1987654321")
            .tenant(testTenant)
            .status(User.UserStatus.ACTIVE)
            .build();

        User savedInvestor = userRepository.saveAndFlush(investorUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedInvestor.getId()));
    }

    @Test
    @DisplayName("Should accept EMPLOYEE role")
    @WithMockPermissions(role = "EMPLOYEE", username = "employee.user")
    void shouldAcceptShopEmployeeRole() throws Exception {
        // Given - Save user to database
        testUser.setKeycloakId("750e8400-e29b-41d4-a716-446655440000");
        User savedUser = userRepository.saveAndFlush(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @DisplayName("Should handle missing JWT claims gracefully")
    @WithMockPermissions(role = "MANAGER", username = "minimal.user")
    void shouldHandleMissingJwtClaims() throws Exception {
        // Given - No user in database for this Keycloak ID

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("750e8400-e29b-41d4-a716-446655440000"))
            .andExpect(jsonPath("$.username").value("minimal.user"))
            .andExpect(jsonPath("$.email").value("manager@testretail.com"))  // Email comes from role mapping
            .andExpect(jsonPath("$.firstName").value("minimal.user"))
            .andExpect(jsonPath("$.lastName").value("User"));
    }
}