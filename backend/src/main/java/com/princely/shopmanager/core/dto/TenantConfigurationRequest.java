package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.TenantConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or update tenant configuration")
public class TenantConfigurationRequest {

    @NotEmpty(message = "Configuration key is required")
    @Schema(description = "Configuration key", example = "tax_rate")
    private String key;

    @Schema(description = "Configuration value", example = "0.075")
    private String value;

    @Schema(description = "Default value if not set", example = "0.0")
    private String defaultValue;

    @NotNull(message = "Value type is required")
    @Schema(description = "Type of the configuration value", example = "NUMBER")
    private TenantConfiguration.ValueType valueType;

    @NotNull(message = "Category is required")
    @Schema(description = "Configuration category", example = "BUSINESS")
    private TenantConfiguration.ConfigCategory category;

    @Schema(description = "Description of the configuration", example = "Sales tax rate as decimal")
    private String description;

    @Schema(description = "Whether the configuration can be edited", example = "true")
    @Builder.Default
    private Boolean editable = true;

    @Schema(description = "Whether the configuration is active", example = "true")
    @Builder.Default
    private Boolean active = true;
}
