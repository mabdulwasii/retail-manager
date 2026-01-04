package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for API key usage statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyUsageStats {

    private Long totalRequests;
    private Long last24Hours;
    private Long last7Days;
    private Long last30Days;
    private String lastUsedEndpoint;
    private LocalDateTime lastUsedAt;
}
