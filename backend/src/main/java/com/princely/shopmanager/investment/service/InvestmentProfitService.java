package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.investment.config.ProfitCalculationConfig;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import com.princely.shopmanager.investment.dto.ProfitCalculationResult;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.service.AuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentProfitService {

    private final InvestmentRepository investmentRepository;
    private final InvestorDistributionRepository distributionRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final AuditService auditService;
    private final ProfitCalculationConfig profitConfig;

    @Transactional(readOnly = true)
    public List<InvestorDistribution> calculateProfitDistributions(LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Calculating profit distributions for period {} to {}", periodStart, periodEnd);

        List<Investment> activeInvestments = investmentRepository.findActiveInvestments();

        List<InvestorDistribution> distributions = new ArrayList<>();
        for (Investment investment : activeInvestments) {
            Optional<InvestorDistribution> distribution = performInvestmentDistributionCalculation(investment, periodStart, periodEnd);
            distribution.ifPresent(distributions::add);
        }
        return distributions;
    }

    private Optional<InvestorDistribution> performInvestmentDistributionCalculation(Investment investment, LocalDateTime periodStart, LocalDateTime periodEnd) {
        // Move the calculation logic here to avoid transactional method call issues
        return calculateInvestmentDistributionInternal(investment, periodStart, periodEnd);
    }

    @Transactional
    public Optional<InvestorDistribution> calculateInvestmentDistribution(
            Investment investment, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return calculateInvestmentDistributionInternal(investment, periodStart, periodEnd);
    }

    private Optional<InvestorDistribution> calculateInvestmentDistributionInternal(
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

        if (result.netProfit().compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("No profit to distribute for investment {} in period {} to {}",
                investment.getId(), periodStart, periodEnd);
            return Optional.empty();
        }

        // Calculate investor's share
        BigDecimal investorSharePercentage = calculateInvestorSharePercentage(investment);
        BigDecimal investorProfitAmount = result.netProfit()
            .multiply(investorSharePercentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Create a distribution record
        InvestorDistribution distribution = InvestorDistribution.builder()
            .investment(investment)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .totalSalesRevenue(result.totalRevenue())
            .totalProfit(result.netProfit())
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
            AuditLog.ActionType.PROFIT_CALCULATED,
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
            case SHOP_WIDE -> totalRevenue = salesTransactionRepository
                    .getTotalRevenueByShopAndPeriod(
                        investment.getShop().getId(), periodStart, periodEnd)
                    .orElse(BigDecimal.ZERO);
            case PRODUCT_SPECIFIC, CATEGORY_SPECIFIC -> {
                // Product/Category-specific tracking moved to investment round level
                // For now, fallback to shop-wide revenue
                log.warn("Product/Category-specific profit calculation not yet implemented for investment {}, using shop-wide revenue",
                    investment.getId());
                totalRevenue = salesTransactionRepository
                    .getTotalRevenueByShopAndPeriod(
                        investment.getShop().getId(), periodStart, periodEnd)
                    .orElse(BigDecimal.ZERO);
            }
            default -> totalRevenue = BigDecimal.ZERO;
        }

        return calculateProfitForInvestment(investment, totalRevenue);
    }

    private ProfitCalculationResult calculateProfitForInvestment(Investment investment, BigDecimal totalRevenue) {
        // Calculate operational costs
        BigDecimal operationalCosts = totalRevenue.multiply(profitConfig.getOperationalCostPercentage());

        // Get profit margin (use category-specific if available)
        BigDecimal profitMargin = getProfitMarginForInvestment(investment);

        // Calculate gross and net profit
        BigDecimal grossProfit = totalRevenue.subtract(operationalCosts);
        BigDecimal netProfit = grossProfit.multiply(profitMargin);

        return new ProfitCalculationResult(totalRevenue, grossProfit, netProfit, operationalCosts);
    }

    private BigDecimal getProfitMarginForInvestment(Investment investment) {
        // Product-specific category margins no longer supported
        // All investments now use default profit margin
        return profitConfig.getDefaultProfitMargin();
    }

    private BigDecimal calculateInvestorSharePercentage(Investment investment) {
        var round = investment.getInvestmentRound();

        return switch (round.getProfitSharingModel()) {
            case PROPORTIONAL_BY_AMOUNT -> {
                // Query total amount in round
                BigDecimal totalRoundAmount = investmentRepository
                    .sumAmountByInvestmentRoundId(round.getId());

                if (totalRoundAmount == null || totalRoundAmount.compareTo(BigDecimal.ZERO) == 0) {
                    log.warn("Total round amount is zero or null for round {}", round.getId());
                    yield BigDecimal.ZERO;
                }

                // Calculate proportion: (investorAmount / totalRoundAmount) * 100
                yield investment.getAmount()
                    .divide(totalRoundAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }

            case FIXED_SHARES -> {
                // Query total shares in round
                Integer totalShares = investmentRepository
                    .sumFixedSharesByInvestmentRoundId(round.getId());

                if (totalShares == null || totalShares == 0) {
                    log.warn("Total shares is zero or null for round {}", round.getId());
                    yield BigDecimal.ZERO;
                }

                if (investment.getFixedShares() == null) {
                    log.warn("Investment {} has no fixed shares set", investment.getId());
                    yield BigDecimal.ZERO;
                }

                // Calculate proportion: (investorShares / totalShares) * 100
                yield BigDecimal.valueOf(investment.getFixedShares())
                    .divide(BigDecimal.valueOf(totalShares), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }

            case TIME_WEIGHTED -> {
                // Calculate base proportion
                BigDecimal totalRoundAmount = investmentRepository
                    .sumAmountByInvestmentRoundId(round.getId());

                if (totalRoundAmount == null || totalRoundAmount.compareTo(BigDecimal.ZERO) == 0) {
                    log.warn("Total round amount is zero or null for round {}", round.getId());
                    yield BigDecimal.ZERO;
                }

                BigDecimal baseProportion = investment.getAmount()
                    .divide(totalRoundAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

                // Apply time multiplier
                BigDecimal yearsInvested = calculateYearsInvested(investment);
                BigDecimal timeMultiplier = round.getTimeWeightingRules()
                    .getMultiplierForYears(yearsInvested);

                yield baseProportion.multiply(timeMultiplier);
            }

            case TIERED -> {
                // Calculate base proportion
                BigDecimal totalRoundAmount = investmentRepository
                    .sumAmountByInvestmentRoundId(round.getId());

                if (totalRoundAmount == null || totalRoundAmount.compareTo(BigDecimal.ZERO) == 0) {
                    log.warn("Total round amount is zero or null for round {}", round.getId());
                    yield BigDecimal.ZERO;
                }

                BigDecimal baseProportion = investment.getAmount()
                    .divide(totalRoundAmount, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

                // Apply tier multiplier
                BigDecimal tierMultiplier = round.getTierConfiguration()
                    .getMultiplierForAmount(investment.getAmount());

                yield baseProportion.multiply(tierMultiplier);
            }
        };
    }

    private BigDecimal calculateYearsInvested(Investment investment) {
        long daysBetween = ChronoUnit.DAYS.between(
            investment.getInvestmentDate(),
            LocalDateTime.now()
        );
        return BigDecimal.valueOf(daysBetween / 365.0);
    }

    private String buildCalculationDetails(Investment investment, ProfitCalculationResult result,
                                         BigDecimal sharePercentage) {
        return String.format(
            "Investment: %s, Type: %s, Model: %s, Revenue: %s, Profit: %s, Share: %s%%",
            investment.getInvestmentNumber(),
            investment.getInvestmentType(),
            investment.getProfitSharingModel(),
            result.totalRevenue(),
            result.netProfit(),
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
            AuditLog.ActionType.DISTRIBUTION_APPROVED,
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
            AuditLog.ActionType.DISTRIBUTION_PAID,
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

}