package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.core.dto.registration.PendingTenantResponse;
import com.princely.shopmanager.core.service.TenantRegistrationService;
import com.princely.shopmanager.shared.service.FeatureFlagService;
import com.princely.shopmanager.auth.principal.UserPrincipal;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(controllers = TenantAdminController.class)
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    TenantAdminControllerTest.ControllerTestConfiguration.class
})
@DisplayName("Tenant Admin Controller Tests")
class TenantAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantRegistrationService tenantRegistrationService;

    @MockBean
    private FeatureFlagService featureFlagService;

    @Test
    @DisplayName("Should return pending registrations for super admin")
    @WithMockPermissions(role = "SUPER_ADMIN")
    void shouldReturnPendingRegistrationsForSuperAdmin() throws Exception {
        // Given
        List<PendingTenantResponse> pendingTenants = List.of(
            new PendingTenantResponse(
                "tenant-1",
                "Test Tenant",
                "Test Description",
                "contact@tenant.com",
                "John Doe",
                "john@tenant.com",
                "+1234567890",
                "123 Main St",
                "City",
                "State",
                "Country",
                "12345",
                List.of(),
                LocalDateTime.now(),
                "INACTIVE",
                "REG123",
                "TAX123"
            )
        );

        when(tenantRegistrationService.getPendingRegistrations()).thenReturn(pendingTenants);

        // When & Then
        mockMvc.perform(get("/api/admin/tenants/pending"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].tenantId").value("tenant-1"))
            .andExpect(jsonPath("$[0].tenantName").value("Test Tenant"));

        verify(tenantRegistrationService).getPendingRegistrations();
    }

    @Test
    @DisplayName("Should deny access to pending registrations for non-super admin")
    @WithMockPermissions(role = "TENANT_ADMIN")
    void shouldDenyAccessToPendingRegistrationsForNonSuperAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/tenants/pending"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should activate tenant successfully")
    void shouldActivateTenantSuccessfully() throws Exception {
        // Given
        String tenantId = "tenant-1";
        TenantActivationRequest request = new TenantActivationRequest(
            tenantId,
            true,
            null,
            List.of("shop-1", "shop-2"),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
                .with(withUserPrincipal("admin", "SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.approved").value(true))
            .andExpect(jsonPath("$.message").value("Tenant approved and activated successfully"));

        verify(tenantRegistrationService).activateTenant(any(TenantActivationRequest.class), anyString());
    }

    @Test
    @DisplayName("Should reject tenant with reason")
    void shouldRejectTenantWithReason() throws Exception {
        // Given
        String tenantId = "tenant-1";
        TenantActivationRequest request = new TenantActivationRequest(
            tenantId,
            false,
            "Incomplete documentation",
            null,
            null
        );

        // When & Then
        mockMvc.perform(post("/api/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
                .with(withUserPrincipal("admin", "SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.approved").value(false))
            .andExpect(jsonPath("$.message").value("Tenant registration rejected: Incomplete documentation"));

        verify(tenantRegistrationService).activateTenant(any(TenantActivationRequest.class), anyString());
    }

    @Test
    @DisplayName("Should return 400 when tenant ID mismatch")
    void shouldReturn400WhenTenantIdMismatch() throws Exception {
        // Given
        String pathTenantId = "tenant-1";
        TenantActivationRequest request = new TenantActivationRequest(
            "tenant-2", // Different tenant ID
            true,
            null,
            List.of("shop-1"),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/admin/tenants/{tenantId}/activate", pathTenantId)
                .with(csrf())
                .with(withUserPrincipal("admin", "SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return tenant details for super admin")
    @WithMockPermissions(role = "SUPER_ADMIN")
    void shouldReturnTenantDetailsForSuperAdmin() throws Exception {
        // Given
        String tenantId = "tenant-1";
        PendingTenantResponse tenantDetails = new PendingTenantResponse(
            tenantId,
            "Test Tenant",
            "Test Description",
            "contact@tenant.com",
            "John Doe",
            "john@tenant.com",
            "+1234567890",
            "123 Main St",
            "City",
            "State",
            "Country",
            "12345",
            List.of(),
            LocalDateTime.now(),
            "INACTIVE",
            "REG123",
            "TAX123"
        );

        when(tenantRegistrationService.getTenantDetails(tenantId)).thenReturn(tenantDetails);

        // When & Then
        mockMvc.perform(get("/api/admin/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.tenantName").value("Test Tenant"));

        verify(tenantRegistrationService).getTenantDetails(tenantId);
    }

    @Test
    @DisplayName("Should deny access to tenant details for non-super admin")
    @WithMockPermissions(role = "TENANT_ADMIN")
    void shouldDenyAccessToTenantDetailsForNonSuperAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/tenants/tenant-1"))
            .andExpect(status().isForbidden());
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public TenantAdminController tenantAdminController(TenantRegistrationService tenantRegistrationService) {
            return new TenantAdminController(tenantRegistrationService);
        }
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor withUserPrincipal(String username, String... roles) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(
            new UsernamePasswordAuthenticationToken(
                UserPrincipal.of(
                    "user-id",
                    username + "@example.com",
                    username,
                    List.of(roles)
                ),
                "password",
                List.of(roles).stream().map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)).toList()
            )
        );
    }
}