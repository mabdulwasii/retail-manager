package com.princely.shopmanager.shared.dto;

import com.princely.shopmanager.shared.domain.FeatureFlag;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Feature flag response containing flag details and status")
public class FeatureFlagResponse {

    @Schema(description = "Unique identifier of the feature flag", example = "ff-123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Shop ID for shop-specific flags, null for global flags", example = "shop-123")
    private String shopId;

    @Schema(description = "Unique name of the feature", example = "analytics.enabled")
    private String featureName;

    @Schema(description = "Whether the feature is enabled", example = "true")
    private boolean enabled;

    @Schema(description = "Whether the feature is currently effective (considers schedule)", example = "true")
    private boolean effective;

    @Schema(description = "Whether this is a global feature flag", example = "false")
    private boolean global;

    @Schema(description = "Human-readable description of the feature", example = "Enables advanced analytics features")
    private String description;

    @Schema(description = "Configuration key-value pairs for the feature")
    private Map<String, String> configuration;

    @Schema(description = "Date and time when the feature becomes effective")
    private LocalDateTime effectiveFrom;

    @Schema(description = "Date and time when the feature stops being effective")
    private LocalDateTime effectiveUntil;

    @Schema(description = "Username of the person who created the flag", example = "admin@shopmanager.com")
    private String createdBy;

    @Schema(description = "Username of the person who last modified the flag", example = "manager@shopmanager.com")
    private String lastModifiedBy;

    @Schema(description = "Date and time when the flag was created")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the flag was last modified")
    private LocalDateTime updatedAt;

    public static FeatureFlagResponse fromEntity(FeatureFlag featureFlag) {
        return FeatureFlagResponse.builder()
            .id(featureFlag.getId())
            .shopId(featureFlag.getShop() != null ? featureFlag.getShop().getId() : null)
            .featureName(featureFlag.getFeatureName())
            .enabled(featureFlag.isEnabled())
            .effective(featureFlag.isEffective())
            .global(featureFlag.isGlobal())
            .description(featureFlag.getDescription())
            .configuration(featureFlag.getConfiguration())
            .effectiveFrom(featureFlag.getEffectiveFrom())
            .effectiveUntil(featureFlag.getEffectiveUntil())
            .createdBy(featureFlag.getCreatedBy())
            .lastModifiedBy(featureFlag.getLastModifiedBy())
            .createdAt(featureFlag.getCreatedAt())
            .updatedAt(featureFlag.getUpdatedAt())
            .build();
    }
}
