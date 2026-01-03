package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for API Key display (without sensitive data).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyDto {

    private String id;
    private String tenantId;
    private String keyPrefix;
    private String maskedKey;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Long usageCount;
    private Set<String> permissions;
}
