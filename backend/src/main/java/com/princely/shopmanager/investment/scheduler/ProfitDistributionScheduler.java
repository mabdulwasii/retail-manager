package com.princely.shopmanager.investment.scheduler;

import com.princely.shopmanager.investment.service.InvestmentProfitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.features.investment.enabled", havingValue = "true")
public class ProfitDistributionScheduler {

    private final InvestmentProfitService investmentProfitService;

    /**
     * Calculate monthly profit distributions
     * Runs on the 1st day of each month at 02:00 AM
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void calculateMonthlyProfitDistributions() {
        log.info("Starting monthly profit distribution calculation");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime previousMonth = now.minusMonths(1);

            // Calculate for the entire previous month
            LocalDateTime periodStart = previousMonth.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime periodEnd = previousMonth.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

            var distributions = investmentProfitService.calculateProfitDistributions(periodStart, periodEnd);

            log.info("Completed monthly profit distribution calculation. Created {} distributions for period {} to {}",
                distributions.size(), periodStart, periodEnd);

        } catch (Exception e) {
            log.error("Error during monthly profit distribution calculation", e);
        }
    }

    /**
     * Calculate weekly profit distributions
     * Runs every Monday at 01:00 AM
     */
    @Scheduled(cron = "0 0 1 * * MON")
    public void calculateWeeklyProfitDistributions() {
        log.info("Starting weekly profit distribution calculation");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Calculate for the previous week (Monday to Sunday)
            LocalDateTime periodEnd = now.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59);
            LocalDateTime periodStart = periodEnd.minusDays(6).withHour(0).withMinute(0).withSecond(0);

            var distributions = investmentProfitService.calculateProfitDistributions(periodStart, periodEnd);

            log.info("Completed weekly profit distribution calculation. Created {} distributions for period {} to {}",
                distributions.size(), periodStart, periodEnd);

        } catch (Exception e) {
            log.error("Error during weekly profit distribution calculation", e);
        }
    }

    /**
     * Health check for profit distribution system
     * Runs daily at 06:00 AM
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void profitDistributionHealthCheck() {
        log.debug("Running profit distribution system health check");

        try {
            var pendingDistributions = investmentProfitService.getPendingDistributions();

            if (!pendingDistributions.isEmpty()) {
                log.info("Found {} pending profit distributions awaiting approval", pendingDistributions.size());

                // Log details for distributions older than 7 days
                var now = LocalDateTime.now();
                pendingDistributions.stream()
                    .filter(dist -> dist.getCreatedAt().isBefore(now.minusDays(7)))
                    .forEach(dist -> log.warn("Distribution {} has been pending for more than 7 days - Investment: {}",
                        dist.getId(), dist.getInvestment().getInvestmentNumber()));
            }

        } catch (Exception e) {
            log.error("Error during profit distribution health check", e);
        }
    }
}