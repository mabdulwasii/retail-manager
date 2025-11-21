package com.princely.shopmanager.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple response indicating if a feature is enabled")
public class FeatureFlagStatusResponse {

    @Schema(description = "Name of the feature being checked", example = "analytics.enabled")
    private String featureName;

    @Schema(description = "Shop ID if shop-specific check", example = "shop-123")
    private String shopId;

    @Schema(description = "Whether the feature is enabled and effective", example = "true")
    private boolean enabled;

    @Schema(description = "Source of the flag (SHOP_SPECIFIC, GLOBAL, or NOT_FOUND)", example = "SHOP_SPECIFIC")
    private String source;
}
