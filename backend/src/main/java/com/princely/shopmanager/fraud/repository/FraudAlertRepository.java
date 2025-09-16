package com.princely.shopmanager.fraud.repository;

import com.princely.shopmanager.fraud.domain.FraudAlert;
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
public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {

    Page<FraudAlert> findByTenantIdOrderByDetectionTimestampDesc(String tenantId, Pageable pageable);

    Page<FraudAlert> findByTenantIdAndStatusOrderByDetectionTimestampDesc(
            String tenantId, FraudAlert.AlertStatus status, Pageable pageable);

    Page<FraudAlert> findByTenantIdAndSeverityOrderByDetectionTimestampDesc(
            String tenantId, FraudAlert.AlertSeverity severity, Pageable pageable);

    Page<FraudAlert> findByTenantIdAndAlertTypeOrderByDetectionTimestampDesc(
            String tenantId, FraudAlert.AlertType alertType, Pageable pageable);

    Page<FraudAlert> findByShopIdOrderByDetectionTimestampDesc(String shopId, Pageable pageable);

    Page<FraudAlert> findByUserIdOrderByDetectionTimestampDesc(String userId, Pageable pageable);

    List<FraudAlert> findByTenantIdAndStatusIn(String tenantId, List<FraudAlert.AlertStatus> statuses);

    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.tenantId = :tenantId AND fa.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") FraudAlert.AlertStatus status);

    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.tenantId = :tenantId AND fa.severity = :severity AND fa.status IN :statuses")
    long countByTenantIdAndSeverityAndStatusIn(
            @Param("tenantId") String tenantId,
            @Param("severity") FraudAlert.AlertSeverity severity,
            @Param("statuses") List<FraudAlert.AlertStatus> statuses);

    @Query("SELECT fa FROM FraudAlert fa WHERE fa.tenantId = :tenantId AND fa.detectionTimestamp BETWEEN :startDate AND :endDate ORDER BY fa.detectionTimestamp DESC")
    Page<FraudAlert> findByTenantIdAndDetectionTimestampBetween(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT fa.alertType, COUNT(fa) FROM FraudAlert fa WHERE fa.tenantId = :tenantId AND fa.detectionTimestamp >= :since GROUP BY fa.alertType")
    List<Object[]> countByAlertTypeSince(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);

    @Query("SELECT fa.severity, COUNT(fa) FROM FraudAlert fa WHERE fa.tenantId = :tenantId AND fa.status IN :statuses GROUP BY fa.severity")
    List<Object[]> countBySeverityAndStatusIn(
            @Param("tenantId") String tenantId,
            @Param("statuses") List<FraudAlert.AlertStatus> statuses);

    @Query("SELECT fa FROM FraudAlert fa WHERE fa.tenantId = :tenantId AND fa.severity IN :severities AND fa.status = :status ORDER BY fa.detectionTimestamp DESC")
    List<FraudAlert> findByTenantIdAndSeverityInAndStatus(
            @Param("tenantId") String tenantId,
            @Param("severities") List<FraudAlert.AlertSeverity> severities,
            @Param("status") FraudAlert.AlertStatus status);

    Optional<FraudAlert> findByAlertNumber(String alertNumber);

    @Query("SELECT fa FROM FraudAlert fa WHERE fa.transactionId = :transactionId")
    List<FraudAlert> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT fa FROM FraudAlert fa WHERE fa.investmentId = :investmentId")
    List<FraudAlert> findByInvestmentId(@Param("investmentId") String investmentId);

    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.detectionRule = :ruleName AND fa.detectionTimestamp >= :since")
    long countByDetectionRuleSince(@Param("ruleName") String ruleName, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.detectionRule = :ruleName AND fa.falsePositive = true")
    long countFalsePositivesByRule(@Param("ruleName") String ruleName);

    boolean existsByTransactionIdAndAlertType(String transactionId, FraudAlert.AlertType alertType);

    boolean existsByUserIdAndAlertTypeAndDetectionTimestampAfter(
            String userId, FraudAlert.AlertType alertType, LocalDateTime since);
}