package com.princely.shopmanager.shared.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByShop(Shop shop, Pageable pageable);

    Page<AuditLog> findByShopAndCategory(Shop shop, AuditLog.AuditCategory category, Pageable pageable);

    Page<AuditLog> findByUserId(String userId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.shop.id = :shopId " +
           "AND al.actionDate >= :startDate AND al.actionDate <= :endDate " +
           "ORDER BY al.actionDate DESC")
    Page<AuditLog> findByShopAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT al FROM AuditLog al WHERE al.category = :category " +
           "AND al.actionDate >= :startDate AND al.actionDate <= :endDate " +
           "ORDER BY al.actionDate DESC")
    List<AuditLog> findByCategoryAndDateRange(
        @Param("category") AuditLog.AuditCategory category,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT al FROM AuditLog al WHERE al.severity IN ('ERROR', 'CRITICAL') " +
           "AND al.actionDate >= :since ORDER BY al.actionDate DESC")
    List<AuditLog> findErrorsSince(@Param("since") LocalDateTime since);

    @Query("SELECT al FROM AuditLog al WHERE al.success = false " +
           "AND al.actionDate >= :since ORDER BY al.actionDate DESC")
    List<AuditLog> findFailedEventsSince(@Param("since") LocalDateTime since);

    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType " +
           "AND al.entityId = :entityId ORDER BY al.actionDate DESC")
    List<AuditLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") String entityId);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.shop.id = :shopId " +
           "AND al.actionDate >= :startDate AND al.actionDate <= :endDate")
    long countByShopAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.category = :category " +
           "AND al.actionDate >= :startDate AND al.actionDate <= :endDate")
    long countByCategoryAndDateRange(
        @Param("category") AuditLog.AuditCategory category,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}