package com.princely.shopmanager.sales.repository;

import com.princely.shopmanager.sales.domain.Receipt;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String>, JpaSpecificationExecutor<Receipt> {

    Optional<Receipt> findByTransaction(SalesTransaction transaction);

    @Query("SELECT r FROM Receipt r WHERE r.transaction.id = :transactionId")
    Optional<Receipt> findByTransactionId(@Param("transactionId") String transactionId);

    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    List<Receipt> findByStatus(Receipt.ReceiptStatus status);

    @Query("SELECT r FROM Receipt r WHERE r.transaction.shop.id = :shopId " +
           "ORDER BY r.generatedAt DESC")
    List<Receipt> findByShopOrderByGeneratedAtDesc(@Param("shopId") String shopId);

    @Query("SELECT r FROM Receipt r WHERE r.transaction.shop.id = :shopId " +
           "AND r.generatedAt >= :startDate AND r.generatedAt <= :endDate " +
           "ORDER BY r.generatedAt DESC")
    List<Receipt> findByShopAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(r) FROM Receipt r WHERE r.transaction.shop.id = :shopId " +
           "AND r.generatedAt >= :startDate AND r.generatedAt <= :endDate")
    long countByShopAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM Receipt r WHERE r.status = :status " +
           "AND r.generatedAt >= :since ORDER BY r.generatedAt ASC")
    List<Receipt> findByStatusAndGeneratedSince(
        @Param("status") Receipt.ReceiptStatus status,
        @Param("since") LocalDateTime since
    );

    boolean existsByReceiptNumber(String receiptNumber);

    boolean existsByTransaction(SalesTransaction transaction);
}