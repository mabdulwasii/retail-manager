package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.registration.ContactUserRequest;
import com.princely.shopmanager.core.dto.registration.ShopInfoRequest;
import com.princely.shopmanager.core.dto.registration.TenantInfoRequest;
import com.princely.shopmanager.core.dto.registration.TenantRegistrationRequest;
import com.princely.shopmanager.core.dto.registration.TenantRegistrationResponse;
import com.princely.shopmanager.core.service.TenantRegistrationService;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    TenantRegistrationControllerTest.ControllerTestConfiguration.class
})
@DisplayName("Tenant Registration Controller Tests")
class TenantRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantRegistrationService tenantRegistrationService;

    @Test
    @DisplayName("Should register tenant successfully with valid request")
    void shouldRegisterTenantSuccessfully() throws Exception {
        // Given
        TenantRegistrationRequest request = createValidRegistrationRequest();
        TenantRegistrationResponse expectedResponse = TenantRegistrationResponse.success(
            "tenant-123",
            "Test Tenant",
            "user-123",
            "admin@test.com",
            List.of("shop-123")
        );

        when(tenantRegistrationService.registerTenant(any(), any(), any()))
            .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/public/registration/tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId").value("tenant-123"))
                .andExpect(jsonPath("$.tenantName").value("Test Tenant"))
                .andExpect(jsonPath("$.contactUserId").value("user-123"))
                .andExpect(jsonPath("$.contactUserEmail").value("admin@test.com"))
                .andExpect(jsonPath("$.shopIds[0]").value("shop-123"))
                .andExpect(jsonPath("$.registrationStatus").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.requiresApproval").value(true));
    }

    @Test
    @DisplayName("Should return 400 for invalid tenant registration request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Given - Invalid request without required fields
        TenantRegistrationRequest invalidRequest = TenantRegistrationRequest.builder()
            .termsAccepted(false)
            .privacyPolicyAccepted(false)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/public/registration/tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should check tenant name availability")
    void shouldCheckTenantNameAvailability() throws Exception {
        // Given
        when(tenantRegistrationService.isTenantNameAvailable("available-name"))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/public/registration/check-tenant-name")
                .param("name", "available-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("available-name"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Tenant name is available"));
    }

    @Test
    @DisplayName("Should check username availability")
    void shouldCheckUsernameAvailability() throws Exception {
        // Given
        when(tenantRegistrationService.isUsernameAvailable("available-username"))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/public/registration/check-username")
                .param("username", "available-username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("available-username"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Username is available"));
    }

    @Test
    @DisplayName("Should check email availability")
    void shouldCheckEmailAvailability() throws Exception {
        // Given
        when(tenantRegistrationService.isEmailAvailable("available@test.com"))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/public/registration/check-email")
                .param("email", "available@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("available@test.com"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Email is available"));
    }

    @Test
    @DisplayName("Should return 400 for missing tenant name parameter")
    void shouldReturn400ForMissingTenantNameParameter() throws Exception {
        mockMvc.perform(get("/api/v1/public/registration/check-tenant-name"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void shouldHandleServiceExceptionsGracefully() throws Exception {
        // Given
        TenantRegistrationRequest request = createValidRegistrationRequest();
        when(tenantRegistrationService.registerTenant(any(), any(), any()))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/v1/public/registration/tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    private TenantRegistrationRequest createValidRegistrationRequest() {
        TenantInfoRequest tenantInfo = new TenantInfoRequest(
            "Test Tenant",
            "Test Description for tenant registration",
            "tenant@test.com",
            "123 Main St",
            "Test City",
            "Test State",
            "Test Country",
            "12345",
            "REG123",
            "TAX123",
            "+15550123"
        );

        ContactUserRequest contactUser = new ContactUserRequest(
            "testadmin",
            "admin@test.com",
            "Test",
            "Admin",
            "+15550124",
            "123 Admin St",
            "Admin City",
            "Admin State",
            "Admin Country",
            "54321"
        );

        ShopInfoRequest shop = new ShopInfoRequest(
            "shop-123",
            "Test Shop",
            "Test Shop Description for registration",
            "123 Shop St",
            "Shop City",
            "Shop State",
            "Shop Country",
            "54321",
            "+15550125",
            "shop@test.com",
            "SHOP-TAX123"
        );

        return TenantRegistrationRequest.builder()
            .tenantInfo(tenantInfo)
            .contactUser(contactUser)
            .shops(List.of(shop))
            .termsAccepted(true)
            .privacyPolicyAccepted(true)
            .agreementVersion("1.0")
            .build();
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public TenantRegistrationController tenantRegistrationController(TenantRegistrationService tenantRegistrationService) {
            return new TenantRegistrationController(tenantRegistrationService);
        }
    }
}