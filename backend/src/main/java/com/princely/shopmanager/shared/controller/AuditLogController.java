package com.princely.shopmanager.shared.controller;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.service.ShopService;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.dto.AuditLogFilterRequest;
import com.princely.shopmanager.shared.dto.AuditLogResponse;
import com.princely.shopmanager.shared.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for audit log access.
 * Provides endpoints for viewing and exporting audit logs with filtering capabilities.
 */
@RestController
@RequestMapping("/api/shops/{shopId}/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Logs", description = "Audit trail and security event logging")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditService auditService;
    private final ShopService shopService;

    /**
     * Get paginated audit logs with optional filtering
     */
    @Operation(
        summary = "Get audit logs",
        description = "Retrieve paginated audit logs for a shop with optional filtering by action type, entity type, date range, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Audit logs retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(responseCode = "403", description = "Access denied to shop"),
        @ApiResponse(responseCode = "404", description = "Shop not found")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Search query") @RequestParam(required = false) String search,
            @Parameter(description = "Action type filter") @RequestParam(required = false) String actionType,
            @Parameter(description = "Entity type filter") @RequestParam(required = false) String entityType,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "User ID filter") @RequestParam(required = false) String userId,
            @Parameter(description = "Date from filter") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Date to filter") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Severity filter") @RequestParam(required = false) String severity,
            @Parameter(description = "Success filter") @RequestParam(required = false) Boolean success,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Fetching audit logs for shop: {} by user: {}", shopId, principal.getUsername());

        // Validate page size
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        // Get shop and validate access through service layer
        Shop shop = shopService.getShopEntity(shopId);

        // Build filter request
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .search(search)
            .actionType(actionType)
            .entityType(entityType)
            .category(category)
            .userId(userId)
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .severity(severity)
            .success(success)
            .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs = auditService.getAuditLogs(shop, filters, pageable);

        // Convert to response DTOs
        Page<AuditLogResponse> response = auditLogs.map(AuditLogResponse::fromEntity);

        return ResponseEntity.ok(response);
    }

    /**
     * Export audit logs to CSV
     */
    @Operation(
        summary = "Export audit logs to CSV",
        description = "Download audit logs in CSV format with optional filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CSV file generated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to shop"),
        @ApiResponse(responseCode = "404", description = "Shop not found")
    })
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('AUDIT_LOG_EXPORT')")
    public ResponseEntity<String> exportAuditLogs(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Search query") @RequestParam(required = false) String search,
            @Parameter(description = "Action type filter") @RequestParam(required = false) String actionType,
            @Parameter(description = "Entity type filter") @RequestParam(required = false) String entityType,
            @Parameter(description = "Category filter") @RequestParam(required = false) String category,
            @Parameter(description = "User ID filter") @RequestParam(required = false) String userId,
            @Parameter(description = "Date from filter") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Date to filter") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @Parameter(description = "Severity filter") @RequestParam(required = false) String severity,
            @Parameter(description = "Success filter") @RequestParam(required = false) Boolean success,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Exporting audit logs for shop: {} by user: {}", shopId, principal.getUsername());

        // Get shop and validate access through service layer
        Shop shop = shopService.getShopEntity(shopId);

        // Build filter request
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .search(search)
            .actionType(actionType)
            .entityType(entityType)
            .category(category)
            .userId(userId)
            .dateFrom(dateFrom)
            .dateTo(dateTo)
            .severity(severity)
            .success(success)
            .build();

        String csv = auditService.exportAuditLogsToCsv(shop, filters);

        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("audit-logs-%s-%s.csv", shopId, timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
}
