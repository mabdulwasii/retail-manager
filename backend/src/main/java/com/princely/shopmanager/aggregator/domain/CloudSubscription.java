package com.princely.shopmanager.aggregator.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cloud Subscription entity.
 * Manages subscription lifecycle, billing cycles, and feature access for cloud tenants.
 */
@Entity
@Table(name = "cloud_subscriptions", indexes = {
        @Index(name = "idx_cloud_subscription_tenant", columnList = "tenant_id"),
        @Index(name = "idx_cloud_subscription_status", columnList = "status"),
        @Index(name = "idx_cloud_subscription_next_billing", columnList = "next_billing_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
public class CloudSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotEmpty(message = "Tenant ID is required")
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    /**
     * Subscription tier (pricing plan).
     */
    @NotNull(message = "Subscription tier is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false, length = 50)
    private SubscriptionTier tier;

    /**
     * Billing cycle (monthly, yearly, etc.).
     */
    @NotNull(message = "Billing cycle is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 50)
    private BillingCycle billingCycle;

    /**
     * Subscription status.
     */
    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.TRIAL;

    /**
     * Price per billing cycle.
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    /**
     * Currency code (USD, EUR, GBP, NGN, etc.).
     */
    @NotEmpty(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Maximum number of shops allowed.
     */
    @Positive(message = "Max shops must be positive")
    @Column(name = "max_shops")
    private Integer maxShops;

    /**
     * Maximum number of users per shop allowed.
     */
    @Positive(message = "Max users must be positive")
    @Column(name = "max_users_per_shop")
    private Integer maxUsersPerShop;

    /**
     * Maximum API requests per month.
     */
    @Positive(message = "Max API requests must be positive")
    @Column(name = "max_api_requests_per_month")
    private Long maxApiRequestsPerMonth;

    /**
     * Current API request count for this billing cycle.
     */
    @Builder.Default
    @Column(name = "current_api_requests", nullable = false)
    private Long currentApiRequests = 0L;

    /**
     * Storage limit in GB.
     */
    @Positive(message = "Storage limit must be positive")
    @Column(name = "storage_limit_gb")
    private Integer storageLimitGb;

    /**
     * Current storage usage in GB.
     */
    @Builder.Default
    @Column(name = "current_storage_gb", precision = 10, scale = 2)
    private BigDecimal currentStorageGb = BigDecimal.ZERO;

    /**
     * Subscription start date.
     */
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Trial end date (null if not on trial).
     */
    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    /**
     * Next billing date.
     */
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    /**
     * Subscription end date (null for active subscriptions).
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Auto-renewal flag.
     */
    @Builder.Default
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = true;

    /**
     * Cancellation reason (if cancelled).
     */
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    /**
     * Cancelled at timestamp.
     */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Subscription tier enum.
     */
    public enum SubscriptionTier {
        FREE,           // Free tier (limited features)
        BASIC,          // Basic paid tier
        PREMIUM,        // Premium tier with advanced features
        ENTERPRISE      // Enterprise tier with all features
    }

    /**
     * Billing cycle enum.
     */
    public enum BillingCycle {
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    /**
     * Subscription status enum.
     */
    public enum Status {
        TRIAL,          // On trial period
        ACTIVE,         // Active subscription (paid)
        PAST_DUE,       // Payment failed, grace period
        CANCELLED,      // Cancelled by user
        SUSPENDED,      // Suspended by admin
        EXPIRED         // Subscription expired
    }

    /**
     * Plan limits based on tier.
     */
    public static class PlanLimits {
        public static final int FREE_MAX_SHOPS = 1;
        public static final int FREE_MAX_USERS_PER_SHOP = 3;
        public static final long FREE_MAX_API_REQUESTS = 1000L;
        public static final int FREE_STORAGE_GB = 1;

        public static final int BASIC_MAX_SHOPS = 5;
        public static final int BASIC_MAX_USERS_PER_SHOP = 10;
        public static final long BASIC_MAX_API_REQUESTS = 50000L;
        public static final int BASIC_STORAGE_GB = 10;

        public static final int PREMIUM_MAX_SHOPS = 20;
        public static final int PREMIUM_MAX_USERS_PER_SHOP = 50;
        public static final long PREMIUM_MAX_API_REQUESTS = 500000L;
        public static final int PREMIUM_STORAGE_GB = 50;

        public static final int ENTERPRISE_MAX_SHOPS = -1; // Unlimited
        public static final int ENTERPRISE_MAX_USERS_PER_SHOP = -1; // Unlimited
        public static final long ENTERPRISE_MAX_API_REQUESTS = -1L; // Unlimited
        public static final int ENTERPRISE_STORAGE_GB = -1; // Unlimited
    }

    /**
     * Check if subscription is active.
     */
    public boolean isActive() {
        return Status.ACTIVE.equals(this.status) || Status.TRIAL.equals(this.status);
    }

    /**
     * Check if subscription is on trial.
     */
    public boolean isTrial() {
        return Status.TRIAL.equals(this.status) &&
                trialEndDate != null &&
                LocalDateTime.now().isBefore(trialEndDate);
    }

    /**
     * Check if trial has expired.
     */
    public boolean isTrialExpired() {
        return Status.TRIAL.equals(this.status) &&
                trialEndDate != null &&
                LocalDateTime.now().isAfter(trialEndDate);
    }

    /**
     * Check if API request limit exceeded.
     */
    public boolean isApiLimitExceeded() {
        if (maxApiRequestsPerMonth == null || maxApiRequestsPerMonth < 0) {
            return false; // Unlimited
        }
        return currentApiRequests >= maxApiRequestsPerMonth;
    }

    /**
     * Check if storage limit exceeded.
     */
    public boolean isStorageLimitExceeded() {
        if (storageLimitGb == null || storageLimitGb < 0) {
            return false; // Unlimited
        }
        return currentStorageGb.compareTo(new BigDecimal(storageLimitGb)) >= 0;
    }

    /**
     * Increment API request count.
     */
    public void incrementApiRequests() {
        this.currentApiRequests = (this.currentApiRequests == null ? 0 : this.currentApiRequests) + 1;
    }

    /**
     * Reset API request count (start of new billing cycle).
     */
    public void resetApiRequests() {
        this.currentApiRequests = 0L;
    }

    /**
     * Cancel subscription.
     */
    public void cancel(String reason) {
        this.status = Status.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.autoRenew = false;
    }

    /**
     * Suspend subscription.
     */
    public void suspend() {
        this.status = Status.SUSPENDED;
    }

    /**
     * Activate subscription (after payment).
     */
    public void activate() {
        this.status = Status.ACTIVE;
    }

    /**
     * Mark as past due (payment failed).
     */
    public void markPastDue() {
        this.status = Status.PAST_DUE;
    }

    /**
     * Expire subscription.
     */
    public void expire() {
        this.status = Status.EXPIRED;
        this.endDate = LocalDateTime.now();
    }
}
