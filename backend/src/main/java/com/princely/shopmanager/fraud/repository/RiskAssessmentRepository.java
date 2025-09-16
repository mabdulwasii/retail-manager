package com.princely.shopmanager.fraud.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, String> {

    List<RiskAssessment> findByShop(Shop shop);

    List<RiskAssessment> findByTransaction(SalesTransaction transaction);

    List<RiskAssessment> findByRiskLevel(RiskAssessment.RiskLevel riskLevel);

    List<RiskAssessment> findByStatus(RiskAssessment.AssessmentStatus status);

    List<RiskAssessment> findByRiskLevelAndStatus(RiskAssessment.RiskLevel riskLevel, RiskAssessment.AssessmentStatus status);

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.shop.id = :shopId " +
           "AND ra.assessmentDate >= :startDate AND ra.assessmentDate <= :endDate " +
           "ORDER BY ra.assessmentDate DESC")
    List<RiskAssessment> findByShopAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.status = 'PENDING' " +
           "AND ra.riskLevel IN ('HIGH', 'CRITICAL') " +
           "ORDER BY ra.assessmentDate ASC")
    List<RiskAssessment> findPendingHighRiskAssessments();

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.assessmentType = :assessmentType " +
           "AND ra.assessmentDate >= :since ORDER BY ra.assessmentDate DESC")
    List<RiskAssessment> findByTypeAndDateSince(
        @Param("assessmentType") RiskAssessment.AssessmentType assessmentType,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(ra) FROM RiskAssessment ra WHERE ra.shop.id = :shopId " +
           "AND ra.riskLevel = :riskLevel " +
           "AND ra.assessmentDate >= :startDate AND ra.assessmentDate <= :endDate")
    long countByShopAndRiskLevelAndDateRange(
        @Param("shopId") String shopId,
        @Param("riskLevel") RiskAssessment.RiskLevel riskLevel,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.reviewedBy IS NULL " +
           "AND ra.status = 'PENDING' " +
           "AND ra.assessmentDate < :oldestAllowed " +
           "ORDER BY ra.assessmentDate ASC")
    List<RiskAssessment> findStaleAssessments(@Param("oldestAllowed") LocalDateTime oldestAllowed);

    Optional<RiskAssessment> findTopByTransactionOrderByAssessmentDateDesc(SalesTransaction transaction);

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.transaction.id = :transactionId " +
           "ORDER BY ra.assessmentDate DESC")
    List<RiskAssessment> findByTransactionId(@Param("transactionId") String transactionId);

    boolean existsByTransactionAndAssessmentType(SalesTransaction transaction, RiskAssessment.AssessmentType assessmentType);

    // Additional queries for enhanced fraud module
    Page<RiskAssessment> findByShopOrderByAssessmentDateDesc(Shop shop, Pageable pageable);

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.shop = :shop AND ra.status IN :statuses ORDER BY ra.assessmentDate DESC")
    Page<RiskAssessment> findByShopAndStatusIn(
        @Param("shop") Shop shop,
        @Param("statuses") List<RiskAssessment.AssessmentStatus> statuses,
        Pageable pageable
    );

    @Query("SELECT COUNT(ra) FROM RiskAssessment ra WHERE ra.status = :status")
    long countByStatus(@Param("status") RiskAssessment.AssessmentStatus status);

    @Query("SELECT ra.riskLevel, COUNT(ra) FROM RiskAssessment ra WHERE ra.assessmentDate >= :since GROUP BY ra.riskLevel")
    List<Object[]> countByRiskLevelSince(@Param("since") LocalDateTime since);

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.riskLevel IN :riskLevels AND ra.status = :status ORDER BY ra.assessmentDate DESC")
    List<RiskAssessment> findByRiskLevelInAndStatus(
        @Param("riskLevels") List<RiskAssessment.RiskLevel> riskLevels,
        @Param("status") RiskAssessment.AssessmentStatus status
    );
}