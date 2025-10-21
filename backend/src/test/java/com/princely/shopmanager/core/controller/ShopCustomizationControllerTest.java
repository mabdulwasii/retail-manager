package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.ShopCustomization;
import com.princely.shopmanager.core.dto.ShopCustomizationRequest;
import com.princely.shopmanager.core.dto.ShopCustomizationResponse;
import com.princely.shopmanager.core.service.ShopCustomizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for ShopCustomizationController REST endpoints.
 *
 * Tests all customization endpoints including:
 * - GET/PUT customization
 * - PATCH color scheme
 * - PATCH theme settings
 * - POST logo upload
 * - PATCH contact info
 * - DELETE reset to defaults
 */
@WebMvcTest
@DisplayName("ShopCustomizationController Tests")
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    ShopCustomizationControllerTest.ControllerTestConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ShopCustomizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShopCustomizationService customizationService;

    @MockBean
    private com.princely.shopmanager.shared.service.FeatureFlagService featureFlagService;

    private ShopCustomization sampleCustomization;
    private ShopCustomizationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleCustomization = ShopCustomization.builder()
            .id("custom-123")
            .primaryColor("#007bff")
            .secondaryColor("#6c757d")
            .accentColor("#28a745")
            .backgroundColor("#ffffff")
            .textColor("#212529")
            .logoUrl("https://cdn.example.com/logo.png")
            .websiteUrl("https://www.myshop.com")
            .themeVariant(ShopCustomization.ThemeVariant.LIGHT)
            .fontSize(ShopCustomization.FontSize.MEDIUM)
            .dashboardLayout(ShopCustomization.DashboardLayout.GRID)
            .receiptShowLogo(true)
            .showBanner(true)
            .enableAnimations(true)
            .showAdvancedFeatures(false)
            .build();

        sampleResponse = ShopCustomizationResponse.builder()
            .id("custom-123")
            .shopId("shop-123")
            .primaryColor("#007bff")
            .secondaryColor("#6c757d")
            .accentColor("#28a745")
            .backgroundColor("#ffffff")
            .textColor("#212529")
            .logoUrl("https://cdn.example.com/logo.png")
            .websiteUrl("https://www.myshop.com")
            .themeVariant("LIGHT")
            .fontSize("MEDIUM")
            .dashboardLayout("GRID")
            .receiptShowLogo(true)
            .showBanner(true)
            .enableAnimations(true)
            .showAdvancedFeatures(false)
            .build();
    }

    @Nested
    @DisplayName("GET /api/shops/{shopId}/customization - Get Customization")
    class GetCustomizationTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should get customization successfully")
        void shouldGetCustomization() throws Exception {
            // Given
            when(customizationService.getShopCustomization("shop-123"))
                .thenReturn(Optional.of(sampleCustomization));

            // When & Then
            mockMvc.perform(get("/api/shops/shop-123/customization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryColor").value("#007bff"))
                .andExpect(jsonPath("$.secondaryColor").value("#6c757d"))
                .andExpect(jsonPath("$.accentColor").value("#28a745"))
                .andExpect(jsonPath("$.themeVariant").value("LIGHT"))
                .andExpect(jsonPath("$.fontSize").value("MEDIUM"))
                .andExpect(jsonPath("$.dashboardLayout").value("GRID"))
                .andExpect(jsonPath("$.receiptShowLogo").value(true))
                .andExpect(jsonPath("$.showBanner").value(true));

            verify(customizationService).getShopCustomization("shop-123");
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should return 404 when customization not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // Given
            when(customizationService.getShopCustomization("shop-123"))
                .thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/shops/shop-123/customization"))
                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            mockMvc.perform(get("/api/shops/shop-123/customization"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/shops/{shopId}/customization - Update Customization")
    class UpdateCustomizationTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should update customization successfully")
        void shouldUpdateCustomization() throws Exception {
            // Given
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#1e40af")
                .secondaryColor("#64748b")
                .themeVariant("DARK")
                .fontSize("LARGE")
                .build();

            when(customizationService.saveShopCustomization(eq("shop-123"), any(ShopCustomization.class)))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(put("/api/shops/shop-123/customization")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryColor").exists())
                .andExpect(jsonPath("$.themeVariant").exists());

            verify(customizationService).getShopCustomization("shop-123");
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("Should allow MANAGER role")
        void shouldAllowManagerRole() throws Exception {
            // Given
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#FF5733")
                .build();

            when(customizationService.saveShopCustomization(eq("shop-123"), any(ShopCustomization.class)))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(put("/api/shops/shop-123/customization")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#FF5733")
                .build();

            mockMvc.perform(put("/api/shops/shop-123/customization")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should validate color format")
        void shouldValidateColorFormat() throws Exception {
            // Given - Invalid hex color
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("INVALID") // Missing # and wrong format
                .build();

            // When & Then
            mockMvc.perform(put("/api/shops/shop-123/customization")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/shops/{shopId}/customization/colors - Update Colors")
    class UpdateColorsTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should update color scheme successfully")
        void shouldUpdateColorScheme() throws Exception {
            // Given
            when(customizationService.updateColorScheme(
                eq("shop-123"), eq("#1e40af"), eq("#64748b"), eq("#10b981")))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/customization/colors")
                    .with(csrf())
                    .param("primaryColor", "#1e40af")
                    .param("secondaryColor", "#64748b")
                    .param("accentColor", "#10b981"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryColor").exists());

            verify(customizationService).updateColorScheme(
                "shop-123", "#1e40af", "#64748b", "#10b981");
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should accept partial color updates")
        void shouldAcceptPartialColorUpdates() throws Exception {
            // Given
            when(customizationService.updateColorScheme(
                eq("shop-123"), eq("#1e40af"), isNull(), isNull()))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/customization/colors")
                    .with(csrf())
                    .param("primaryColor", "#1e40af"))
                .andExpect(status().isOk());

            verify(customizationService).updateColorScheme(
                "shop-123", "#1e40af", null, null);
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            mockMvc.perform(patch("/api/shops/shop-123/customization/colors")
                    .with(csrf())
                    .param("primaryColor", "#1e40af"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/shops/{shopId}/customization/theme - Update Theme")
    class UpdateThemeTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should update theme settings successfully")
        void shouldUpdateThemeSettings() throws Exception {
            // Given
            when(customizationService.updateThemeSettings(
                eq("shop-123"),
                eq(ShopCustomization.ThemeVariant.DARK),
                eq(ShopCustomization.FontSize.LARGE)))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/customization/theme")
                    .with(csrf())
                    .param("themeVariant", "DARK")
                    .param("fontSize", "LARGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themeVariant").exists())
                .andExpect(jsonPath("$.fontSize").exists());

            verify(customizationService).updateThemeSettings(
                "shop-123",
                ShopCustomization.ThemeVariant.DARK,
                ShopCustomization.FontSize.LARGE);
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should accept partial theme updates")
        void shouldAcceptPartialThemeUpdates() throws Exception {
            // Given
            when(customizationService.updateThemeSettings(
                eq("shop-123"),
                eq(ShopCustomization.ThemeVariant.AUTO),
                isNull()))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/customization/theme")
                    .with(csrf())
                    .param("themeVariant", "AUTO"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/shops/{shopId}/customization/logo - Upload Logo")
    class UploadLogoTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should upload logo successfully")
        void shouldUploadLogo() throws Exception {
            // Given
            MockMultipartFile logoFile = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "logo content".getBytes()
            );

            when(customizationService.uploadLogo(eq("shop-123"), any()))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(multipart("/api/shops/shop-123/customization/logo")
                    .file(logoFile)
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logoUrl").exists());

            verify(customizationService).uploadLogo(eq("shop-123"), any());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            MockMultipartFile logoFile = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "logo content".getBytes()
            );

            mockMvc.perform(multipart("/api/shops/shop-123/customization/logo")
                    .file(logoFile)
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/shops/{shopId}/customization/contact - Update Contact Info")
    class UpdateContactInfoTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should update contact info successfully")
        void shouldUpdateContactInfo() throws Exception {
            // Given
            when(customizationService.updateContactInfo(
                eq("shop-123"),
                eq("https://www.myshop.com"),
                eq("{\"facebook\":\"@myshop\"}")))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/customization/contact")
                    .with(csrf())
                    .param("websiteUrl", "https://www.myshop.com")
                    .param("socialMediaLinks", "{\"facebook\":\"@myshop\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.websiteUrl").exists());

            verify(customizationService).updateContactInfo(
                "shop-123",
                "https://www.myshop.com",
                "{\"facebook\":\"@myshop\"}");
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should accept partial contact info updates")
        void shouldAcceptPartialContactUpdates() throws Exception {
            // Given
            when(customizationService.updateContactInfo(
                eq("shop-123"),
                eq("https://www.myshop.com"),
                isNull()))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/customization/contact")
                    .with(csrf())
                    .param("websiteUrl", "https://www.myshop.com"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/shops/{shopId}/customization - Reset to Defaults")
    class ResetToDefaultsTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should reset to defaults successfully")
        void shouldResetToDefaults() throws Exception {
            // Given
            when(customizationService.resetToDefaults("shop-123"))
                .thenReturn(sampleCustomization);

            // When & Then
            mockMvc.perform(delete("/api/shops/shop-123/customization")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themeVariant").exists());

            verify(customizationService).resetToDefaults("shop-123");
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("Should deny access to MANAGER role")
        void shouldDenyAccessToManagerRole() throws Exception {
            mockMvc.perform(delete("/api/shops/shop-123/customization")
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            mockMvc.perform(delete("/api/shops/shop-123/customization")
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public ShopCustomizationController shopCustomizationController(
            ShopCustomizationService customizationService) {
            return new ShopCustomizationController(customizationService);
        }
    }
}
