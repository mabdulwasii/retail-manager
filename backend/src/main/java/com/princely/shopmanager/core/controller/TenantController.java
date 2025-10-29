package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.UserCreateRequest;
import com.princely.shopmanager.core.dto.UserResponse;
import com.princely.shopmanager.core.service.TenantService;
import com.princely.shopmanager.core.service.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for tenant management operations.
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Management", description = "Operations for managing tenants and tenant users")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {

    private final UserService userService;
    private final TenantService tenantService;

    /**
     * Create a new user in the tenant.
     *
     * @param tenantId Tenant ID
     * @param request User creation request
     * @return Created user
     */
    @Operation(
        summary = "Create user in tenant",
        description = "Creates a new user within the specified tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or username/email already exists"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
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
    @PostMapping("/{tenantId}/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER')")
    public ResponseEntity<UserResponse> createUserInTenant(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("Creating user {} in tenant {}", request.getUsername(), tenantId);
        User user = userService.createUser(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    /**
     * Get all users in a tenant.
     *
     * @param tenantId Tenant ID
     * @return List of users in the tenant
     */
    @Operation(
        summary = "Get tenant users",
        description = "Retrieves all users within the specified tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
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
    @GetMapping("/{tenantId}/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER')")
    public ResponseEntity<List<UserResponse>> getTenantUsers(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId
    ) {
        log.debug("Retrieving users for tenant {}", tenantId);
        List<User> users = userService.getUsersByTenant(tenantId);
        List<UserResponse> response = users.stream()
            .map(UserResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
