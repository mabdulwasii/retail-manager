package com.princely.shopmanager.embedded.dto;

import com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import com.princely.shopmanager.embedded.domain.SystemSettings.SettingDataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * System Setting Data Transfer Object
 *
 * Represents a system setting for API responses.
 * Sensitive values are automatically masked.
 *
 * @author Claude Code
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingDTO {

    private String id;
    private String key;
    private String value;  // Masked if sensitive
    private SettingCategory category;
    private SettingDataType dataType;
    private String description;
    private Boolean requiresRestart;
    private Boolean isSensitive;
    private String defaultValue;
    private Boolean isModified;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private Integer version;
}
