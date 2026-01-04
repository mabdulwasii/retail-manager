package com.princely.shopmanager.aggregator.dto;

import com.princely.shopmanager.aggregator.domain.CloudSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for CloudSubscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

    private String id;
    private String tenantId;
    private CloudSubscription.SubscriptionTier tier;
    private CloudSubscription.BillingCycle billingCycle;
    private CloudSubscription.Status status;
    private BigDecimal price;
    private String currency;
    private Integer maxShops;
    private Integer maxUsersPerShop;
    private Long maxApiRequestsPerMonth;
    private Long currentApiRequests;
    private Integer storageLimitGb;
    private BigDecimal currentStorageGb;
    private LocalDateTime startDate;
    private LocalDateTime trialEndDate;
    private LocalDateTime nextBillingDate;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
