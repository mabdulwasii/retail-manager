package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
            .keycloakId("keycloak-456")
            .username("john.doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .status(User.UserStatus.ACTIVE)
            .isInvestor(false)
            .build();
    }

    @Test
    @DisplayName("Should return user profile when user exists in database")
    @WithMockUser(roles = {"MANAGER"})
    void shouldReturnUserProfileWhenUserExists() throws Exception {
        // Given - Save user to database
        User savedUser = userRepository.save(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "john.doe")
                    .claim("email", "john.doe@example.com")
                    .claim("given_name", "John")
                    .claim("family_name", "Doe")
                    .claim("name", "John Doe")
                    .claim("realm_access", java.util.Map.of("roles", List.of("MANAGER", "EMPLOYEE")))
                    .claim("tenant_id", "tenant-123")
                    .claim("shop_id", "shop-456")
                ))
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
            .andExpect(jsonPath("$.isInvestor").value(false))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles[0]").value("MANAGER"))
            .andExpect(jsonPath("$.roles[1]").value("EMPLOYEE"))
            .andExpect(jsonPath("$.tenantId").value("tenant-123"))
            .andExpect(jsonPath("$.shopId").value("shop-456"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Should return JWT-based profile when user not found in database")
    @WithMockUser(roles = {"MANAGER"})
    void shouldReturnJwtProfileWhenUserNotInDatabase() throws Exception {
        // Given - No user in database (repository is cleared in @BeforeEach)

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-different-id")
                    .claim("preferred_username", "jane.doe")
                    .claim("email", "jane.doe@example.com")
                    .claim("given_name", "Jane")
                    .claim("family_name", "Doe")
                    .claim("name", "Jane Doe")
                    .claim("realm_access", java.util.Map.of("roles", List.of("INVESTOR")))
                    .claim("tenant_id", "tenant-789")
                    .claim("shop_id", "shop-999")
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("keycloak-different-id"))
            .andExpect(jsonPath("$.username").value("jane.doe"))
            .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.fullName").value("Jane Doe"))
            .andExpect(jsonPath("$.roles[0]").value("INVESTOR"))
            .andExpect(jsonPath("$.tenantId").value("tenant-789"))
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
    @WithMockUser(roles = {"CUSTOMER"})
    void shouldAllowAnyAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "john.doe")
                    .claim("email", "john.doe@example.com")
                    .claim("given_name", "John")
                    .claim("family_name", "Doe")
                    .claim("name", "John Doe")
                    .claim("realm_access", java.util.Map.of("roles", List.of("CUSTOMER")))
                    .claim("tenant_id", "tenant-123")
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("john.doe"))
            .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    @Test
    @DisplayName("Should accept TENANT_ADMIN role")
    @WithMockUser(roles = {"TENANT_ADMIN"})
    void shouldAcceptTenantAdminRole() throws Exception {
        // Given - Save user to database
        User savedUser = userRepository.save(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "admin.user")
                    .claim("realm_access", java.util.Map.of("roles", List.of("TENANT_ADMIN")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @DisplayName("Should accept INVESTOR role")
    @WithMockUser(roles = {"INVESTOR"})
    void shouldAcceptInvestorRole() throws Exception {
        // Given - Create and save investor user
        User investorUser = User.builder()
            .keycloakId("keycloak-789")
            .username("investor.user")
            .email("investor@example.com")
            .firstName("Investor")
            .lastName("User")
            .status(User.UserStatus.ACTIVE)
            .isInvestor(true)
            .build();

        User savedInvestor = userRepository.save(investorUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-789")
                    .claim("preferred_username", "investor.user")
                    .claim("email", "investor@example.com")
                    .claim("realm_access", java.util.Map.of("roles", List.of("INVESTOR")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedInvestor.getId()))
            .andExpect(jsonPath("$.isInvestor").value(true));
    }

    @Test
    @DisplayName("Should accept EMPLOYEE role")
    @WithMockUser(roles = {"EMPLOYEE"})
    void shouldAcceptShopEmployeeRole() throws Exception {
        // Given - Save user to database
        User savedUser = userRepository.save(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "employee.user")
                    .claim("realm_access", java.util.Map.of("roles", List.of("EMPLOYEE")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(savedUser.getId()));
    }

    @Test
    @DisplayName("Should handle missing JWT claims gracefully")
    @WithMockUser(roles = {"MANAGER"})
    void shouldHandleMissingJwtClaims() throws Exception {
        // Given - No user in database for this Keycloak ID

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-minimal")
                    .claim("preferred_username", "minimal.user")
                    // Missing many optional claims
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("keycloak-minimal"))
            .andExpect(jsonPath("$.username").value("minimal.user"))
            .andExpect(jsonPath("$.email").doesNotExist())
            .andExpect(jsonPath("$.firstName").doesNotExist())
            .andExpect(jsonPath("$.lastName").doesNotExist());
    }
}