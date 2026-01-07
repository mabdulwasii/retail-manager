package com.princely.shopmanager.embedded.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request to update multiple system settings at once
 *
 * @author Claude Code
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateSettingsRequest {

    @NotNull(message = "Updates map is required")
    @NotEmpty(message = "At least one setting must be provided")
    private Map<String, String> updates;
}
