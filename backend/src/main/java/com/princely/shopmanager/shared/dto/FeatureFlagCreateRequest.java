package com.princely.shopmanager.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new feature flag")
public class FeatureFlagCreateRequest {

    @Schema(description = "Shop ID for shop-specific flags, null/empty for global flags", example = "shop-123")
    private String shopId;

    @NotBlank(message = "Feature name is required")
    @Pattern(regexp = "^[a-z][a-z0-9._-]*$", message = "Feature name must start with lowercase letter and contain only lowercase letters, numbers, dots, hyphens, and underscores")
    @Size(min = 3, max = 100, message = "Feature name must be between 3 and 100 characters")
    @Schema(description = "Unique name of the feature", example = "analytics.enabled")
    private String featureName;

    @Builder.Default
    @Schema(description = "Whether the feature is enabled", example = "true")
    private boolean enabled = false;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Human-readable description of the feature", example = "Enables advanced analytics features")
    private String description;

    @Schema(description = "Configuration key-value pairs for the feature")
    private Map<String, String> configuration;

    @Schema(description = "Date and time when the feature becomes effective", example = "2024-01-01T00:00:00")
    private LocalDateTime effectiveFrom;

    @Schema(description = "Date and time when the feature stops being effective", example = "2024-12-31T23:59:59")
    private LocalDateTime effectiveUntil;
}
