package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.registration.TenantRegistrationRequest;
import com.princely.shopmanager.core.dto.registration.TenantRegistrationResponse;
import com.princely.shopmanager.core.service.TenantRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for tenant registration (open endpoint - no authentication required)
 */
@RestController
@RequestMapping("/api/v1/public/registration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Registration", description = "Public endpoints for tenant registration")
public class TenantRegistrationController {

    private final TenantRegistrationService tenantRegistrationService;

    @Operation(summary = "Register a new tenant",
               description = "Register a new tenant with contact user and shops. This is a public endpoint that creates inactive entities pending admin approval.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration submitted successfully",
                content = @Content(schema = @Schema(implementation = TenantRegistrationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "409", description = "Tenant name or user already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/tenant")
    public ResponseEntity<TenantRegistrationResponse> registerTenant(
            @Valid @RequestBody TenantRegistrationRequest request,
            HttpServletRequest httpRequest) {

        log.info("Received tenant registration request for: {}", request.getTenantInfo().name());

        try {
            // Extract client information
            String clientIp = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // Process registration
            TenantRegistrationResponse response = tenantRegistrationService.registerTenant(
                request, clientIp, userAgent);

            log.info("Tenant registration successful: {}", response.tenantId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Tenant registration failed for: {}", request.getTenantInfo().name(), e);
            throw e; // Will be handled by global exception handler
        }
    }

    @Operation(summary = "Check tenant name availability",
               description = "Check if a tenant name is available for registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Name availability checked"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/check-tenant-name")
    public ResponseEntity<NameAvailabilityResponse> checkTenantNameAvailability(
            @RequestParam("name") String tenantName) {

        log.debug("Checking tenant name availability: {}", tenantName);

        boolean available = tenantRegistrationService.isTenantNameAvailable(tenantName);

        return ResponseEntity.ok(new NameAvailabilityResponse(
            tenantName,
            available,
            available ? "Tenant name is available" : "Tenant name is already taken"
        ));
    }

    @Operation(summary = "Check username availability",
               description = "Check if a username is available for registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Username availability checked"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/check-username")
    public ResponseEntity<NameAvailabilityResponse> checkUsernameAvailability(
            @RequestParam("username") String username) {

        log.debug("Checking username availability: {}", username);

        boolean available = tenantRegistrationService.isUsernameAvailable(username);

        return ResponseEntity.ok(new NameAvailabilityResponse(
            username,
            available,
            available ? "Username is available" : "Username is already taken"
        ));
    }

    @Operation(summary = "Check email availability",
               description = "Check if an email is available for registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email availability checked"),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @GetMapping("/check-email")
    public ResponseEntity<NameAvailabilityResponse> checkEmailAvailability(
            @RequestParam("email") String email) {

        log.debug("Checking email availability: {}", email);

        boolean available = tenantRegistrationService.isEmailAvailable(email);

        return ResponseEntity.ok(new NameAvailabilityResponse(
            email,
            available,
            available ? "Email is available" : "Email is already registered"
        ));
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Response for name/email availability checks
     */
    public record NameAvailabilityResponse(
        String value,
        boolean available,
        String message
    ) {}
}