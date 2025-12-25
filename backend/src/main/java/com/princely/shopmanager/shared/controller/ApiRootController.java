package com.princely.shopmanager.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Root API controller providing basic API information and health status.
 * This controller resolves the 404 error when accessing the root path.
 *
 * <p>Only active in cloud/development profiles. For embedded profile, the root path
 * serves the frontend static resources instead.
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Profile("!embedded")  // Disable in embedded mode to allow frontend at root
@Tag(name = "API Root", description = "Root API endpoints and service information")
public class ApiRootController {

    @GetMapping
    @Operation(
        summary = "Get API information",
        description = "Returns basic information about the Shop Manager API, including available endpoints and documentation links"
    )
    @ApiResponse(
        responseCode = "200",
        description = "API information retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
    )
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        Map<String, Object> apiInfo = Map.of(
            "service", "Shop Manager API",
            "version", "1.0.0",
            "description", "Comprehensive retail management platform with multi-tenancy support",
            "timestamp", Instant.now().toString(),
            "status", "operational",
            "documentation", Map.of(
                "swagger-ui", "/swagger-ui/index.html",
                "api-docs", "/v3/api-docs",
                "actuator", "/actuator"
            ),
            "endpoints", Map.of(
                "shops", "/api/shops",
                "analytics", "/api/analytics",
                "inventory", "/api/inventory",
                "sales", "/api/receipts",
                "investments", "/api/investments",
                "returns", "/api/returns",
                "expenses", "/api/expenses",
                "fraud-detection", "/api/fraud-detection"
            ),
            "authentication", Map.of(
                "type", "OAuth2 / JWT",
                "provider", "Keycloak",
                "realm", "shop-manager"
            )
        );

        return ResponseEntity.ok(apiInfo);
    }

    @GetMapping("/api")
    @Operation(
        summary = "Get API base information",
        description = "Returns information about the main API endpoints"
    )
    @ApiResponse(
        responseCode = "200",
        description = "API base information retrieved successfully"
    )
    public ResponseEntity<Map<String, Object>> getApiBase() {
        Map<String, Object> apiBase = Map.of(
            "message", "Shop Manager API v1.0",
            "documentation", "/swagger-ui/index.html",
            "health", "/actuator/health",
            "base_path", "/api"
        );

        return ResponseEntity.ok(apiBase);
    }
}