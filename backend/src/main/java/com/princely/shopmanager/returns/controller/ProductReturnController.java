package com.princely.shopmanager.returns.controller;

import com.princely.shopmanager.returns.dto.ProductReturnCreateRequest;
import com.princely.shopmanager.returns.dto.ProductReturnResponse;
import com.princely.shopmanager.returns.service.ProductReturnService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for product return management operations.
 *
 * This controller provides comprehensive product return management endpoints including:
 * - Creating product returns with proper validation
 * - Processing returns with inventory integration
 * - Retrieving return history and status
 * - Multi-tenant access control and isolation
 *
 * All endpoints are secured and require appropriate authentication and authorization.
 * Operations respect tenant boundaries and include comprehensive audit logging.
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 *
 * @author Shop Manager Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/shops/{shopId}/returns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Returns", description = "Operations for managing product returns and refunds")
@SecurityRequirement(name = "bearerAuth")
public class ProductReturnController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ProductReturnService productReturnService;

    /**
     * Creates a new product return in the system.
     *
     * This endpoint allows authorized users to create new product returns with proper validation
     * and automatic processing workflows. Only users with appropriate roles can create returns.
     *
     * @param shopId Shop identifier for tenant isolation
     * @param request Product return creation request with validation
     * @return Created product return information
     */
    @Operation(
        summary = "Create a new product return",
        description = "Creates a new product return with validation and processing workflows. Requires appropriate permissions.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Product return creation details",
            required = true,
            content = @Content(schema = @Schema(implementation = ProductReturnCreateRequest.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product return created successfully",
            content = @Content(schema = @Schema(implementation = ProductReturnResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or business rule violation",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires MANAGER or CASHIER role"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or access denied"
        )
    })
    @PostMapping
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RETURN_CREATE)")
    public ResponseEntity<ProductReturnResponse> createReturn(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Valid @RequestBody ProductReturnCreateRequest request,
        @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("Creating product return for shop: {}, quantity: {}", shopId, request.getQuantityReturned());
        ProductReturnResponse response = productReturnService.createReturn(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Processes a product return by ID.
     *
     * This endpoint processes a pending return, handling inventory restocking,
     * refund calculations, and status updates. Only authorized staff can process returns.
     *
     * @param shopId Shop identifier for tenant isolation
     * @param returnId Unique product return identifier
     * @return Processed product return information
     */
    @Operation(
        summary = "Process a product return",
        description = "Processes a pending product return, handling inventory and refund calculations. Requires MANAGER or higher permissions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product return processed successfully",
            content = @Content(schema = @Schema(implementation = ProductReturnResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Return cannot be processed in current state"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires MANAGER or higher role"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Return not found or access denied"
        )
    })
    @PostMapping("/{returnId}/process")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RETURN_APPROVE)")
    public ResponseEntity<ProductReturnResponse> processReturn(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Parameter(description = "Return ID", example = "return-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String returnId,
        @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("Processing product return: {} for shop: {}", returnId, shopId);
        ProductReturnResponse response = productReturnService.processReturn(returnId, principal);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all product returns for a shop with pagination.
     *
     * Returns paginated list of product returns for the specified shop.
     * Tenant isolation is enforced - users can only access returns within their tenant.
     *
     * @param shopId Shop identifier for tenant isolation
     * @param pageable Pagination parameters
     * @return Paginated list of product returns
     */
    @Operation(
        summary = "Get product returns for shop",
        description = "Retrieves paginated list of product returns for a specific shop. Access is restricted by tenant isolation."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product returns retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or access denied"
        )
    })
    @GetMapping
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RETURN_LIST)")
    public ResponseEntity<Page<ProductReturnResponse>> getReturns(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable,
        @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.debug("Retrieving product returns for shop: {}, page: {}, size: {}",
                 shopId, pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductReturnResponse> returns = productReturnService.getReturns(shopId, pageable, principal);
        return ResponseEntity.ok(returns);
    }
}