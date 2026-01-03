package com.princely.shopmanager.aggregator.repository;

import com.princely.shopmanager.aggregator.domain.BillingInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BillingInvoice entity.
 */
@Repository
public interface BillingInvoiceRepository extends JpaRepository<BillingInvoice, String> {

    /**
     * Find invoice by invoice number.
     */
    Optional<BillingInvoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find all invoices for a tenant.
     */
    List<BillingInvoice> findByTenantIdOrderByIssueDateDesc(String tenantId);

    /**
     * Find all invoices for a subscription.
     */
    List<BillingInvoice> findBySubscriptionIdOrderByIssueDateDesc(String subscriptionId);

    /**
     * Find invoices by status.
     */
    List<BillingInvoice> findByStatus(BillingInvoice.Status status);

    /**
     * Find invoices by tenant and status.
     */
    List<BillingInvoice> findByTenantIdAndStatus(String tenantId, BillingInvoice.Status status);

    /**
     * Find overdue invoices (pending or failed, past due date).
     */
    @Query("SELECT i FROM BillingInvoice i WHERE i.dueDate < :now AND i.status IN ('PENDING', 'FAILED', 'OVERDUE')")
    List<BillingInvoice> findOverdueInvoices(@Param("now") LocalDateTime now);

    /**
     * Find invoices due for payment retry.
     */
    @Query("SELECT i FROM BillingInvoice i WHERE i.status = 'FAILED' AND i.nextRetryDate <= :now AND i.retryCount < 3")
    List<BillingInvoice> findDueForRetry(@Param("now") LocalDateTime now);

    /**
     * Find pending invoices approaching due date.
     */
    @Query("SELECT i FROM BillingInvoice i WHERE i.status = 'PENDING' AND i.dueDate BETWEEN :start AND :end")
    List<BillingInvoice> findApproachingDueDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Find latest invoice for a subscription.
     */
    Optional<BillingInvoice> findFirstBySubscriptionIdOrderByIssueDateDesc(String subscriptionId);

    /**
     * Check if invoice number already exists.
     */
    boolean existsByInvoiceNumber(String invoiceNumber);

    /**
     * Count invoices by status for a tenant.
     */
    @Query("SELECT i.status, COUNT(i) FROM BillingInvoice i WHERE i.tenantId = :tenantId GROUP BY i.status")
    List<Object[]> countInvoicesByStatus(@Param("tenantId") String tenantId);

    /**
     * Calculate total revenue (paid invoices).
     */
    @Query("SELECT SUM(i.total) FROM BillingInvoice i WHERE i.status = 'PAID'")
    Optional<java.math.BigDecimal> calculateTotalRevenue();

    /**
     * Calculate revenue by tenant.
     */
    @Query("SELECT SUM(i.total) FROM BillingInvoice i WHERE i.tenantId = :tenantId AND i.status = 'PAID'")
    Optional<java.math.BigDecimal> calculateRevenueByTenant(@Param("tenantId") String tenantId);

    /**
     * Find invoices by period.
     */
    @Query("SELECT i FROM BillingInvoice i WHERE i.periodStart >= :start AND i.periodEnd <= :end")
    List<BillingInvoice> findByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
