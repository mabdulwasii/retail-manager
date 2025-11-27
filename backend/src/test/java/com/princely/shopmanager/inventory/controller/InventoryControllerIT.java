package com.princely.shopmanager.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryAdjustmentRequest;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryUpdateRequest;
import com.princely.shopmanager.inventory.dto.StockReservationRequest;
import com.princely.shopmanager.test.TestConstants;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for InventoryController.
 * Tests granular permission-based authorization for inventory management.
 */
@DisplayName("Inventory Controller Integration Tests")
class InventoryControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============== Create Inventory Tests ==============

    @Test
    @DisplayName("OWNER should create inventory")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldCreateInventory() throws Exception {
        // First create a product
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
            .name("Inventory Test Product")
            .REMOVED_PRICE(BigDecimal.valueOf(50.00))
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String productResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(productResponse).get("id").asText();

        // Create inventory
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(100)
            .batchNumber("BATCH-001")
            .expiryDate(LocalDate.now().plusMonths(12))
            .location("Warehouse A")
            .costPrice(BigDecimal.valueOf(40.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.currentStock").value(100))
            .andExpect(jsonPath("$.batchNumber").value("BATCH-001"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("MANAGER should create inventory")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldCreateInventory() throws Exception {
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
            .name("Manager Inventory Product")
            .REMOVED_PRICE(BigDecimal.valueOf(30.00))
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String productResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(productResponse).get("id").asText();

        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(50)
            .batchNumber("MGR-BATCH-001")
            .expiryDate(LocalDate.now().plusMonths(6))
            .costPrice(BigDecimal.valueOf(25.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("EMPLOYEE should create inventory")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldCreateInventory() throws Exception {
        // Employees can create inventory per permission matrix
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(TestConstants.PROD_WIRELESS_MOUSE)
            .currentStock(20)
            .batchNumber("EMP-BATCH-001")
            .costPrice(BigDecimal.valueOf(15.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    // ============== List Inventory Tests ==============

    @Test
    @DisplayName("OWNER should list inventory")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldListInventory() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(Iterable.class)));
    }

    @Test
    @DisplayName("MANAGER should list inventory")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldListInventory() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should list inventory")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldListInventory() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User without INVENTORY_LIST permission should NOT list inventory")
    @WithMockPermissions(value = {"PRODUCT_READ"})
    void userWithoutPermissionShouldNotListInventory() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001))
            .andExpect(status().isForbidden());
    }

    // ============== Inventory Adjustment Tests ==============

    @Test
    @DisplayName("OWNER should adjust inventory")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldAdjustInventory() throws Exception {
        // Create inventory first
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
            .name("Adjustment Test Product")
            .REMOVED_PRICE(BigDecimal.valueOf(60.00))
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String productResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(productResponse).get("id").asText();

        InventoryCreateRequest createRequest = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(100)
            .batchNumber("ADJ-BATCH-001")
            .costPrice(BigDecimal.valueOf(50.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        String inventoryResponse = mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String inventoryId = objectMapper.readTree(inventoryResponse).get("id").asText();

        // Adjust inventory
        InventoryAdjustmentRequest adjustmentRequest = InventoryAdjustmentRequest.builder()
            .newStock(-10)
            .reason("Damaged items")
            .build();

        mockMvc.perform(post("/api/inventory/{inventoryId}/adjust", inventoryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentStock").value(90));
    }

    @Test
    @DisplayName("MANAGER should adjust inventory")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldAdjustInventory() throws Exception {
        InventoryAdjustmentRequest adjustmentRequest = InventoryAdjustmentRequest.builder()
            .newStock(5)
            .reason("Found stock")
            .build();

        // Will return 404 as we don't have inventory, but proves permission is granted
        mockMvc.perform(post("/api/inventory/{inventoryId}/adjust", "any-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("EMPLOYEE should NOT adjust inventory")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotAdjustInventory() throws Exception {
        InventoryAdjustmentRequest adjustmentRequest = InventoryAdjustmentRequest.builder()
            .newStock(5)
            .reason("Test")
            .build();

        mockMvc.perform(post("/api/inventory/{inventoryId}/adjust", "any-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustmentRequest)))
            .andExpect(status().isForbidden());
    }

    // ============== Reserve Stock Tests ==============

    @Test
    @DisplayName("OWNER should reserve stock")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldReserveStock() throws Exception {
        StockReservationRequest reservationRequest = StockReservationRequest.builder()
            .inventoryId(TestConstants.PROD_WIRELESS_MOUSE)
            .quantity(10)
            .reason("Sales order")
            .build();

        // Will return 404/400 but proves permission is granted
        mockMvc.perform(post("/api/shops/{shopId}/inventory/reserve", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("MANAGER should reserve stock")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldReserveStock() throws Exception {
        StockReservationRequest reservationRequest = StockReservationRequest.builder()
            .inventoryId(TestConstants.PROD_WIRELESS_MOUSE)
            .quantity(5)
            .reason("Test reservation")
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/inventory/reserve", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("EMPLOYEE should NOT reserve stock")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotReserveStock() throws Exception {
        StockReservationRequest reservationRequest = StockReservationRequest.builder()
            .inventoryId(TestConstants.PROD_WIRELESS_MOUSE)
            .quantity(5)
            .reason("Test")
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/inventory/reserve", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservationRequest)))
            .andExpect(status().isForbidden());
    }

    // ============== Update Inventory Tests ==============

    @Test
    @DisplayName("OWNER should update inventory metadata")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldUpdateInventoryMetadata() throws Exception {
        // Create product and inventory first
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
            .name("Update Test Product")
            .REMOVED_PRICE(BigDecimal.valueOf(45.00))
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String productResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(productResponse).get("id").asText();

        InventoryCreateRequest createRequest = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(0)  // Zero stock so we can delete later
            .batchNumber("OLD-BATCH")
            .location("Old Location")
            .costPrice(BigDecimal.valueOf(35.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .minimumStock(5)
            .build();

        String inventoryResponse = mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String inventoryId = objectMapper.readTree(inventoryResponse).get("id").asText();

        // Update inventory metadata
        InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
            .batchNumber("NEW-BATCH")
            .location("New Location")
            .expiryDate(LocalDate.now().plusMonths(12))
            .minimumStock(10)
            .maximumStock(500)
            .reorderPoint(30)
            .costPrice(BigDecimal.valueOf(38.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        mockMvc.perform(put("/api/inventory/{inventoryId}", inventoryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(inventoryId));
    }

    @Test
    @DisplayName("MANAGER should update inventory metadata")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldUpdateInventoryMetadata() throws Exception {
        InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
            .location("Manager Updated Location")
            .minimumStock(15)
            .build();

        mockMvc.perform(put("/api/inventory/{inventoryId}", "any-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("EMPLOYEE should NOT update inventory metadata")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotUpdateInventoryMetadata() throws Exception {
        InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
            .location("Test Location")
            .build();

        mockMvc.perform(put("/api/inventory/{inventoryId}", "any-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    // ============== Delete Inventory Tests ==============

    @Test
    @DisplayName("OWNER should delete inventory with zero stock")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldDeleteInventoryWithZeroStock() throws Exception {
        // Create product and inventory with zero stock
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
            .name("Delete Test Product")
            .REMOVED_PRICE(BigDecimal.valueOf(25.00))
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String productResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(productResponse).get("id").asText();

        InventoryCreateRequest createRequest = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(0)  // Zero stock so we can delete
            .batchNumber("DEL-BATCH")
            .costPrice(BigDecimal.valueOf(20.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        String inventoryResponse = mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String inventoryId = objectMapper.readTree(inventoryResponse).get("id").asText();

        // Delete inventory
        mockMvc.perform(delete("/api/inventory/{inventoryId}", inventoryId)
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("OWNER should NOT delete inventory with active stock")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldNotDeleteInventoryWithActiveStock() throws Exception {
        // Create product and inventory with stock
        ProductCreateRequest productRequest = ProductCreateRequest.builder()
            .name("Active Stock Product")
            .REMOVED_PRICE(BigDecimal.valueOf(35.00))
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String productResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(productResponse).get("id").asText();

        InventoryCreateRequest createRequest = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(50)  // Has stock
            .batchNumber("STOCK-BATCH")
            .costPrice(BigDecimal.valueOf(30.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        String inventoryResponse = mockMvc.perform(post("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String inventoryId = objectMapper.readTree(inventoryResponse).get("id").asText();

        // Try to delete inventory with stock
        mockMvc.perform(delete("/api/inventory/{inventoryId}", inventoryId)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("MANAGER should delete inventory")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldDeleteInventory() throws Exception {
        mockMvc.perform(delete("/api/inventory/{inventoryId}", "any-id")
                .with(csrf()))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("EMPLOYEE should NOT delete inventory")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotDeleteInventory() throws Exception {
        mockMvc.perform(delete("/api/inventory/{inventoryId}", "any-id")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    // ============== Filter Tests ==============

    @Test
    @DisplayName("Should filter inventory by status")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterInventoryByStatus() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .param("status", "ACTIVE"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should filter inventory by location")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterInventoryByLocation() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .param("location", "Warehouse A"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should filter low stock items")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterLowStockItems() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .param("lowStock", "true"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should filter expired items")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterExpiredItems() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .param("expired", "true"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should filter expiring soon items")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterExpiringSoonItems() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/inventory", TestConstants.TEST_SHOP_001)
                .param("expiringSoon", "true")
                .param("expiringDays", "30"))
            .andExpect(status().isOk());
    }

    // ============== Inventory History Tests ==============

    @Test
    @DisplayName("OWNER should view inventory history")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldViewInventoryHistory() throws Exception {
        mockMvc.perform(get("/api/inventory/{inventoryId}/history", "any-id"))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("MANAGER should view inventory history")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldViewInventoryHistory() throws Exception {
        mockMvc.perform(get("/api/inventory/{inventoryId}/history", "any-id"))
            .andExpect(status().isNotFound()); // 404 not 403
    }

    @Test
    @DisplayName("EMPLOYEE should view inventory history")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldViewInventoryHistory() throws Exception {
        mockMvc.perform(get("/api/inventory/{inventoryId}/history", "any-id"))
            .andExpect(status().isNotFound()); // 404 not 403, employees can view history
    }
}
