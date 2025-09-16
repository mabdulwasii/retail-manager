package com.princely.shopmanager.investment.repository;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestorDistributionRepository extends JpaRepository<InvestorDistribution, String> {

    List<InvestorDistribution> findByInvestment(Investment investment);

    List<InvestorDistribution> findByInvestmentAndStatus(Investment investment, InvestorDistribution.DistributionStatus status);

    @Query("SELECT d FROM InvestorDistribution d WHERE d.investment.id = :investmentId " +
           "AND d.periodStart >= :startDate AND d.periodEnd <= :endDate")
    List<InvestorDistribution> findByInvestmentAndPeriod(
        @Param("investmentId") String investmentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT d FROM InvestorDistribution d WHERE d.investment.shop.id = :shopId " +
           "AND d.status = :status")
    List<InvestorDistribution> findByShopAndStatus(
        @Param("shopId") String shopId,
        @Param("status") InvestorDistribution.DistributionStatus status
    );

    @Query("SELECT d FROM InvestorDistribution d WHERE d.investment.investor.id = :investorId " +
           "ORDER BY d.periodStart DESC")
    List<InvestorDistribution> findByInvestorOrderByPeriodStartDesc(@Param("investorId") String investorId);

    @Query("SELECT d FROM InvestorDistribution d WHERE d.status = :status " +
           "AND d.distributionDate IS NULL ORDER BY d.periodStart")
    List<InvestorDistribution> findPendingDistributions(@Param("status") InvestorDistribution.DistributionStatus status);

    @Query("SELECT SUM(d.distributionAmount) FROM InvestorDistribution d " +
           "WHERE d.investment.id = :investmentId AND d.status = 'PAID'")
    Optional<java.math.BigDecimal> getTotalDistributedAmount(@Param("investmentId") String investmentId);

    @Query("SELECT COUNT(d) FROM InvestorDistribution d " +
           "WHERE d.investment.shop.id = :shopId " +
           "AND d.periodStart >= :startDate AND d.periodEnd <= :endDate")
    long countDistributionsByShopAndPeriod(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    boolean existsByInvestmentAndPeriodStartAndPeriodEnd(
        Investment investment, LocalDateTime periodStart, LocalDateTime periodEnd
    );

    @Query("SELECT d FROM InvestorDistribution d WHERE d.investment.id = :investmentId " +
           "ORDER BY d.periodStart DESC")
    List<InvestorDistribution> findByInvestmentIdOrderByPeriodStartDesc(@Param("investmentId") String investmentId);

    @Query("SELECT d FROM InvestorDistribution d WHERE d.investment.investor.id = :investorId " +
           "ORDER BY d.periodStart DESC")
    List<InvestorDistribution> findByInvestmentInvestorIdOrderByPeriodStartDesc(@Param("investorId") String investorId);
}