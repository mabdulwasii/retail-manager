package com.princely.shopmanager.aggregator.service;

import com.princely.shopmanager.aggregator.domain.BillingInvoice;
import com.princely.shopmanager.aggregator.domain.CloudSubscription;
import com.princely.shopmanager.aggregator.domain.CloudTenant;
import com.princely.shopmanager.aggregator.dto.CreateSubscriptionRequest;
import com.princely.shopmanager.aggregator.dto.InvoiceDto;
import com.princely.shopmanager.aggregator.dto.SubscriptionDto;
import com.princely.shopmanager.aggregator.dto.SubscriptionUsageDto;
import com.princely.shopmanager.aggregator.repository.BillingInvoiceRepository;
import com.princely.shopmanager.aggregator.repository.CloudSubscriptionRepository;
import com.princely.shopmanager.aggregator.repository.CloudTenantRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing cloud subscriptions and billing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudSubscriptionService {

    private final CloudSubscriptionRepository subscriptionRepository;
    private final BillingInvoiceRepository invoiceRepository;
    private final CloudTenantRepository cloudTenantRepository;

    // Pricing configuration (in USD)
    private static final BigDecimal BASIC_MONTHLY_PRICE = new BigDecimal("29.99");
    private static final BigDecimal BASIC_YEARLY_PRICE = new BigDecimal("299.99");
    private static final BigDecimal PREMIUM_MONTHLY_PRICE = new BigDecimal("99.99");
    private static final BigDecimal PREMIUM_YEARLY_PRICE = new BigDecimal("999.99");
    private static final BigDecimal ENTERPRISE_MONTHLY_PRICE = new BigDecimal("299.99");
    private static final BigDecimal ENTERPRISE_YEARLY_PRICE = new BigDecimal("2999.99");

    /**
     * Create a new subscription for a tenant.
     */
    @Transactional
    public SubscriptionDto createSubscription(CreateSubscriptionRequest request) {
        log.info("Creating subscription for tenant: {}, tier: {}", request.getTenantId(), request.getTier());

        // Validate tenant exists
        CloudTenant tenant = cloudTenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Cloud tenant not found: " + request.getTenantId()));

        // Check if tenant already has active subscription
        if (subscriptionRepository.existsByTenantIdAndStatusIn(request.getTenantId(),
                List.of(CloudSubscription.Status.ACTIVE, CloudSubscription.Status.TRIAL))) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Tenant already has an active subscription");
        }

        // Calculate pricing
        BigDecimal price = calculatePrice(request.getTier(), request.getBillingCycle());

        // Calculate dates
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime trialEndDate = null;
        LocalDateTime nextBillingDate = null;
        CloudSubscription.Status status;

        if (request.getTrialDays() != null && request.getTrialDays() > 0) {
            // Start with trial
            status = CloudSubscription.Status.TRIAL;
            trialEndDate = startDate.plusDays(request.getTrialDays());
            nextBillingDate = trialEndDate;
        } else {
            // Start as active subscription (requires immediate payment)
            status = CloudSubscription.Status.ACTIVE;
            nextBillingDate = calculateNextBillingDate(startDate, request.getBillingCycle());
        }

        // Get plan limits
        var limits = getPlanLimits(request.getTier());

        // Create subscription
        CloudSubscription subscription = CloudSubscription.builder()
                .tenantId(request.getTenantId())
                .tier(request.getTier())
                .billingCycle(request.getBillingCycle())
                .status(status)
                .price(price)
                .currency("USD")
                .maxShops(limits.maxShops)
                .maxUsersPerShop(limits.maxUsersPerShop)
                .maxApiRequestsPerMonth(limits.maxApiRequests)
                .currentApiRequests(0L)
                .storageLimitGb(limits.storageLimitGb)
                .currentStorageGb(BigDecimal.ZERO)
                .startDate(startDate)
                .trialEndDate(trialEndDate)
                .nextBillingDate(nextBillingDate)
                .autoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : true)
                .build();

        CloudSubscription saved = subscriptionRepository.save(subscription);

        // Update tenant subscription tier
        tenant.setSubscriptionTier(CloudTenant.SubscriptionTier.valueOf(request.getTier().name()));
        cloudTenantRepository.save(tenant);

        // Create first invoice if not on trial
        if (status == CloudSubscription.Status.ACTIVE) {
            createInvoiceForSubscription(saved);
        }

        log.info("Subscription created: {} for tenant: {}", saved.getId(), request.getTenantId());
        return toDto(saved);
    }

    /**
     * Get subscription for a tenant.
     */
    public SubscriptionDto getSubscription(String tenantId) {
        log.debug("Getting subscription for tenant: {}", tenantId);

        CloudSubscription subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, CloudSubscription.Status.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.TRIAL))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "No active subscription found for tenant: " + tenantId));

        return toDto(subscription);
    }

    /**
     * Get subscription usage statistics.
     */
    public SubscriptionUsageDto getUsageStatistics(String tenantId) {
        log.debug("Getting usage statistics for tenant: {}", tenantId);

        CloudSubscription subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, CloudSubscription.Status.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.TRIAL))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "No active subscription found for tenant: " + tenantId));

        // Get current tenant info
        CloudTenant tenant = cloudTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Tenant not found: " + tenantId));

        // Calculate usage percentages
        double apiUsagePercentage = subscription.getMaxApiRequestsPerMonth() != null &&
                subscription.getMaxApiRequestsPerMonth() > 0
                ? (subscription.getCurrentApiRequests() * 100.0) / subscription.getMaxApiRequestsPerMonth()
                : 0.0;

        double storageUsagePercentage = subscription.getStorageLimitGb() != null &&
                subscription.getStorageLimitGb() > 0
                ? (subscription.getCurrentStorageGb().doubleValue() * 100.0) / subscription.getStorageLimitGb()
                : 0.0;

        return SubscriptionUsageDto.builder()
                .currentApiRequests(subscription.getCurrentApiRequests())
                .maxApiRequests(subscription.getMaxApiRequestsPerMonth())
                .apiUsagePercentage(apiUsagePercentage)
                .currentStorageGb(subscription.getCurrentStorageGb())
                .maxStorageGb(subscription.getStorageLimitGb())
                .storageUsagePercentage(storageUsagePercentage)
                .currentShops(tenant.getShopCount())
                .maxShops(subscription.getMaxShops())
                .currentUsers(0) // TODO: Implement user count
                .maxUsers(subscription.getMaxUsersPerShop())
                .apiLimitExceeded(subscription.isApiLimitExceeded())
                .storageLimitExceeded(subscription.isStorageLimitExceeded())
                .build();
    }

    /**
     * Cancel subscription.
     */
    @Transactional
    public void cancelSubscription(String tenantId, String reason) {
        log.info("Cancelling subscription for tenant: {}, reason: {}", tenantId, reason);

        CloudSubscription subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, CloudSubscription.Status.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.TRIAL))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "No active subscription found for tenant: " + tenantId));

        subscription.cancel(reason);
        subscriptionRepository.save(subscription);

        log.info("Subscription cancelled: {}", subscription.getId());
    }

    /**
     * Upgrade/downgrade subscription.
     */
    @Transactional
    public SubscriptionDto changeSubscriptionTier(String tenantId, CloudSubscription.SubscriptionTier newTier) {
        log.info("Changing subscription tier for tenant: {} to: {}", tenantId, newTier);

        CloudSubscription subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, CloudSubscription.Status.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.TRIAL))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "No active subscription found for tenant: " + tenantId));

        // Calculate new pricing
        BigDecimal newPrice = calculatePrice(newTier, subscription.getBillingCycle());

        // Update subscription
        subscription.setTier(newTier);
        subscription.setPrice(newPrice);

        // Update limits
        var limits = getPlanLimits(newTier);
        subscription.setMaxShops(limits.maxShops);
        subscription.setMaxUsersPerShop(limits.maxUsersPerShop);
        subscription.setMaxApiRequestsPerMonth(limits.maxApiRequests);
        subscription.setStorageLimitGb(limits.storageLimitGb);

        CloudSubscription saved = subscriptionRepository.save(subscription);

        // Update tenant tier
        CloudTenant tenant = cloudTenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Tenant not found: " + tenantId));
        tenant.setSubscriptionTier(CloudTenant.SubscriptionTier.valueOf(newTier.name()));
        cloudTenantRepository.save(tenant);

        log.info("Subscription tier changed to: {} for tenant: {}", newTier, tenantId);
        return toDto(saved);
    }

    /**
     * Get invoices for a tenant.
     */
    public List<InvoiceDto> getInvoices(String tenantId) {
        log.debug("Getting invoices for tenant: {}", tenantId);

        List<BillingInvoice> invoices = invoiceRepository.findByTenantIdOrderByIssueDateDesc(tenantId);
        return invoices.stream()
                .map(this::toInvoiceDto)
                .collect(Collectors.toList());
    }

    /**
     * Record API usage (increment counter).
     */
    @Transactional
    public void recordApiUsage(String tenantId) {
        subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.TRIAL))
                .ifPresent(subscription -> {
                    subscription.incrementApiRequests();
                    subscriptionRepository.save(subscription);
                });
    }

    // ==================== Helper Methods ====================

    /**
     * Calculate price based on tier and billing cycle.
     */
    private BigDecimal calculatePrice(CloudSubscription.SubscriptionTier tier, CloudSubscription.BillingCycle cycle) {
        return switch (tier) {
            case FREE -> BigDecimal.ZERO;
            case BASIC -> cycle == CloudSubscription.BillingCycle.YEARLY ? BASIC_YEARLY_PRICE : BASIC_MONTHLY_PRICE;
            case PREMIUM -> cycle == CloudSubscription.BillingCycle.YEARLY ? PREMIUM_YEARLY_PRICE : PREMIUM_MONTHLY_PRICE;
            case ENTERPRISE -> cycle == CloudSubscription.BillingCycle.YEARLY ? ENTERPRISE_YEARLY_PRICE : ENTERPRISE_MONTHLY_PRICE;
        };
    }

    /**
     * Calculate next billing date based on cycle.
     */
    private LocalDateTime calculateNextBillingDate(LocalDateTime startDate, CloudSubscription.BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    /**
     * Get plan limits for a tier.
     */
    private static class PlanLimitsDto {
        int maxShops;
        int maxUsersPerShop;
        long maxApiRequests;
        int storageLimitGb;

        PlanLimitsDto(int maxShops, int maxUsersPerShop, long maxApiRequests, int storageLimitGb) {
            this.maxShops = maxShops;
            this.maxUsersPerShop = maxUsersPerShop;
            this.maxApiRequests = maxApiRequests;
            this.storageLimitGb = storageLimitGb;
        }
    }

    private PlanLimitsDto getPlanLimits(CloudSubscription.SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> new PlanLimitsDto(
                    CloudSubscription.PlanLimits.FREE_MAX_SHOPS,
                    CloudSubscription.PlanLimits.FREE_MAX_USERS_PER_SHOP,
                    CloudSubscription.PlanLimits.FREE_MAX_API_REQUESTS,
                    CloudSubscription.PlanLimits.FREE_STORAGE_GB
            );
            case BASIC -> new PlanLimitsDto(
                    CloudSubscription.PlanLimits.BASIC_MAX_SHOPS,
                    CloudSubscription.PlanLimits.BASIC_MAX_USERS_PER_SHOP,
                    CloudSubscription.PlanLimits.BASIC_MAX_API_REQUESTS,
                    CloudSubscription.PlanLimits.BASIC_STORAGE_GB
            );
            case PREMIUM -> new PlanLimitsDto(
                    CloudSubscription.PlanLimits.PREMIUM_MAX_SHOPS,
                    CloudSubscription.PlanLimits.PREMIUM_MAX_USERS_PER_SHOP,
                    CloudSubscription.PlanLimits.PREMIUM_MAX_API_REQUESTS,
                    CloudSubscription.PlanLimits.PREMIUM_STORAGE_GB
            );
            case ENTERPRISE -> new PlanLimitsDto(
                    CloudSubscription.PlanLimits.ENTERPRISE_MAX_SHOPS,
                    CloudSubscription.PlanLimits.ENTERPRISE_MAX_USERS_PER_SHOP,
                    CloudSubscription.PlanLimits.ENTERPRISE_MAX_API_REQUESTS,
                    CloudSubscription.PlanLimits.ENTERPRISE_STORAGE_GB
            );
        };
    }

    /**
     * Create invoice for a subscription billing cycle.
     */
    private void createInvoiceForSubscription(CloudSubscription subscription) {
        String invoiceNumber = generateInvoiceNumber();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(14); // 14 days payment term

        BillingInvoice invoice = BillingInvoice.builder()
                .tenantId(subscription.getTenantId())
                .subscriptionId(subscription.getId())
                .invoiceNumber(invoiceNumber)
                .periodStart(subscription.getNextBillingDate().minusMonths(1)) // Previous month
                .periodEnd(subscription.getNextBillingDate())
                .issueDate(now)
                .dueDate(dueDate)
                .subtotal(subscription.getPrice())
                .taxAmount(BigDecimal.ZERO)
                .taxRate(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .total(subscription.getPrice())
                .currency(subscription.getCurrency())
                .status(BillingInvoice.Status.PENDING)
                .lineItems("Subscription: " + subscription.getTier() + " - " + subscription.getBillingCycle())
                .build();

        invoiceRepository.save(invoice);
        log.info("Invoice created: {} for subscription: {}", invoiceNumber, subscription.getId());
    }

    /**
     * Generate unique invoice number.
     */
    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = invoiceRepository.count() + 1;
        return String.format("%s-%06d", prefix, count);
    }

    /**
     * Convert CloudSubscription entity to DTO.
     */
    private SubscriptionDto toDto(CloudSubscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .tenantId(subscription.getTenantId())
                .tier(subscription.getTier())
                .billingCycle(subscription.getBillingCycle())
                .status(subscription.getStatus())
                .price(subscription.getPrice())
                .currency(subscription.getCurrency())
                .maxShops(subscription.getMaxShops())
                .maxUsersPerShop(subscription.getMaxUsersPerShop())
                .maxApiRequestsPerMonth(subscription.getMaxApiRequestsPerMonth())
                .currentApiRequests(subscription.getCurrentApiRequests())
                .storageLimitGb(subscription.getStorageLimitGb())
                .currentStorageGb(subscription.getCurrentStorageGb())
                .startDate(subscription.getStartDate())
                .trialEndDate(subscription.getTrialEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .cancellationReason(subscription.getCancellationReason())
                .cancelledAt(subscription.getCancelledAt())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    /**
     * Convert BillingInvoice entity to DTO.
     */
    private InvoiceDto toInvoiceDto(BillingInvoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .tenantId(invoice.getTenantId())
                .subscriptionId(invoice.getSubscriptionId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .periodStart(invoice.getPeriodStart())
                .periodEnd(invoice.getPeriodEnd())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .taxRate(invoice.getTaxRate())
                .discountAmount(invoice.getDiscountAmount())
                .total(invoice.getTotal())
                .amountPaid(invoice.getAmountPaid())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .paymentTransactionId(invoice.getPaymentTransactionId())
                .paymentDate(invoice.getPaymentDate())
                .lineItems(invoice.getLineItems())
                .notes(invoice.getNotes())
                .pdfUrl(invoice.getPdfUrl())
                .retryCount(invoice.getRetryCount())
                .nextRetryDate(invoice.getNextRetryDate())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
