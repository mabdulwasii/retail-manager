package com.princely.shopmanager.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.core.dto.registration.PendingTenantResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.princely.shopmanager.auth.principal.UserPrincipal;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@TestPropertySource(properties = {
    "app.features.analytics.enabled=false",
    "app.features.investment.enabled=false",
    "app.features.fraud.enabled=false"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    TenantAdminIntegrationTest.ControllerTestConfiguration.class
})
@DisplayName("Tenant Admin Integration Tests")
class TenantAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantRegistrationService tenantRegistrationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should get pending tenant registrations")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldGetPendingTenantRegistrations() throws Exception {
        // Given
        PendingTenantResponse.PendingShopInfo shopInfo = new PendingTenantResponse.PendingShopInfo(
            "test-shop-id",
            "Test Shop",
            "A test shop",
            "456 Shop Avenue",
            "Shop City",
            "Shop State",
            "Shop Country",
            "shop@testtenant.com",
            "+1111111111"
        );

        PendingTenantResponse mockResponse = new PendingTenantResponse(
            "test-tenant-id",
            "Test Tenant Corp",
            "A test tenant for integration testing",
            "contact@testtenant.com",
            "testuser",
            "testuser@testtenant.com",
            "+1234567890",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            List.of(shopInfo),
            LocalDateTime.now(),
            "INACTIVE",
            "REG123",
            "TAX123"
        );
        when(tenantRegistrationService.getPendingRegistrations()).thenReturn(List.of(mockResponse));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/pending"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].tenantId").value("test-tenant-id"))
            .andExpect(jsonPath("$[0].tenantName").value("Test Tenant Corp"))
            .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    @DisplayName("Should deny access to non-super admin users")
    @WithMockUser(roles = "TENANT_ADMIN")
    void shouldDenyAccessToNonSuperAdminUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/pending"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get specific tenant details")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldGetSpecificTenantDetails() throws Exception {
        // Given
        String tenantId = "test-tenant-id";
        PendingTenantResponse.PendingShopInfo shopInfo = new PendingTenantResponse.PendingShopInfo(
            "test-shop-id",
            "Test Shop",
            "A test shop",
            "456 Shop Avenue",
            "Shop City",
            "Shop State",
            "Shop Country",
            "shop@testtenant.com",
            "+1111111111"
        );

        PendingTenantResponse mockResponse = new PendingTenantResponse(
            tenantId,
            "Test Tenant Corp",
            "A test tenant for integration testing",
            "contact@testtenant.com",
            "testuser",
            "testuser@testtenant.com",
            "+1234567890",
            "123 Test Street",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            List.of(shopInfo),
            LocalDateTime.now(),
            "INACTIVE",
            "REG123",
            "TAX123"
        );
        when(tenantRegistrationService.getTenantDetails(tenantId)).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/{tenantId}", tenantId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.tenantName").value("Test Tenant Corp"))
            .andExpect(jsonPath("$.contactUserEmail").value("testuser@testtenant.com"));
    }

    @Test
    @DisplayName("Should activate tenant successfully")
    void shouldActivateTenantSuccessfully() throws Exception {
        // Given
        String tenantId = "test-tenant-id";
        String shopId = "test-shop-id";
        TenantActivationRequest request = new TenantActivationRequest(
            tenantId,
            true,
            null,
            List.of(shopId),
            "Approved for testing"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
                .with(withUserPrincipal("admin", "SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.approved").value(true))
            .andExpect(jsonPath("$.message").value("Tenant approved and activated successfully"));
    }

    @Test
    @DisplayName("Should reject tenant with reason")
    void shouldRejectTenantWithReason() throws Exception {
        // Given
        String tenantId = "test-tenant-id";
        TenantActivationRequest request = new TenantActivationRequest(
            tenantId,
            false,
            "Incomplete documentation provided",
            null,
            "Needs additional verification"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
                .with(withUserPrincipal("admin", "SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tenantId").value(tenantId))
            .andExpect(jsonPath("$.approved").value(false))
            .andExpect(jsonPath("$.message").value("Tenant registration rejected: Incomplete documentation provided"));
    }

    @Test
    @DisplayName("Should return 400 for tenant ID mismatch")
    void shouldReturn400ForTenantIdMismatch() throws Exception {
        // Given
        String tenantId = "test-tenant-id";
        String shopId = "test-shop-id";
        TenantActivationRequest request = new TenantActivationRequest(
            "different-tenant-id",
            true,
            null,
            List.of(shopId),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", tenantId)
                .with(csrf())
                .with(withUserPrincipal("admin", "SUPER_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor withUserPrincipal(String username, String... roles) {
        return authentication(
            new UsernamePasswordAuthenticationToken(
                UserPrincipal.of(
                    "test-user-id",
                    username + "@example.com",
                    username,
                    List.of(roles)
                ),
                "password",
                List.of(roles).stream().map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)).toList()
            )
        );
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public com.princely.shopmanager.core.controller.TenantAdminController tenantAdminController(TenantRegistrationService tenantRegistrationService) {
            return new com.princely.shopmanager.core.controller.TenantAdminController(tenantRegistrationService);
        }
    }
}