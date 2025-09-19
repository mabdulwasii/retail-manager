package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.core.dto.registration.PendingTenantResponse;
import com.princely.shopmanager.core.service.TenantRegistrationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(TenantAdminController.class)
@DisplayName("Tenant Admin Controller Tests")
class TenantAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantRegistrationService tenantRegistrationService;

    @Test
    @DisplayName("Should return pending registrations for super admin")
    @WithMockUser(roles = "SUPER_ADMIN")
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
        mockMvc.perform(get("/api/v1/admin/tenants/pending"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].tenantId").value("tenant-1"))
            .andExpect(jsonPath("$[0].tenantName").value("Test Tenant"));

        verify(tenantRegistrationService).getPendingRegistrations();
    }

    @Test
    @DisplayName("Should deny access to pending registrations for non-super admin")
    @WithMockUser(roles = "TENANT_ADMIN")
    void shouldDenyAccessToPendingRegistrationsForNonSuperAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/pending"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should activate tenant successfully")
    @WithMockUser(roles = "SUPER_ADMIN", username = "admin")
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
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
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
    @WithMockUser(roles = "SUPER_ADMIN", username = "admin")
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
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
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
    @WithMockUser(roles = "SUPER_ADMIN", username = "admin")
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
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", pathTenantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return tenant details for super admin")
    @WithMockUser(roles = "SUPER_ADMIN")
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
        mockMvc.perform(get("/api/v1/admin/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.tenantName").value("Test Tenant"));

        verify(tenantRegistrationService).getTenantDetails(tenantId);
    }

    @Test
    @DisplayName("Should deny access to tenant details for non-super admin")
    @WithMockUser(roles = "TENANT_ADMIN")
    void shouldDenyAccessToTenantDetailsForNonSuperAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/tenant-1"))
            .andExpect(status().isForbidden());
    }
}