package com.princely.shopmanager.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple test controller to verify SpringDoc is working.
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for SpringDoc verification")
public class TestController {

    @Operation(summary = "Test endpoint", description = "Simple test endpoint to verify API documentation")
    @ApiResponse(responseCode = "200", description = "Test successful")
    @GetMapping("/hello")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("SpringDoc is working!");
    }
}