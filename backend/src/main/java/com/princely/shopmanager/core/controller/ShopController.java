package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.core.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for shop management operations.
 *
 * This controller provides comprehensive shop management endpoints including:
 * - CRUD operations for shops with proper validation
 * - Multi-tenant access control and isolation
 * - Status management and business rule enforcement
 * - Pagination support for listing operations
 *
 * All endpoints are secured and require appropriate authentication and authorization.
 * Operations respect tenant boundaries and include comprehensive audit logging.
 *
 * @author Shop Manager Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shop Management", description = "Operations for managing shops and multi-tenant functionality")
@SecurityRequirement(name = "bearerAuth")
public class ShopController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ShopService shopService;

    /**
     * Creates a new shop in the system.
     *
     * This endpoint allows authorized users to create new shops with proper validation
     * and automatic tenant ID generation. Only users with SYSTEM_ADMIN or SHOP_OWNER
     * roles can create new shops.
     *
     * @param request Shop creation request with validation
     * @return Created shop information
     */
    @Operation(
        summary = "Create a new shop",
        description = "Creates a new shop with automatic tenant ID generation. Requires SYSTEM_ADMIN or SHOP_OWNER role.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Shop creation details",
            required = true,
            content = @Content(schema = @Schema(implementation = ShopCreateRequest.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Shop created successfully",
            content = @Content(schema = @Schema(implementation = ShopResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or shop name already exists",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SYSTEM_ADMIN or SHOP_OWNER role"
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER')")
    public ResponseEntity<ShopResponse> createShop(
        @Valid @RequestBody ShopCreateRequest request
    ) {
        log.info("Creating shop: {}", request.getName());
        ShopResponse response = shopService.createShop(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a specific shop by ID.
     *
     * Returns shop details if the shop exists and the user has access rights.
     * Tenant isolation is enforced - users can only access shops within their tenant.
     *
     * @param shopId Unique shop identifier
     * @return Shop details
     */
    @Operation(
        summary = "Get shop by ID",
        description = "Retrieves detailed information about a specific shop. Access is restricted by tenant isolation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Shop found and returned",
            content = @Content(schema = @Schema(implementation = ShopResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or access denied",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/{shopId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER') or hasRole('SHOP_MANAGER') or hasRole('CASHIER')")
    public ResponseEntity<ShopResponse> getShop(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId
    ) {
        log.debug("Retrieving shop: {}", shopId);
        ShopResponse response = shopService.getShop(shopId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a paginated list of shops.
     *
     * Returns shops accessible to the current user:
     * - System admins can see all shops
     * - Tenant users can only see shops within their tenant
     *
     * @param pageable Pagination parameters
     * @return Paginated list of shops
     */
    @Operation(
        summary = "Get shops with pagination",
        description = "Retrieves a paginated list of shops. System admins see all shops, tenant users see only their tenant's shops."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Shops retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<Page<ShopResponse>> getShops(
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "name")
        @Parameter(description = "Pagination parameters (page, size, sort)")
        Pageable pageable
    ) {
        log.debug("Retrieving shops with pagination: {}", pageable);
        Page<ShopResponse> response = shopService.getShops(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all active shops for the current tenant.
     *
     * This endpoint is optimized for dropdown lists and quick selections.
     * Returns only shops with ACTIVE status.
     *
     * @return List of active shops
     */
    @Operation(
        summary = "Get active shops",
        description = "Retrieves all active shops for the current tenant. Useful for dropdown lists and quick selections."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active shops retrieved successfully",
            content = @Content(schema = @Schema(implementation = List.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/active")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER') or hasRole('SHOP_MANAGER') or hasRole('CASHIER')")
    public ResponseEntity<List<ShopResponse>> getActiveShops() {
        log.debug("Retrieving active shops");
        List<ShopResponse> response = shopService.getActiveShops();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing shop's information.
     *
     * Supports partial updates - only non-null fields in the request will be updated.
     * Maintains audit trail of all changes and enforces business rules.
     *
     * @param shopId Shop ID to update
     * @param request Update request with optional fields
     * @return Updated shop information
     */
    @Operation(
        summary = "Update shop information",
        description = "Updates shop information with partial update support. Only non-null fields are updated. Requires SHOP_OWNER or SHOP_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Shop updated successfully",
            content = @Content(schema = @Schema(implementation = ShopResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or access denied",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SHOP_OWNER or SHOP_MANAGER role"
        )
    })
    @PutMapping("/{shopId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER') or hasRole('SHOP_MANAGER')")
    public ResponseEntity<ShopResponse> updateShop(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Valid @RequestBody ShopUpdateRequest request
    ) {
        log.info("Updating shop: {}", shopId);
        ShopResponse response = shopService.updateShop(shopId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Changes the status of a shop.
     *
     * Allows changing shop status (ACTIVE, INACTIVE, SUSPENDED, CLOSED) with
     * proper validation of status transitions and business rules.
     *
     * @param shopId Shop ID to update
     * @param status New status to apply
     * @return Updated shop information
     */
    @Operation(
        summary = "Change shop status",
        description = "Changes the status of a shop with validation of status transitions. Requires SHOP_OWNER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Shop status updated successfully",
            content = @Content(schema = @Schema(implementation = ShopResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid status transition",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or access denied",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SHOP_OWNER role"
        )
    })
    @PatchMapping("/{shopId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER')")
    public ResponseEntity<ShopResponse> changeShopStatus(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Parameter(description = "New shop status", example = "INACTIVE")
        @RequestParam Shop.ShopStatus status
    ) {
        log.info("Changing status of shop {} to {}", shopId, status);
        ShopResponse response = shopService.changeShopStatus(shopId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Soft deletes a shop by setting its status to CLOSED.
     *
     * This operation preserves historical data and relationships while marking
     * the shop as no longer active. Physical deletion is not supported.
     *
     * @param shopId Shop ID to delete
     * @return No content response
     */
    @Operation(
        summary = "Delete shop (soft delete)",
        description = "Soft deletes a shop by setting status to CLOSED. Preserves historical data. Requires SHOP_OWNER role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Shop deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or access denied",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SHOP_OWNER role"
        )
    })
    @DeleteMapping("/{shopId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SHOP_OWNER')")
    public ResponseEntity<Void> deleteShop(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId
    ) {
        log.info("Deleting shop: {}", shopId);
        shopService.deleteShop(shopId);
        return ResponseEntity.noContent().build();
    }
}