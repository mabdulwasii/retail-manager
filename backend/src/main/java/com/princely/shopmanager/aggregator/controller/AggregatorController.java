package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.aggregator.dto.ShopLinkRequest;
import com.princely.shopmanager.aggregator.dto.TenantRegistrationRequest;
import com.princely.shopmanager.aggregator.dto.TenantRegistrationResponse;
import com.princely.shopmanager.aggregator.service.CloudTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cloud Aggregator API Controller.
 * Handles registration and management of local embedded installations.
 * This API is used by local RetailHQ installations to register with the cloud
 * and enable future analytics sync functionality.
 */
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cloud Aggregator", description = "Cloud aggregator API for local installation registration")
public class AggregatorController {

    private final CloudTenantService cloudTenantService;

    /**
     * Register a new tenant from local embedded installation.
     * Returns API key for future sync operations (only returned once).
     *
     * POST /api/registration/tenants
     *
     * @param request Tenant registration request
     * @return Registration response with cloud tenant ID and API key
     */
    @PostMapping("/tenants")
    @Operation(summary = "Register local installation",
            description = "Register a local RetailHQ installation with the cloud aggregator. " +
                    "Returns an API key that must be securely stored for future sync operations.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tenant successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request or tenant already exists")
    })
    public ResponseEntity<TenantRegistrationResponse> registerTenant(
            @Valid @RequestBody TenantRegistrationRequest request) {

        log.info("Received tenant registration request for: {}", request.getTenantEmail());

        TenantRegistrationResponse response = cloudTenantService.registerTenant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Link an additional shop to an existing cloud tenant.
     * Requires API key authentication.
     *
     * POST /api/registration/shops
     *
     * @param request Shop link request
     * @param apiKey API key from X-API-Key header
     * @return Success response
     */
    @PostMapping("/shops")
    @Operation(summary = "Link additional shop",
            description = "Link an additional shop to an existing cloud tenant registration. " +
                    "Requires API key in X-API-Key header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shop successfully linked"),
            @ApiResponse(responseCode = "401", description = "Invalid API key"),
            @ApiResponse(responseCode = "404", description = "Cloud tenant not found")
    })
    public ResponseEntity<Void> linkShop(
            @Valid @RequestBody ShopLinkRequest request,
            @Parameter(description = "API key for authentication", required = true)
            @RequestHeader(value = "X-API-Key") String apiKey) {

        log.info("Received shop link request for cloud tenant: {}", request.getCloudTenantId());

        cloudTenantService.linkShop(request, apiKey);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Unregister a cloud tenant and all associated shops.
     * Requires API key authentication.
     *
     * DELETE /api/registration/tenants/{cloudTenantId}
     *
     * @param cloudTenantId Cloud tenant ID
     * @param apiKey API key from X-API-Key header
     * @return Success response
     */
    @DeleteMapping("/tenants/{cloudTenantId}")
    @Operation(summary = "Unregister tenant",
            description = "Unregister a cloud tenant and delete all associated data. " +
                    "Requires API key in X-API-Key header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cloud tenant successfully unregistered"),
            @ApiResponse(responseCode = "401", description = "Invalid API key"),
            @ApiResponse(responseCode = "404", description = "Cloud tenant not found")
    })
    public ResponseEntity<Void> unregisterTenant(
            @PathVariable String cloudTenantId,
            @Parameter(description = "API key for authentication", required = true)
            @RequestHeader(value = "X-API-Key") String apiKey) {

        log.info("Received unregister request for cloud tenant: {}", cloudTenantId);

        cloudTenantService.unregisterTenant(cloudTenantId, apiKey);

        return ResponseEntity.ok().build();
    }

    /**
     * Health check endpoint for cloud aggregator API.
     *
     * GET /api/registration/health
     *
     * @return Success response
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if cloud aggregator API is operational")
    @ApiResponse(responseCode = "200", description = "Cloud aggregator API is operational")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Cloud aggregator API is operational");
    }
}