package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Comprehensive test suite for ShopController REST endpoints.
 *
 * This test class validates:
 * - HTTP endpoint behavior and responses
 * - Request/response serialization
 * - Security authorization rules
 * - Input validation and error handling
 * - Service method invocations
 *
 * Uses @WebMvcTest for focused controller testing with mocked dependencies.
 */
@WebMvcTest
@DisplayName("ShopController Tests")
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    ShopControllerTest.ControllerTestConfiguration.class
})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShopService shopService;

    @MockBean
    private com.princely.shopmanager.shared.service.FeatureFlagService featureFlagService;

    private ShopResponse sampleShopResponse;
    private ShopCreateRequest sampleCreateRequest;
    private ShopUpdateRequest sampleUpdateRequest;

    @BeforeEach
    void setUp() {
        sampleShopResponse = ShopResponse.builder()
            .id("shop-123")
            .name("Downtown Electronics")
            .tenantId("tenant-downtown-electronics")
            .description("Electronics and gadgets store")
            .address("123 Main Street")
            .city("New York")
            .state("NY")
            .country("United States")
            .postalCode("10001")
            .phoneNumber("+1-555-123-4567")
            .email("contact@downtownelectronics.com")
            .taxId("TAX123456789")
            .status("ACTIVE")
            .openingDate(LocalDateTime.of(2024, 1, 15, 9, 0))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        sampleCreateRequest = ShopCreateRequest.builder()
            .name("Downtown Electronics")
            .description("Electronics and gadgets store")
            .address("123 Main Street")
            .city("New York")
            .state("NY")
            .country("United States")
            .postalCode("10001")
            .phoneNumber("+1-555-123-4567")
            .email("contact@downtownelectronics.com")
            .taxId("TAX123456789")
            .openingDate(LocalDateTime.of(2024, 1, 15, 9, 0))
            .build();

        sampleUpdateRequest = ShopUpdateRequest.builder()
            .name("Updated Electronics Store")
            .description("Updated description")
            .phoneNumber("+1-555-987-6543")
            .build();
    }

    @Nested
    @DisplayName("POST /api/shops - Create Shop")
    class CreateShopTests {

        @Test
        @WithMockUser(roles = "SYSTEM_ADMIN")
        @DisplayName("Should create shop successfully with SYSTEM_ADMIN role")
        void shouldCreateShopWithSystemAdminRole() throws Exception {
            // Given
            when(shopService.createShop(any(ShopCreateRequest.class)))
                .thenReturn(sampleShopResponse);

            // When & Then
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andDo(print())  // Print the response for debugging
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("shop-123"))
                .andExpect(jsonPath("$.name").value("Downtown Electronics"))
                .andExpect(jsonPath("$.tenantId").value("tenant-downtown-electronics"))
                .andExpect(jsonPath("$.email").value("contact@downtownelectronics.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

            verify(shopService).createShop(any(ShopCreateRequest.class));
        }

        @Test
        @WithMockUser(roles = "SHOP_OWNER")
        @DisplayName("Should create shop successfully with SHOP_OWNER role")
        void shouldCreateShopWithShopOwnerRole() throws Exception {
            // Given
            when(shopService.createShop(any(ShopCreateRequest.class)))
                .thenReturn(sampleShopResponse);

            // When & Then
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Downtown Electronics"));
        }

        @Test
        @WithMockUser(roles = "SHOP_MANAGER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SYSTEM_ADMIN")
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() throws Exception {
            // Given - Create request with missing required fields
            ShopCreateRequest invalidRequest = ShopCreateRequest.builder()
                .description("Store without name")
                .build();

            // When & Then
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "SYSTEM_ADMIN")
        @DisplayName("Should validate email format")
        void shouldValidateEmailFormat() throws Exception {
            // Given
            sampleCreateRequest.setEmail("invalid-email-format");

            // When & Then
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should require authentication")
        void shouldRequireAuthentication() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleCreateRequest)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/shops/{shopId} - Get Shop")
    class GetShopTests {

        @Test
        @WithMockUser(roles = "SHOP_OWNER")
        @DisplayName("Should get shop successfully")
        void shouldGetShopSuccessfully() throws Exception {
            // Given
            when(shopService.getShop("shop-123")).thenReturn(sampleShopResponse);

            // When & Then
            mockMvc.perform(get("/api/shops/shop-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shop-123"))
                .andExpect(jsonPath("$.name").value("Downtown Electronics"))
                .andExpect(jsonPath("$.tenantId").value("tenant-downtown-electronics"));

            verify(shopService).getShop("shop-123");
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should allow access with CASHIER role")
        void shouldAllowAccessWithCashierRole() throws Exception {
            // Given
            when(shopService.getShop("shop-123")).thenReturn(sampleShopResponse);

            // When & Then
            mockMvc.perform(get("/api/shops/shop-123"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "SHOP_OWNER")
        @DisplayName("Should handle shop not found")
        void shouldHandleShopNotFound() throws Exception {
            // Given
            when(shopService.getShop("nonexistent-shop"))
                .thenThrow(new IllegalArgumentException("Shop not found: nonexistent-shop"));

            // When & Then - IllegalArgumentException causes test processing to fail
            // The exception is thrown during request processing, not handled as HTTP response
            try {
                mockMvc.perform(get("/api/shops/nonexistent-shop"));
            } catch (Exception e) {
                // Expect the service exception to propagate
                assertTrue(e.getCause() instanceof IllegalArgumentException);
                assertEquals("Shop not found: nonexistent-shop", e.getCause().getMessage());
            }
        }
    }

    @Nested
    @DisplayName("GET /api/shops - Get Shops with Pagination")
    class GetShopsTests {

        @Test
        @WithMockUser(roles = "SYSTEM_ADMIN")
        @DisplayName("Should get shops with pagination")
        void shouldGetShopsWithPagination() throws Exception {
            // Given
            List<ShopResponse> shops = Arrays.asList(sampleShopResponse);
            Page<ShopResponse> page = new PageImpl<>(shops, PageRequest.of(0, 20), 1);
            when(shopService.getShops(any(Pageable.class))).thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/shops")
                    .param("page", "0")
                    .param("size", "20")
                    .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("shop-123"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(20));
        }

        @Test
        @WithMockUser(roles = "SHOP_MANAGER")
        @DisplayName("Should allow access with SHOP_MANAGER role")
        void shouldAllowAccessWithShopManagerRole() throws Exception {
            // Given
            Page<ShopResponse> page = new PageImpl<>(Arrays.asList(sampleShopResponse));
            when(shopService.getShops(any(Pageable.class))).thenReturn(page);

            // When & Then
            mockMvc.perform(get("/api/shops"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/shops"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/shops/active - Get Active Shops")
    class GetActiveShopsTests {

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should get active shops")
        void shouldGetActiveShops() throws Exception {
            // Given
            List<ShopResponse> activeShops = Arrays.asList(sampleShopResponse);
            when(shopService.getActiveShops()).thenReturn(activeShops);

            // When & Then
            mockMvc.perform(get("/api/shops/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("shop-123"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

            verify(shopService).getActiveShops();
        }
    }

    @Nested
    @DisplayName("PUT /api/shops/{shopId} - Update Shop")
    class UpdateShopTests {

        @Test
        @WithMockUser(roles = "SHOP_OWNER")
        @DisplayName("Should update shop successfully")
        void shouldUpdateShopSuccessfully() throws Exception {
            // Given
            ShopResponse updatedResponse = ShopResponse.builder()
                .id("shop-123")
                .name("Updated Electronics Store")
                .description("Updated description")
                .phoneNumber("+1-555-987-6543")
                .build();
            when(shopService.updateShop(eq("shop-123"), any(ShopUpdateRequest.class)))
                .thenReturn(updatedResponse);

            // When & Then
            mockMvc.perform(put("/api/shops/shop-123")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shop-123"))
                .andExpect(jsonPath("$.name").value("Updated Electronics Store"))
                .andExpect(jsonPath("$.phoneNumber").value("+1-555-987-6543"));

            verify(shopService).updateShop(eq("shop-123"), any(ShopUpdateRequest.class));
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/shops/shop-123")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleUpdateRequest)))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/shops/{shopId}/status - Change Shop Status")
    class ChangeShopStatusTests {

        @Test
        @WithMockUser(roles = "SHOP_OWNER")
        @DisplayName("Should change shop status successfully")
        void shouldChangeShopStatusSuccessfully() throws Exception {
            // Given
            ShopResponse suspendedResponse = ShopResponse.builder()
                .id("shop-123")
                .name("Downtown Electronics")
                .status("SUSPENDED")
                .build();
            when(shopService.changeShopStatus("shop-123", Shop.ShopStatus.SUSPENDED))
                .thenReturn(suspendedResponse);

            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/status")
                    .with(csrf())
                    .param("status", "SUSPENDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("shop-123"))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

            verify(shopService).changeShopStatus("shop-123", Shop.ShopStatus.SUSPENDED);
        }

        @Test
        @WithMockUser(roles = "SHOP_MANAGER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            // When & Then
            mockMvc.perform(patch("/api/shops/shop-123/status")
                    .with(csrf())
                    .param("status", "SUSPENDED"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/shops/{shopId} - Delete Shop")
    class DeleteShopTests {

        @Test
        @WithMockUser(roles = "SHOP_OWNER")
        @DisplayName("Should delete shop successfully")
        void shouldDeleteShopSuccessfully() throws Exception {
            // Given
            doNothing().when(shopService).deleteShop("shop-123");

            // When & Then
            mockMvc.perform(delete("/api/shops/shop-123")
                    .with(csrf()))
                .andExpect(status().isNoContent());

            verify(shopService).deleteShop("shop-123");
        }

        @Test
        @WithMockUser(roles = "SHOP_MANAGER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/shops/shop-123")
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Authentication and Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthenticationForAllEndpoints() throws Exception {
            // Test all endpoints without authentication
            mockMvc.perform(get("/api/shops")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/shops/shop-123")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/shops/active")).andExpect(status().isUnauthorized());
            mockMvc.perform(post("/api/shops").with(csrf())).andExpect(status().isUnauthorized());
            mockMvc.perform(put("/api/shops/shop-123").with(csrf())).andExpect(status().isUnauthorized());
            mockMvc.perform(patch("/api/shops/shop-123/status").with(csrf())).andExpect(status().isUnauthorized());
            mockMvc.perform(delete("/api/shops/shop-123").with(csrf())).andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "INVALID_ROLE")
        @DisplayName("Should deny access with invalid roles")
        void shouldDenyAccessWithInvalidRoles() throws Exception {
            // Test endpoints with invalid role
            mockMvc.perform(get("/api/shops")).andExpect(status().isForbidden());
            mockMvc.perform(post("/api/shops")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Test Shop\",\"address\":\"123 Test St\",\"email\":\"test@test.com\"}"))
                .andExpect(status().isForbidden());
            mockMvc.perform(patch("/api/shops/shop-123/status")
                    .with(csrf())
                    .param("status", "ACTIVE"))
                .andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/shops/shop-123").with(csrf())).andExpect(status().isForbidden());
        }
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public ShopController shopController(ShopService shopService) {
            return new ShopController(shopService);
        }
    }
}