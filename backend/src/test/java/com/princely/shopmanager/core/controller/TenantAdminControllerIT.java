package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.test.TestConstants;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TenantAdminController using TestContainers with PostgreSQL.
 * Tests use real database with test-data.sql for realistic end-to-end scenarios.
 */
@DisplayName("Tenant Admin Controller Integration Tests")
@Disabled("Temporarily disabled during IT reduction - will be replaced with TenantAdminControllerMinimalIT and unit tests")
class TenantAdminControllerIT extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return pending registrations for super admin")
    @WithMockPermissions(role = "SUPER_ADMIN")
    void shouldReturnPendingRegistrationsForSuperAdmin() throws Exception {
        // When & Then - test-data.sql has 2 tenants loaded
        mockMvc.perform(get("/api/admin/tenants/pending"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(TestConstants.EXPECTED_TENANT_COUNT)));
    }

    @Test
    @DisplayName("Should deny access to pending registrations for non-super admin")
    @WithMockPermissions(role = "MANAGER")
    void shouldDenyAccessToPendingRegistrationsForNonSuperAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/tenants/pending"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockPermissions(role = "SUPER_ADMIN")
    @DisplayName("Should activate tenant successfully")
    void shouldActivateTenantSuccessfully() throws Exception {
        // Given - Use existing tenant from test-data.sql
        String tenantId = TestConstants.TEST_TENANT_001;
        TenantActivationRequest request = new TenantActivationRequest(
            tenantId,
            true,
            null,
            List.of(TestConstants.TEST_SHOP_001, TestConstants.TEST_SHOP_002),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.approved").value(true))
            .andExpect(jsonPath("$.message").value("Tenant approved and activated successfully"));
    }

    @Test
    @WithMockPermissions(role = "SUPER_ADMIN")
    @DisplayName("Should reject tenant with reason")
    void shouldRejectTenantWithReason() throws Exception {
        // Given - Use existing tenant from test-data.sql
        String tenantId = TestConstants.TEST_TENANT_002;
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.approved").value(false))
            .andExpect(jsonPath("$.message").value("Tenant registration rejected: Incomplete documentation"));
    }

    @Test
    @WithMockPermissions(role = "SUPER_ADMIN")
    @DisplayName("Should return 400 when tenant ID mismatch")
    void shouldReturn400WhenTenantIdMismatch() throws Exception {
        // Given
        String pathTenantId = TestConstants.TEST_TENANT_001;
        TenantActivationRequest request = new TenantActivationRequest(
            TestConstants.TEST_TENANT_002, // Different tenant ID
            true,
            null,
            List.of(TestConstants.TEST_SHOP_001),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/admin/tenants/{tenantId}/activate", pathTenantId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return tenant details for super admin")
    @WithMockPermissions(role = "SUPER_ADMIN")
    void shouldReturnTenantDetailsForSuperAdmin() throws Exception {
        // Given - Use existing tenant from test-data.sql
        String tenantId = TestConstants.TEST_TENANT_001;

        // When & Then
        mockMvc.perform(get("/api/admin/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.tenantName").value(TestConstants.TENANT_NAME_TEST_RETAIL));
    }

    @Test
    @DisplayName("Should deny access to tenant details for non-super admin")
    @WithMockPermissions(role = "MANAGER")
    void shouldDenyAccessToTenantDetailsForNonSuperAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/tenants/" + TestConstants.TEST_TENANT_001))
            .andExpect(status().isForbidden());
    }
}
