package com.princely.shopmanager.sales.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesTransactionRepository extends JpaRepository<SalesTransaction, String> {

    List<SalesTransaction> findByShop(Shop shop);

    List<SalesTransaction> findByShopAndStatus(Shop shop, SalesTransaction.TransactionStatus status);

    Optional<SalesTransaction> findByTransactionNumber(String transactionNumber);

    @Query("SELECT SUM(st.totalAmount) FROM SalesTransaction st " +
           "WHERE st.shop.id = :shopId " +
           "AND st.transactionDate >= :startDate " +
           "AND st.transactionDate <= :endDate " +
           "AND st.status = 'COMPLETED' " +
           "AND st.isVoided = false")
    Optional<BigDecimal> getTotalRevenueByShopAndPeriod(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(li.lineTotal) FROM SalesTransaction st " +
           "JOIN st.lineItems li " +
           "WHERE li.product.id IN :productIds " +
           "AND st.transactionDate >= :startDate " +
           "AND st.transactionDate <= :endDate " +
           "AND st.status = 'COMPLETED' " +
           "AND st.isVoided = false")
    Optional<BigDecimal> getTotalRevenueByProductsAndPeriod(
        @Param("productIds") List<String> productIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(st) FROM SalesTransaction st " +
           "WHERE st.shop.id = :shopId " +
           "AND st.transactionDate >= :startDate " +
           "AND st.transactionDate <= :endDate " +
           "AND st.status = 'COMPLETED'")
    long countTransactionsByShopAndPeriod(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT st FROM SalesTransaction st " +
           "WHERE st.shop.id = :shopId " +
           "AND st.transactionDate >= :startDate " +
           "AND st.transactionDate <= :endDate " +
           "ORDER BY st.transactionDate DESC")
    List<SalesTransaction> findByShopAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT st FROM SalesTransaction st " +
           "WHERE st.riskLevel IN ('HIGH', 'CRITICAL') " +
           "AND st.requiresReview = true " +
           "ORDER BY st.transactionDate DESC")
    List<SalesTransaction> findHighRiskTransactions();

    @Query("SELECT st FROM SalesTransaction st " +
           "WHERE st.shop.id = :shopId " +
           "AND st.totalAmount > :amount " +
           "AND st.transactionDate >= :since")
    List<SalesTransaction> findLargeTransactionsSince(
        @Param("shopId") String shopId,
        @Param("amount") BigDecimal amount,
        @Param("since") LocalDateTime since
    );

    boolean existsByTransactionNumber(String transactionNumber);
}