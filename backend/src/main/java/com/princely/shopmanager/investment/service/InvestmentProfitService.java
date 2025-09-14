package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentProfitService {

    private final InvestmentRepository investmentRepository;
    private final InvestorDistributionRepository distributionRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final AuditService auditService;

    @Transactional
    public List<InvestorDistribution> calculateProfitDistributions(LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Calculating profit distributions for period {} to {}", periodStart, periodEnd);

        List<Investment> activeInvestments = investmentRepository.findActiveInvestments();

        return activeInvestments.stream()
            .map(investment -> calculateInvestmentDistribution(investment, periodStart, periodEnd))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    @Transactional
    public Optional<InvestorDistribution> calculateInvestmentDistribution(
            Investment investment, LocalDateTime periodStart, LocalDateTime periodEnd) {

        // Check if distribution already exists for this period
        if (distributionRepository.existsByInvestmentAndPeriodStartAndPeriodEnd(
                investment, periodStart, periodEnd)) {
            log.debug("Distribution already exists for investment {} in period {} to {}",
                investment.getId(), periodStart, periodEnd);
            return Optional.empty();
        }

        // Calculate sales and profit for the period
        ProfitCalculationResult result = calculateProfitForInvestment(investment, periodStart, periodEnd);

        if (result.totalProfit().compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("No profit to distribute for investment {} in period {} to {}",
                investment.getId(), periodStart, periodEnd);
            return Optional.empty();
        }

        // Calculate investor's share
        BigDecimal investorSharePercentage = calculateInvestorSharePercentage(investment, result);
        BigDecimal investorProfitAmount = result.totalProfit()
            .multiply(investorSharePercentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Create distribution record
        InvestorDistribution distribution = InvestorDistribution.builder()
            .investment(investment)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .totalSalesRevenue(result.totalRevenue())
            .totalProfit(result.totalProfit())
            .investorSharePercentage(investorSharePercentage)
            .investorProfitAmount(investorProfitAmount)
            .distributionAmount(investorProfitAmount)
            .status(InvestorDistribution.DistributionStatus.CALCULATED)
            .calculationDetails(buildCalculationDetails(investment, result, investorSharePercentage))
            .build();

        distribution = distributionRepository.save(distribution);

        // Update investment's total profit earned
        investment.setTotalProfitEarned(
            investment.getTotalProfitEarned().add(investorProfitAmount)
        );
        investment.setLastProfitCalculation(LocalDateTime.now());
        investmentRepository.save(investment);

        log.info("Calculated profit distribution {} for investment {} - Amount: {}",
            distribution.getId(), investment.getId(), investorProfitAmount);

        auditService.logFinancialTransaction(
            investment.getShop(),
            "SYSTEM",
            "system",
            "PROFIT_CALCULATED",
            distribution.getId(),
            String.format("Profit distribution calculated for investment %s - Amount: %s",
                investment.getInvestmentNumber(), investorProfitAmount),
            true
        );

        return Optional.of(distribution);
    }

    private ProfitCalculationResult calculateProfitForInvestment(
            Investment investment, LocalDateTime periodStart, LocalDateTime periodEnd) {

        BigDecimal totalRevenue;

        switch (investment.getInvestmentType()) {
            case SHOP_WIDE -> {
                totalRevenue = salesTransactionRepository
                    .getTotalRevenueByShopAndPeriod(
                        investment.getShop().getId(), periodStart, periodEnd)
                    .orElse(BigDecimal.ZERO);
            }
            case PRODUCT_SPECIFIC -> {
                List<String> productIds = investment.getProducts().stream()
                    .map(product -> product.getId())
                    .toList();
                totalRevenue = salesTransactionRepository
                    .getTotalRevenueByProductsAndPeriod(productIds, periodStart, periodEnd)
                    .orElse(BigDecimal.ZERO);
            }
            case CATEGORY_SPECIFIC -> {
                // TODO: Implement category-specific calculation
                totalRevenue = BigDecimal.ZERO;
            }
            default -> totalRevenue = BigDecimal.ZERO;
        }

        // Apply profit margin (configurable, default 30%)
        BigDecimal profitMargin = BigDecimal.valueOf(0.30); // 30% profit margin
        BigDecimal totalProfit = totalRevenue.multiply(profitMargin);

        return new ProfitCalculationResult(totalRevenue, totalProfit);
    }

    private BigDecimal calculateInvestorSharePercentage(Investment investment, ProfitCalculationResult result) {
        return switch (investment.getProfitSharingModel()) {
            case PROPORTIONAL_BY_AMOUNT -> investment.getProfitPercentage();
            case FIXED_SHARES -> {
                // Calculate based on fixed shares
                // For now, use the profit percentage as configured
                yield investment.getProfitPercentage();
            }
            case TIME_WEIGHTED -> {
                // Calculate time-weighted percentage
                long daysInvested = ChronoUnit.DAYS.between(
                    investment.getInvestmentDate(), LocalDateTime.now()
                );
                BigDecimal timeWeight = BigDecimal.valueOf(Math.min(daysInvested / 365.0, 1.0));
                yield investment.getProfitPercentage().multiply(timeWeight);
            }
            case TIERED -> {
                // Calculate tiered percentage based on investment amount
                BigDecimal amount = investment.getAmount();
                if (amount.compareTo(BigDecimal.valueOf(100000)) >= 0) {
                    yield investment.getProfitPercentage().multiply(BigDecimal.valueOf(1.2)); // 20% bonus
                } else if (amount.compareTo(BigDecimal.valueOf(50000)) >= 0) {
                    yield investment.getProfitPercentage().multiply(BigDecimal.valueOf(1.1)); // 10% bonus
                } else {
                    yield investment.getProfitPercentage();
                }
            }
        };
    }

    private String buildCalculationDetails(Investment investment, ProfitCalculationResult result,
                                         BigDecimal sharePercentage) {
        return String.format(
            "Investment: %s, Type: %s, Model: %s, Revenue: %s, Profit: %s, Share: %s%%",
            investment.getInvestmentNumber(),
            investment.getInvestmentType(),
            investment.getProfitSharingModel(),
            result.totalRevenue(),
            result.totalProfit(),
            sharePercentage
        );
    }

    @Transactional
    public void approveDistribution(String distributionId, String approvedBy) {
        InvestorDistribution distribution = distributionRepository.findById(distributionId)
            .orElseThrow(() -> new IllegalArgumentException("Distribution not found: " + distributionId));

        if (distribution.getStatus() != InvestorDistribution.DistributionStatus.CALCULATED) {
            throw new IllegalStateException("Distribution must be in CALCULATED status to approve");
        }

        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        distribution.setNotes("Approved by " + approvedBy);
        distributionRepository.save(distribution);

        auditService.logFinancialTransaction(
            distribution.getInvestment().getShop(),
            approvedBy,
            approvedBy,
            "DISTRIBUTION_APPROVED",
            distributionId,
            String.format("Profit distribution approved - Amount: %s",
                distribution.getDistributionAmount()),
            true
        );

        log.info("Approved profit distribution {} by {}", distributionId, approvedBy);
    }

    @Transactional
    public void markDistributionAsPaid(String distributionId, String paymentReference, String paidBy) {
        InvestorDistribution distribution = distributionRepository.findById(distributionId)
            .orElseThrow(() -> new IllegalArgumentException("Distribution not found: " + distributionId));

        if (!distribution.canBePaid()) {
            throw new IllegalStateException("Distribution cannot be paid in current status: " + distribution.getStatus());
        }

        distribution.markAsPaid(paymentReference);
        distributionRepository.save(distribution);

        // Update investment's total withdrawn amount
        Investment investment = distribution.getInvestment();
        investment.setTotalWithdrawn(
            investment.getTotalWithdrawn().add(distribution.getDistributionAmount())
        );
        investmentRepository.save(investment);

        auditService.logFinancialTransaction(
            investment.getShop(),
            paidBy,
            paidBy,
            "DISTRIBUTION_PAID",
            distributionId,
            String.format("Profit distribution paid - Amount: %s, Reference: %s",
                distribution.getDistributionAmount(), paymentReference),
            true
        );

        log.info("Marked profit distribution {} as paid with reference {}", distributionId, paymentReference);
    }

    public List<InvestorDistribution> getPendingDistributions() {
        return distributionRepository.findPendingDistributions(InvestorDistribution.DistributionStatus.CALCULATED);
    }

    public List<InvestorDistribution> getInvestorDistributions(String investorId) {
        return distributionRepository.findByInvestorOrderByPeriodStartDesc(investorId);
    }

    public List<InvestorDistribution> getShopDistributions(String shopId, InvestorDistribution.DistributionStatus status) {
        return distributionRepository.findByShopAndStatus(shopId, status);
    }

    private record ProfitCalculationResult(BigDecimal totalRevenue, BigDecimal totalProfit) {}
}