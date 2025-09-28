package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.UserProfileResponse;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private JwtPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("user-123")
            .keycloakId("keycloak-456")
            .username("john.doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .phoneNumber("+1234567890")
            .status(User.UserStatus.ACTIVE)
            .isInvestor(false)
            .build();
        // Set the auditing fields manually for testing
        testUser.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        testUser.setUpdatedAt(LocalDateTime.of(2024, 1, 20, 15, 45));

        testPrincipal = JwtPrincipal.builder()
            .subject("keycloak-456")
            .preferredUsername("john.doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .roles(List.of("SHOP_MANAGER", "SHOP_EMPLOYEE"))
            .tenantId("tenant-123")
            .build();
    }

    @Test
    @DisplayName("Should return user profile when user exists in database")
    @WithMockUser(roles = {"SHOP_MANAGER"})
    void shouldReturnUserProfileWhenUserExists() throws Exception {
        // Given
        when(userService.getUserByKeycloakId("keycloak-456")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "john.doe")
                    .claim("email", "john.doe@example.com")
                    .claim("given_name", "John")
                    .claim("family_name", "Doe")
                    .claim("name", "John Doe")
                    .claim("realm_access", java.util.Map.of("roles", List.of("SHOP_MANAGER", "SHOP_EMPLOYEE")))
                    .claim("tenant_id", "tenant-123")
                    .claim("shop_id", "shop-456")
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("user-123"))
            .andExpect(jsonPath("$.username").value("john.doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.fullName").value("John Doe"))
            .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.isInvestor").value(false))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles[0]").value("SHOP_MANAGER"))
            .andExpect(jsonPath("$.roles[1]").value("SHOP_EMPLOYEE"))
            .andExpect(jsonPath("$.tenantId").value("tenant-123"))
            .andExpect(jsonPath("$.shopId").value("shop-456"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Should return JWT-based profile when user not found in database")
    @WithMockUser(roles = {"SHOP_MANAGER"})
    void shouldReturnJwtProfileWhenUserNotInDatabase() throws Exception {
        // Given
        when(userService.getUserByKeycloakId("keycloak-456")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
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
            .andExpect(jsonPath("$.id").value("keycloak-456"))
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
    @DisplayName("Should return 403 when user lacks required role")
    @WithMockUser(roles = {"INVALID_ROLE"})
    void shouldReturn403WhenInvalidRole() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "john.doe")
                    .claim("realm_access", java.util.Map.of("roles", List.of("INVALID_ROLE")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    @WithMockUser(roles = {"SHOP_MANAGER"})
    void shouldHandleServiceExceptions() throws Exception {
        // Given
        when(userService.getUserByKeycloakId(anyString())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "john.doe")
                    .claim("email", "john.doe@example.com")
                    .claim("realm_access", java.util.Map.of("roles", List.of("SHOP_MANAGER")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should accept TENANT_ADMIN role")
    @WithMockUser(roles = {"TENANT_ADMIN"})
    void shouldAcceptTenantAdminRole() throws Exception {
        // Given
        when(userService.getUserByKeycloakId("keycloak-456")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "admin.user")
                    .claim("realm_access", java.util.Map.of("roles", List.of("TENANT_ADMIN")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("user-123"));
    }

    @Test
    @DisplayName("Should accept INVESTOR role")
    @WithMockUser(roles = {"INVESTOR"})
    void shouldAcceptInvestorRole() throws Exception {
        // Given
        User investorUser = User.builder()
            .id("investor-123")
            .keycloakId("keycloak-789")
            .username("investor.user")
            .email("investor@example.com")
            .firstName("Investor")
            .lastName("User")
            .status(User.UserStatus.ACTIVE)
            .isInvestor(true)
            .build();

        when(userService.getUserByKeycloakId("keycloak-789")).thenReturn(investorUser);

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
            .andExpect(jsonPath("$.id").value("investor-123"))
            .andExpect(jsonPath("$.isInvestor").value(true));
    }

    @Test
    @DisplayName("Should accept SHOP_EMPLOYEE role")
    @WithMockUser(roles = {"SHOP_EMPLOYEE"})
    void shouldAcceptShopEmployeeRole() throws Exception {
        // Given
        when(userService.getUserByKeycloakId("keycloak-456")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "employee.user")
                    .claim("realm_access", java.util.Map.of("roles", List.of("SHOP_EMPLOYEE")))
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("user-123"));
    }

    @Test
    @DisplayName("Should handle missing JWT claims gracefully")
    @WithMockUser(roles = {"SHOP_MANAGER"})
    void shouldHandleMissingJwtClaims() throws Exception {
        // Given
        when(userService.getUserByKeycloakId("keycloak-456")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt
                    .subject("keycloak-456")
                    .claim("preferred_username", "minimal.user")
                    // Missing many optional claims
                ))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("keycloak-456"))
            .andExpect(jsonPath("$.username").value("minimal.user"))
            .andExpect(jsonPath("$.email").doesNotExist())
            .andExpect(jsonPath("$.firstName").doesNotExist())
            .andExpect(jsonPath("$.lastName").doesNotExist());
    }
}