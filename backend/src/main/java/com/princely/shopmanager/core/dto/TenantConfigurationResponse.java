package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.TenantConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tenant configuration response")
public class TenantConfigurationResponse {

    @Schema(description = "Configuration ID", example = "config-123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Tenant ID", example = "tenant-123e4567-e89b-12d3-a456-426614174000")
    private String tenantId;

    @Schema(description = "Configuration key", example = "tax_rate")
    private String key;

    @Schema(description = "Configuration value", example = "0.075")
    private String value;

    @Schema(description = "Default value", example = "0.0")
    private String defaultValue;

    @Schema(description = "Effective value (value or default)", example = "0.075")
    private String effectiveValue;

    @Schema(description = "Value type", example = "NUMBER")
    private TenantConfiguration.ValueType valueType;

    @Schema(description = "Configuration category", example = "BUSINESS")
    private TenantConfiguration.ConfigCategory category;

    @Schema(description = "Configuration description", example = "Sales tax rate as decimal")
    private String description;

    @Schema(description = "Whether the configuration can be edited", example = "true")
    private boolean editable;

    @Schema(description = "Whether the configuration is active", example = "true")
    private boolean active;

    @Schema(description = "Creation timestamp", example = "2025-11-10T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-11-10T14:30:00")
    private LocalDateTime updatedAt;

    public static TenantConfigurationResponse fromEntity(TenantConfiguration entity) {
        return TenantConfigurationResponse.builder()
            .id(entity.getId())
            .tenantId(entity.getTenant().getId())
            .key(entity.getKey())
            .value(entity.getValue())
            .defaultValue(entity.getDefaultValue())
            .effectiveValue(entity.getEffectiveValue())
            .valueType(entity.getValueType())
            .category(entity.getCategory())
            .description(entity.getDescription())
            .editable(entity.isEditable())
            .active(entity.isActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
