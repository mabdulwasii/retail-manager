package com.princely.shopmanager.embedded.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to update a single system setting
 *
 * @author Claude Code
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSettingRequest {

    @NotNull(message = "Setting value is required")
    private String value;
}
