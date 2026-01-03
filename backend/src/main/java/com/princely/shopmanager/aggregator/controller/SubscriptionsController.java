package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.aggregator.domain.CloudSubscription;
import com.princely.shopmanager.aggregator.dto.CreateSubscriptionRequest;
import com.princely.shopmanager.aggregator.dto.SubscriptionDto;
import com.princely.shopmanager.aggregator.dto.SubscriptionUsageDto;
import com.princely.shopmanager.aggregator.service.CloudSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Subscriptions Controller.
 * Manages cloud subscriptions for tenants.
 */
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscriptions", description = "Manage tenant subscriptions and billing")
public class SubscriptionsController {

    private final CloudSubscriptionService subscriptionService;

    /**
     * Create a new subscription for a tenant.
     *
     * POST /api/cloud/tenants/{tenantId}/subscription
     *
     * @param tenantId Tenant ID
     * @param request Subscription creation request
     * @return Created subscription
     */
    @PostMapping
    @Operation(summary = "Create subscription",
            description = "Create a new subscription for a tenant. Starts with trial if specified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or tenant already has active subscription"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<SubscriptionDto> createSubscription(
            @PathVariable String tenantId,
            @Valid @RequestBody CreateSubscriptionRequest request) {

        log.info("Creating subscription for tenant: {}", tenantId);

        // Ensure tenant ID in request matches path variable
        request.setTenantId(tenantId);

        SubscriptionDto subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    /**
     * Get subscription details for a tenant.
     *
     * GET /api/cloud/tenants/{tenantId}/subscription
     *
     * @param tenantId Tenant ID
     * @return Subscription details
     */
    @GetMapping
    @Operation(summary = "Get subscription",
            description = "Get active subscription details for a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<SubscriptionDto> getSubscription(@PathVariable String tenantId) {
        log.info("Getting subscription for tenant: {}", tenantId);
        SubscriptionDto subscription = subscriptionService.getSubscription(tenantId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Get subscription usage statistics.
     *
     * GET /api/cloud/tenants/{tenantId}/subscription/usage
     *
     * @param tenantId Tenant ID
     * @return Usage statistics
     */
    @GetMapping("/usage")
    @Operation(summary = "Get subscription usage",
            description = "Get usage statistics (API calls, storage, shops, users) for a subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved usage stats"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<SubscriptionUsageDto> getUsage(@PathVariable String tenantId) {
        log.debug("Getting usage statistics for tenant: {}", tenantId);
        SubscriptionUsageDto usage = subscriptionService.getUsageStatistics(tenantId);
        return ResponseEntity.ok(usage);
    }

    /**
     * Change subscription tier (upgrade/downgrade).
     *
     * PUT /api/cloud/tenants/{tenantId}/subscription/tier
     *
     * @param tenantId Tenant ID
     * @param request Tier change request
     * @return Updated subscription
     */
    @PutMapping("/tier")
    @Operation(summary = "Change subscription tier",
            description = "Upgrade or downgrade subscription tier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription tier successfully changed"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<SubscriptionDto> changeTier(
            @PathVariable String tenantId,
            @RequestBody ChangeTierRequest request) {

        log.info("Changing subscription tier for tenant: {} to: {}", tenantId, request.newTier());
        SubscriptionDto subscription = subscriptionService.changeSubscriptionTier(tenantId, request.newTier());
        return ResponseEntity.ok(subscription);
    }

    /**
     * Cancel subscription.
     *
     * DELETE /api/cloud/tenants/{tenantId}/subscription
     *
     * @param tenantId Tenant ID
     * @param request Cancellation request
     * @return Success response
     */
    @DeleteMapping
    @Operation(summary = "Cancel subscription",
            description = "Cancel an active subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subscription successfully cancelled"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable String tenantId,
            @RequestBody CancelSubscriptionRequest request) {

        log.info("Cancelling subscription for tenant: {}, reason: {}", tenantId, request.reason());
        subscriptionService.cancelSubscription(tenantId, request.reason());
        return ResponseEntity.noContent().build();
    }

    /**
     * Request DTO for tier change.
     */
    public record ChangeTierRequest(CloudSubscription.SubscriptionTier newTier) {
    }

    /**
     * Request DTO for cancellation.
     */
    public record CancelSubscriptionRequest(String reason) {
    }
}
