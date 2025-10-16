package com.princely.shopmanager.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to update an existing feature flag")
public class FeatureFlagUpdateRequest {

    @Schema(description = "Whether the feature is enabled", example = "true")
    private Boolean enabled;

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
