package com.princely.shopmanager.aggregator.repository;

import com.princely.shopmanager.aggregator.domain.CloudSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CloudSubscription entity.
 */
@Repository
public interface CloudSubscriptionRepository extends JpaRepository<CloudSubscription, String> {

    /**
     * Find active subscription for a tenant.
     * Only one active subscription per tenant is allowed.
     */
    Optional<CloudSubscription> findByTenantIdAndStatus(
            String tenantId,
            CloudSubscription.Status status
    );

    /**
     * Find all subscriptions for a tenant.
     */
    List<CloudSubscription> findByTenantId(String tenantId);

    /**
     * Find subscriptions by status.
     */
    List<CloudSubscription> findByStatus(CloudSubscription.Status status);

    /**
     * Find subscriptions due for billing (next billing date is today or earlier).
     */
    @Query("SELECT s FROM CloudSubscription s WHERE s.nextBillingDate <= :now AND s.status = 'ACTIVE'")
    List<CloudSubscription> findDueForBilling(@Param("now") LocalDateTime now);

    /**
     * Find subscriptions with expired trials.
     */
    @Query("SELECT s FROM CloudSubscription s WHERE s.status = 'TRIAL' AND s.trialEndDate <= :now")
    List<CloudSubscription> findExpiredTrials(@Param("now") LocalDateTime now);

    /**
     * Find subscriptions approaching API limit.
     */
    @Query("SELECT s FROM CloudSubscription s WHERE s.currentApiRequests >= (s.maxApiRequestsPerMonth * 0.8) AND s.status IN ('ACTIVE', 'TRIAL')")
    List<CloudSubscription> findApproachingApiLimit();

    /**
     * Find subscriptions by tier.
     */
    List<CloudSubscription> findByTier(CloudSubscription.SubscriptionTier tier);

    /**
     * Check if tenant has active subscription.
     */
    boolean existsByTenantIdAndStatusIn(
            String tenantId,
            List<CloudSubscription.Status> statuses
    );

    /**
     * Count active subscriptions by tier.
     */
    @Query("SELECT s.tier, COUNT(s) FROM CloudSubscription s WHERE s.status = 'ACTIVE' GROUP BY s.tier")
    List<Object[]> countActiveSubscriptionsByTier();

    /**
     * Find subscriptions with auto-renew enabled due for renewal.
     */
    @Query("SELECT s FROM CloudSubscription s WHERE s.autoRenew = true AND s.nextBillingDate BETWEEN :start AND :end")
    List<CloudSubscription> findDueForRenewal(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
