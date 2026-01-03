package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for subscription usage statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUsageDto {

    private Long currentApiRequests;
    private Long maxApiRequests;
    private Double apiUsagePercentage;

    private BigDecimal currentStorageGb;
    private Integer maxStorageGb;
    private Double storageUsagePercentage;

    private Integer currentShops;
    private Integer maxShops;

    private Integer currentUsers;
    private Integer maxUsers;

    private Boolean apiLimitExceeded;
    private Boolean storageLimitExceeded;
}
