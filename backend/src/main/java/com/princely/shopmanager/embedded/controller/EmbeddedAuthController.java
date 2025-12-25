package com.princely.shopmanager.embedded.controller;

import com.princely.shopmanager.embedded.dto.LoginRequest;
import com.princely.shopmanager.embedded.dto.LoginResponse;
import com.princely.shopmanager.embedded.dto.RefreshTokenRequest;
import com.princely.shopmanager.embedded.dto.RegisterRequest;
import com.princely.shopmanager.embedded.service.EmbeddedAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for embedded mode.
 * Handles local JWT-based authentication (no Keycloak).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Profile("embedded")
@Tag(name = "Authentication", description = "Embedded mode authentication endpoints")
public class EmbeddedAuthController {

    private final EmbeddedAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
