package com.princely.shopmanager.sales.controller;

import com.princely.shopmanager.sales.dto.SalesTransactionCreateRequest;
import com.princely.shopmanager.sales.dto.SalesTransactionResponse;
import com.princely.shopmanager.sales.service.SalesTransactionService;
import com.princely.shopmanager.shared.constants.PermissionConstants;

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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for sales transaction management.
 * Sales transactions represent completed purchases with automatic FEFO inventory deduction.
 *
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Transactions", description = "Operations for managing sales transactions")
@SecurityRequirement(name = "bearerAuth")
public class SalesTransactionController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final SalesTransactionService salesTransactionService;

    @Operation(
        summary = "Create a new sales transaction",
        description = "Creates a new sales transaction with automatic FEFO inventory deduction."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sales transaction created successfully",
            content = @Content(schema = @Schema(implementation = SalesTransactionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).SALES_CREATE)")
    public ResponseEntity<SalesTransactionResponse> createTransaction(
        @Valid @RequestBody SalesTransactionCreateRequest request
    ) {
        log.info("Creating sales transaction for shop: {}", request.getShopId());
        SalesTransactionResponse response = salesTransactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get a sales transaction by ID",
        description = "Retrieves detailed information about a specific sales transaction"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sales transaction found and returned",
            content = @Content(schema = @Schema(implementation = SalesTransactionResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sales transaction not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).SALES_READ)")
    public ResponseEntity<SalesTransactionResponse> getTransaction(
        @Parameter(description = "Transaction ID", example = "txn-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String id
    ) {
        log.debug("Retrieving sales transaction: {}", id);
        SalesTransactionResponse response = salesTransactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get paginated sales transactions",
        description = "Retrieves a paginated list of sales transactions for a shop"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sales transactions retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).SALES_LIST)")
    public ResponseEntity<Page<SalesTransactionResponse>> getTransactions(
        @Parameter(description = "Shop ID", required = true)
        @RequestParam String shopId,
        @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable
    ) {
        log.debug("Retrieving sales transactions for shop: {}", shopId);
        Page<SalesTransactionResponse> response = salesTransactionService.getTransactions(shopId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get sales transactions by date range",
        description = "Retrieves sales transactions for a shop within a specified date range"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sales transactions retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/by-date-range")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).SALES_LIST)")
    public ResponseEntity<List<SalesTransactionResponse>> getTransactionsByDateRange(
        @Parameter(description = "Shop ID", required = true)
        @RequestParam String shopId,
        @Parameter(description = "Start date (ISO format)", example = "2025-01-01T00:00:00")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @Parameter(description = "End date (ISO format)", example = "2025-12-31T23:59:59")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.debug("Retrieving transactions for shop {} between {} and {}", shopId, startDate, endDate);
        List<SalesTransactionResponse> response = salesTransactionService.getTransactionsByDateRange(shopId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Void a sales transaction",
        description = "Voids a sales transaction and restores inventory."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Transaction voided successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Transaction not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping("/{id}/void")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).SALES_VOID)")
    public ResponseEntity<Void> voidTransaction(
        @Parameter(description = "Transaction ID")
        @PathVariable String id,
        @Parameter(description = "Reason for voiding the transaction")
        @RequestParam String reason
    ) {
        log.info("Voiding transaction: {} with reason: {}", id, reason);
        salesTransactionService.voidTransaction(id, reason);
        return ResponseEntity.noContent().build();
    }
}
