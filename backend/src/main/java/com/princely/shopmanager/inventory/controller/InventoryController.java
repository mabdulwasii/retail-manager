package com.princely.shopmanager.inventory.controller;

import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.domain.InventoryHistory;
import com.princely.shopmanager.inventory.dto.InventoryAdjustmentRequest;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.dto.InventorySummaryDto;
import com.princely.shopmanager.inventory.dto.InventoryUpdateRequest;
import com.princely.shopmanager.inventory.dto.StockReservationRequest;
import com.princely.shopmanager.inventory.specification.InventorySpecifications;
import com.princely.shopmanager.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for inventory management operations.
 * Manages stock levels, batch tracking, expiry dates, and inventory movements.
 *
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory and stock management operations")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
        summary = "Create new inventory item",
        description = "Create a new inventory item for a product in the shop"
    )
    @ApiResponse(responseCode = "201", description = "Inventory item created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping("/shops/{shopId}/inventory")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_CREATE)")
    public ResponseEntity<InventoryResponse> createInventory(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Valid @RequestBody InventoryCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating inventory item for shop: {}, product: {}, user: {}",
                shopId, request.getProductId(), principal.getUsername());

        InventoryResponse response = inventoryService.createInventory(shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get inventory items",
        description = "Retrieve inventory items for a shop with filtering and pagination"
    )
    @ApiResponse(responseCode = "200", description = "Inventory items retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/inventory")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_LIST)")
    public ResponseEntity<Page<InventoryResponse>> getInventory(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Search query for product name or SKU") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status") @RequestParam(required = false) Inventory.InventoryStatus status,
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Filter by location") @RequestParam(required = false) String location,
            @Parameter(description = "Filter low stock items only") @RequestParam(required = false) Boolean lowStock,
            @Parameter(description = "Filter expired items only") @RequestParam(required = false) Boolean expired,
            @Parameter(description = "Filter expiring soon items only") @RequestParam(required = false) Boolean expiringSoon,
            @Parameter(description = "Minimum stock filter") @RequestParam(required = false) Integer minStock,
            @Parameter(description = "Maximum stock filter") @RequestParam(required = false) Integer maxStock,
            @Parameter(description = "Days threshold for expiring soon") @RequestParam(defaultValue = "30") int expiringDays,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "lastStockUpdate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        // Build specification based on filters
        Specification<Inventory> spec = InventorySpecifications.forShop(shopId);

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and(InventorySpecifications.productNameContains(search));
        }

        if (status != null) {
            spec = spec.and(InventorySpecifications.hasStatus(status));
        }

        if (location != null && !location.trim().isEmpty()) {
            spec = spec.and(InventorySpecifications.atLocation(location));
        }

        if (Boolean.TRUE.equals(lowStock)) {
            spec = spec.and(InventorySpecifications.hasLowStock());
        }

        if (Boolean.TRUE.equals(expired)) {
            spec = spec.and(InventorySpecifications.isExpired());
        }

        if (Boolean.TRUE.equals(expiringSoon)) {
            spec = spec.and(InventorySpecifications.expiresWithinDays(expiringDays));
        }

        if (minStock != null) {
            spec = spec.and(InventorySpecifications.hasStockAvailable(minStock));
        }

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InventoryResponse> response = inventoryService.getInventory(shopId, spec, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get inventory item by ID",
        description = "Retrieve a specific inventory item by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Inventory item retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @GetMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_READ)")
    public ResponseEntity<InventoryResponse> getInventoryById(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InventoryResponse response = inventoryService.getInventoryById(inventoryId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Adjust stock level",
        description = "Adjust the stock level of an inventory item"
    )
    @ApiResponse(responseCode = "200", description = "Stock adjusted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @PutMapping("/inventory/{inventoryId}/adjust-stock")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_ADJUST)")
    public ResponseEntity<InventoryResponse> adjustStock(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @Valid @RequestBody InventoryAdjustmentRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Adjusting stock for inventory: {}, new stock: {}, reason: {}, user: {}",
                inventoryId, request.getNewStock(), request.getReason(), principal.getUsername());

        InventoryResponse response = inventoryService.adjustStock(inventoryId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Reserve stock",
        description = "Reserve stock for a sale or other operation"
    )
    @ApiResponse(responseCode = "200", description = "Stock reserved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or insufficient stock")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @PostMapping("/inventory/{inventoryId}/reserve")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_RESERVE)")
    public ResponseEntity<Void> reserveStock(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @Valid @RequestBody StockReservationRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Reserving stock for inventory: {}, quantity: {}, user: {}",
                inventoryId, request.getQuantity(), principal.getUsername());

        request.setInventoryId(inventoryId);
        inventoryService.reserveStock(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Release reserved stock",
        description = "Release previously reserved stock"
    )
    @ApiResponse(responseCode = "200", description = "Reserved stock released successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @PostMapping("/inventory/{inventoryId}/release")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_RESERVE)")
    public ResponseEntity<Void> releaseReservedStock(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @Parameter(description = "Quantity to release") @RequestParam int quantity,
            @Parameter(description = "Reference ID (e.g., sale ID)") @RequestParam(required = false) String referenceId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Releasing reserved stock for inventory: {}, quantity: {}, user: {}",
                inventoryId, quantity, principal.getUsername());

        inventoryService.releaseReservedStock(inventoryId, quantity, referenceId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Update inventory status",
        description = "Update the status of an inventory item (e.g., activate, deactivate, quarantine)"
    )
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @PutMapping("/inventory/{inventoryId}/status")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_UPDATE)")
    public ResponseEntity<InventoryResponse> updateInventoryStatus(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @Parameter(description = "New status") @RequestParam Inventory.InventoryStatus status,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating inventory status: {}, new status: {}, user: {}",
                inventoryId, status, principal.getUsername());

        InventoryResponse response = inventoryService.updateInventoryStatus(inventoryId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update inventory metadata",
        description = "Update inventory information such as batch number, location, expiry date, and stock thresholds. Does NOT update stock quantities - use adjust-stock endpoint for that."
    )
    @ApiResponse(responseCode = "200", description = "Inventory updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @PutMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_UPDATE)")
    public ResponseEntity<InventoryResponse> updateInventory(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @Valid @RequestBody InventoryUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating inventory metadata: {}, user: {}", inventoryId, principal.getUsername());

        InventoryResponse response = inventoryService.updateInventory(inventoryId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete inventory item",
        description = "Delete an inventory item. Can only delete items with zero current stock and zero reserved stock."
    )
    @ApiResponse(responseCode = "204", description = "Inventory deleted successfully")
    @ApiResponse(responseCode = "400", description = "Cannot delete inventory with active or reserved stock")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @DeleteMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_DELETE)")
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Deleting inventory: {}, user: {}", inventoryId, principal.getUsername());

        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get inventory history",
        description = "Retrieve the complete history of changes for an inventory item"
    )
    @ApiResponse(responseCode = "200", description = "History retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Inventory item not found")
    @GetMapping("/inventory/{inventoryId}/history")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_HISTORY)")
    public ResponseEntity<List<InventoryHistory>> getInventoryHistory(
            @Parameter(description = "Inventory ID") @PathVariable String inventoryId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        List<InventoryHistory> history = inventoryService.getInventoryHistory(inventoryId);
        return ResponseEntity.ok(history);
    }

    @Operation(
        summary = "Get low stock items",
        description = "Retrieve all items with low stock levels for a shop"
    )
    @ApiResponse(responseCode = "200", description = "Low stock items retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/inventory/low-stock")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_LIST)")
    public ResponseEntity<List<InventoryResponse>> getLowStockItems(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        List<InventoryResponse> response = inventoryService.getLowStockItems(shopId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get expiring items",
        description = "Retrieve items that are expiring within the specified number of days"
    )
    @ApiResponse(responseCode = "200", description = "Expiring items retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/inventory/expiring")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_LIST)")
    public ResponseEntity<List<InventoryResponse>> getExpiringItems(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Days threshold for expiry warning") @RequestParam(defaultValue = "30") int daysThreshold,
            @AuthenticationPrincipal JwtPrincipal principal) {

        List<InventoryResponse> response = inventoryService.getExpiringItems(shopId, daysThreshold);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get total inventory value",
        description = "Calculate the total value of all inventory in a shop"
    )
    @ApiResponse(responseCode = "200", description = "Total inventory value calculated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/inventory/total-value")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_READ)")
    public ResponseEntity<BigDecimal> getTotalInventoryValue(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        BigDecimal totalValue = inventoryService.getTotalInventoryValue(shopId);
        return ResponseEntity.ok(totalValue);
    }

    @Operation(
        summary = "Trigger demand forecasting",
        description = "Trigger demand forecasting analysis for a specific product"
    )
    @ApiResponse(responseCode = "200", description = "Demand forecasting triggered successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PostMapping("/products/{productId}/forecast")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_FORECAST)")
    public ResponseEntity<Void> forecastDemand(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Forecast period in days") @RequestParam(defaultValue = "30") int forecastDays,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Triggering demand forecast for product: {}, forecast days: {}, user: {}",
                productId, forecastDays, principal.getUsername());

        inventoryService.forecastDemand(productId, forecastDays);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get inventory summary",
        description = "Get inventory summary and statistics for a shop"
    )
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/inventory/summary")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_READ)")
    public ResponseEntity<InventorySummaryDto> getInventorySummary(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InventorySummaryDto response = inventoryService.getInventorySummary(shopId);
        return ResponseEntity.ok(response);
    }
}