package com.princely.shopmanager.aggregator.dto;

import com.princely.shopmanager.aggregator.domain.CloudSubscription;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotEmpty(message = "Tenant ID is required")
    private String tenantId;

    @NotNull(message = "Subscription tier is required")
    private CloudSubscription.SubscriptionTier tier;

    @NotNull(message = "Billing cycle is required")
    private CloudSubscription.BillingCycle billingCycle;

    private Integer trialDays; // Number of trial days (null = no trial)

    private Boolean autoRenew = true; // Default to auto-renew
}
