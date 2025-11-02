package com.princely.shopmanager.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.dto.InventoryAdjustmentRequest;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.dto.InventorySummaryDto;
import com.princely.shopmanager.inventory.service.InventoryService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.test.security.WithMockPermissions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@TestPropertySource(properties = {
    "app.security.tenant-isolation=false",
    "app.features.analytics.enabled=false",
    "app.features.investment.enabled=false",
    "app.features.fraud.enabled=false"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    InventoryControllerTest.ControllerTestConfiguration.class
})
@DisplayName("Inventory Controller Tests")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private JwtPrincipal mockPrincipal;
    private InventoryResponse inventoryResponse;
    private InventoryCreateRequest createRequest;
    private InventoryAdjustmentRequest adjustmentRequest;

    @BeforeEach
    void setUp() {
        mockPrincipal = JwtPrincipal.builder()
            .subject("test-user")
            .preferredUsername("test@example.com")
            .email("test@example.com")
            .build();

        inventoryResponse = InventoryResponse.builder()
            .id("inventory-1")
            .shopId("shop-1")
            .shopName("Test Shop")
            .productId("product-1")
            .productName("Test Product")
            .productSku("TEST-001")
            .currentStock(100)
            .reservedStock(10)
            .availableStock(90)
            .minimumStock(20)
            .maximumStock(200)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(25.50))
            .location("A1-B2")
            .batchNumber("BATCH001")
            .expiryDate(LocalDate.now().plusMonths(6))
            .status(Inventory.InventoryStatus.ACTIVE)
            .lastStockUpdate(LocalDateTime.now())
            .isLowStock(false)
            .isExpired(false)
            .isExpiringSoon(false)
            .build();

        createRequest = InventoryCreateRequest.builder()
            .productId("product-1")
            .currentStock(100)
            .minimumStock(20)
            .maximumStock(200)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(25.50))
            .location("A1-B2")
            .batchNumber("BATCH001")
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();

        adjustmentRequest = InventoryAdjustmentRequest.builder()
            .newStock(150)
            .reason("Stock adjustment for testing")
            .build();
    }

    @Test
    @DisplayName("Should create inventory item successfully")
    void shouldCreateInventorySuccessfully() throws Exception {
        when(inventoryService.createInventory(any(InventoryCreateRequest.class)))
            .thenReturn(inventoryResponse);

        ResultActions result = mockMvc.perform(post("/api/shops/shop-1/inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest))
            .with(withJwtPrincipal("manager", "MANAGER")));

        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("inventory-1"))
            .andExpect(jsonPath("$.productName").value("Test Product"))
            .andExpect(jsonPath("$.currentStock").value(100));

        verify(inventoryService).createInventory(any(InventoryCreateRequest.class));
    }

    @Test
    @DisplayName("Should return validation errors for invalid create request")
    void shouldReturnValidationErrorsForInvalidCreateRequest() throws Exception {
        InventoryCreateRequest invalidRequest = InventoryCreateRequest.builder()
            .currentStock(-5)
            .minimumStock(-10)
            .build();

        ResultActions result = mockMvc.perform(post("/api/shops/shop-1/inventory")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest))
            .with(withJwtPrincipal("manager", "MANAGER")));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get inventory items with pagination")
    void shouldGetInventoryWithPagination() throws Exception {
        Page<InventoryResponse> inventoryPage = new PageImpl<>(
            Arrays.asList(inventoryResponse),
            PageRequest.of(0, 20),
            1
        );

        when(inventoryService.getInventory(anyString(), any(Specification.class), any(Pageable.class)))
            .thenReturn(inventoryPage);

        ResultActions result = mockMvc.perform(get("/api/shops/shop-1/inventory")
            .param("page", "0")
            .param("size", "20")
            .param("sortBy", "lastStockUpdate")
            .param("sortDir", "desc")
            .with(withJwtPrincipal("manager", "MANAGER")));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value("inventory-1"))
            .andExpect(jsonPath("$.content[0].productName").value("Test Product"))
            .andExpect(jsonPath("$.totalElements").value(1));

        verify(inventoryService).getInventory(anyString(), any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get inventory item by ID")
    void shouldGetInventoryById() throws Exception {
        when(inventoryService.getInventoryById("inventory-1"))
            .thenReturn(inventoryResponse);

        ResultActions result = mockMvc.perform(get("/api/inventory/inventory-1")
            .with(withJwtPrincipal("manager", "MANAGER")));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("inventory-1"))
            .andExpect(jsonPath("$.productName").value("Test Product"));

        verify(inventoryService).getInventoryById("inventory-1");
    }

    @Test
    @DisplayName("Should adjust stock successfully")
    void shouldAdjustStockSuccessfully() throws Exception {
        InventoryResponse adjustedResponse = InventoryResponse.builder()
            .id(inventoryResponse.getId())
            .shopId(inventoryResponse.getShopId())
            .shopName(inventoryResponse.getShopName())
            .productId(inventoryResponse.getProductId())
            .productName(inventoryResponse.getProductName())
            .productSku(inventoryResponse.getProductSku())
            .currentStock(150)
            .reservedStock(inventoryResponse.getReservedStock())
            .availableStock(140)
            .minimumStock(inventoryResponse.getMinimumStock())
            .maximumStock(inventoryResponse.getMaximumStock())
            .reorderPoint(inventoryResponse.getReorderPoint())
            .unitCost(inventoryResponse.getUnitCost())
            .location(inventoryResponse.getLocation())
            .batchNumber(inventoryResponse.getBatchNumber())
            .expiryDate(inventoryResponse.getExpiryDate())
            .status(inventoryResponse.getStatus())
            .lastStockUpdate(inventoryResponse.getLastStockUpdate())
            .isLowStock(inventoryResponse.isLowStock())
            .isExpired(inventoryResponse.isExpired())
            .isExpiringSoon(inventoryResponse.isExpiringSoon())
            .build();

        when(inventoryService.adjustStock("inventory-1", adjustmentRequest))
            .thenReturn(adjustedResponse);

        ResultActions result = mockMvc.perform(put("/api/inventory/inventory-1/adjust-stock")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(adjustmentRequest))
            .with(withJwtPrincipal("manager", "MANAGER")));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.currentStock").value(150))
            .andExpect(jsonPath("$.availableStock").value(140));

        verify(inventoryService).adjustStock("inventory-1", adjustmentRequest);
    }

    @Test
    @DisplayName("Should get inventory summary successfully")
    void shouldGetInventorySummarySuccessfully() throws Exception {
        InventorySummaryDto summary = InventorySummaryDto.builder()
            .totalItems(50)
            .totalValue(BigDecimal.valueOf(25000.00))
            .lowStockItems(5)
            .expiredItems(2)
            .expiringSoonItems(3)
            .categoryBreakdown(Arrays.asList(
                InventorySummaryDto.CategoryBreakdown.builder()
                    .category("Electronics")
                    .itemCount(20)
                    .totalValue(BigDecimal.valueOf(15000.00))
                    .lowStockCount(2)
                    .build()
            ))
            .build();

        when(inventoryService.getInventorySummary("shop-1"))
            .thenReturn(summary);

        ResultActions result = mockMvc.perform(get("/api/shops/shop-1/inventory/summary")
            .with(withJwtPrincipal("manager", "MANAGER")));

        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(50))
            .andExpect(jsonPath("$.totalValue").value(25000.00))
            .andExpect(jsonPath("$.lowStockItems").value(5))
            .andExpect(jsonPath("$.categoryBreakdown[0].category").value("Electronics"));

        verify(inventoryService).getInventorySummary("shop-1");
    }

    @Test
    @DisplayName("Should deny access for unauthorized user")
    @WithMockPermissions(role = "CUSTOMER")
    void shouldDenyAccessForUnauthorizedUser() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/shops/shop-1/inventory")
            .with(withJwtPrincipal("customer", "CUSTOMER")));

        result.andExpect(status().isForbidden());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor withJwtPrincipal(String username, String... roles) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(
            new UsernamePasswordAuthenticationToken(
                JwtPrincipal.builder()
                    .subject("test-user-id")
                    .preferredUsername(username)
                    .email(username + "@example.com")
                    .firstName(username)
                    .lastName("User")
                    .roles(List.of(roles))
                    .build(),
                "password",
                List.of(roles).stream().map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)).toList()
            )
        );
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public InventoryController inventoryController(InventoryService inventoryService) {
            return new InventoryController(inventoryService);
        }
    }
}