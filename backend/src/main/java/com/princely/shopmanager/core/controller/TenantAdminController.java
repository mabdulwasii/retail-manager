package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.core.dto.activation.TenantActivationResponse;
import com.princely.shopmanager.core.dto.registration.PendingTenantResponse;
import com.princely.shopmanager.core.service.TenantRegistrationService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for super admin tenant management operations
 */
@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Administration", description = "Super admin endpoints for tenant management")
@SecurityRequirement(name = "bearerAuth")
public class TenantAdminController {

    private final TenantRegistrationService tenantRegistrationService;

    @Operation(summary = "Get all pending tenant registrations",
               description = "Retrieve all tenant registrations pending super admin approval")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending registrations retrieved successfully",
                content = @Content(schema = @Schema(implementation = PendingTenantResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - super admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_LIST)")
    public ResponseEntity<List<PendingTenantResponse>> getPendingRegistrations() {
        log.info("Retrieving pending tenant registrations");

        List<PendingTenantResponse> pendingTenants = tenantRegistrationService.getPendingRegistrations();

        log.info("Found {} pending tenant registrations", pendingTenants.size());
        return ResponseEntity.ok(pendingTenants);
    }

    @Operation(summary = "Activate or reject a tenant registration",
               description = "Approve or reject a pending tenant registration. Approval activates the tenant, contact user, and selected shops.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tenant activation processed successfully",
                content = @Content(schema = @Schema(implementation = TenantActivationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid activation request"),
        @ApiResponse(responseCode = "403", description = "Access denied - super admin role required"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{tenantId}/activate")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_UPDATE)")
    public ResponseEntity<TenantActivationResponse> activateTenant(
            @PathVariable String tenantId,
            @Valid @RequestBody TenantActivationRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Processing tenant activation request for tenant: {} by admin: {}",
                tenantId, principal.getUsername());

        // Validate that tenantId in path matches request
        if (!tenantId.equals(request.tenantId())) {
            throw new IllegalArgumentException("Tenant ID in path must match tenant ID in request body");
        }

        try {
            tenantRegistrationService.activateTenant(request, principal.getSubject());

            TenantActivationResponse response = request.approved()
                ? TenantActivationResponse.approved(tenantId, null, principal.getUsername())
                : TenantActivationResponse.rejected(tenantId, null, request.rejectionReason(), principal.getUsername());

            log.info("Tenant {} processed successfully: {}", tenantId, request.approved() ? "approved" : "rejected");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to process tenant activation for: {}", tenantId, e);
            throw e; // Will be handled by global exception handler
        }
    }

    @Operation(summary = "Get tenant registration details",
               description = "Get detailed information about a specific tenant registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tenant details retrieved successfully",
                content = @Content(schema = @Schema(implementation = PendingTenantResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - super admin role required"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_READ)")
    public ResponseEntity<PendingTenantResponse> getTenantDetails(@PathVariable String tenantId) {
        log.info("Retrieving tenant details for: {}", tenantId);

        // This method needs to be implemented in the service
        PendingTenantResponse tenantDetails = tenantRegistrationService.getTenantDetails(tenantId);

        return ResponseEntity.ok(tenantDetails);
    }
}