package com.princely.shopmanager.aggregator.service;

import com.princely.shopmanager.aggregator.dto.*;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for cross-shop analytics aggregation.
 * Aggregates sales, revenue, and product data across multiple shops within a tenant.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrossShopAnalyticsService {

    private final JdbcTemplate jdbcTemplate;
    private final ShopRepository shopRepository;
    private final SalesTransactionRepository salesTransactionRepository;

    /**
     * Get revenue analytics aggregated across shops for a tenant.
     *
     * @param tenantId Tenant ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param shopIds List of shop IDs to include (optional, null = all shops)
     * @return Revenue analytics
     */
    public RevenueAnalyticsDto getRevenueAnalytics(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> shopIds) {

        log.info("Getting revenue analytics for tenant: {} (shops: {}, period: {} to {})",
                tenantId, shopIds != null ? shopIds.size() : "all", startDate, endDate);

        // Default to last 30 days if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get shop IDs for the tenant
        List<String> targetShopIds = getTargetShopIds(tenantId, shopIds);

        if (targetShopIds.isEmpty()) {
            log.warn("No shops found for tenant: {}", tenantId);
            return buildEmptyRevenueAnalytics();
        }

        // Get daily revenue data points
        List<RevenueAnalyticsDto.RevenueDataPoint> dataPoints = getDailyRevenueDataPoints(
                targetShopIds, startDateTime, endDateTime);

        // Calculate totals
        BigDecimal totalRevenue = dataPoints.stream()
                .map(RevenueAnalyticsDto.RevenueDataPoint::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalTransactions = dataPoints.stream()
                .map(RevenueAnalyticsDto.RevenueDataPoint::getTransactionCount)
                .reduce(0L, Long::sum);

        BigDecimal averageOrderValue = totalTransactions > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get previous period revenue for comparison
        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDateTime previousStart = startDateTime.minusDays(periodDays);
        LocalDateTime previousEnd = startDateTime.minusSeconds(1);

        BigDecimal previousPeriodRevenue = getTotalRevenue(targetShopIds, previousStart, previousEnd);

        BigDecimal growthPercentage = calculateGrowthPercentage(totalRevenue, previousPeriodRevenue);

        return RevenueAnalyticsDto.builder()
                .dataPoints(dataPoints)
                .totalRevenue(totalRevenue)
                .totalTransactions(totalTransactions)
                .averageOrderValue(averageOrderValue)
                .previousPeriodRevenue(previousPeriodRevenue)
                .growthPercentage(growthPercentage)
                .build();
    }

    /**
     * Get sales metrics aggregated across shops for a tenant.
     *
     * @param tenantId Tenant ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param shopIds List of shop IDs to include (optional, null = all shops)
     * @return Sales metrics
     */
    public SalesMetricsDto getSalesMetrics(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> shopIds) {

        log.info("Getting sales metrics for tenant: {} (shops: {}, period: {} to {})",
                tenantId, shopIds != null ? shopIds.size() : "all", startDate, endDate);

        // Default to last 30 days if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get shop IDs for the tenant
        List<String> targetShopIds = getTargetShopIds(tenantId, shopIds);

        if (targetShopIds.isEmpty()) {
            log.warn("No shops found for tenant: {}", tenantId);
            return buildEmptySalesMetrics();
        }

        // Get total sales count
        long totalSales = getTotalTransactionCount(targetShopIds, startDateTime, endDateTime);

        // Get total revenue
        BigDecimal totalRevenue = getTotalRevenue(targetShopIds, startDateTime, endDateTime);

        // Calculate average order value
        BigDecimal averageOrderValue = totalSales > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get top selling day
        LocalDate topSellingDay = getTopSellingDay(targetShopIds, startDateTime, endDateTime);

        // Get peak hour
        Integer peakHour = getPeakHour(targetShopIds, startDateTime, endDateTime);

        // Get previous period sales for comparison
        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDateTime previousStart = startDateTime.minusDays(periodDays);
        LocalDateTime previousEnd = startDateTime.minusSeconds(1);

        Long previousPeriodSales = getTotalTransactionCount(targetShopIds, previousStart, previousEnd);

        BigDecimal salesGrowth = calculateGrowthPercentage(
                BigDecimal.valueOf(totalSales),
                BigDecimal.valueOf(previousPeriodSales));

        return SalesMetricsDto.builder()
                .totalSales(totalSales)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .topSellingDay(topSellingDay)
                .peakHour(peakHour)
                .previousPeriodSales(previousPeriodSales)
                .salesGrowth(salesGrowth)
                .build();
    }

    /**
     * Get top selling products aggregated across shops for a tenant.
     *
     * @param tenantId Tenant ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param shopIds List of shop IDs to include (optional, null = all shops)
     * @param limit Maximum number of products to return
     * @return Top products analytics
     */
    public TopProductsAnalyticsDto getTopProducts(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> shopIds,
            Integer limit) {

        log.info("Getting top {} products for tenant: {} (shops: {}, period: {} to {})",
                limit, tenantId, shopIds != null ? shopIds.size() : "all", startDate, endDate);

        // Default to last 30 days if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get shop IDs for the tenant
        List<String> targetShopIds = getTargetShopIds(tenantId, shopIds);

        if (targetShopIds.isEmpty()) {
            log.warn("No shops found for tenant: {}", tenantId);
            return TopProductsAnalyticsDto.builder()
                    .products(new ArrayList<>())
                    .totalProducts(0)
                    .build();
        }

        // Get top products
        String sql = """
            SELECT
                p.id as product_id,
                p.name as product_name,
                c.name as category,
                SUM(li.quantity) as quantity_sold,
                SUM(li.line_total) as revenue,
                AVG(li.unit_price) as average_price
            FROM sales_transactions st
            JOIN line_items li ON li.transaction_id = st.id
            JOIN products p ON p.id = li.product_id
            LEFT JOIN categories c ON c.id = p.category_id
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            GROUP BY p.id, p.name, c.name
            ORDER BY quantity_sold DESC
            LIMIT ?
            """;

        String inClause = targetShopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(targetShopIds);
        params.add(startDateTime);
        params.add(endDateTime);
        params.add(limit);

        List<TopProductsAnalyticsDto.TopProduct> products = jdbcTemplate.query(finalSql, params.toArray(),
                (rs, rowNum) -> TopProductsAnalyticsDto.TopProduct.builder()
                        .productId(rs.getString("product_id"))
                        .productName(rs.getString("product_name"))
                        .category(rs.getString("category"))
                        .quantitySold(rs.getLong("quantity_sold"))
                        .revenue(rs.getBigDecimal("revenue"))
                        .averagePrice(rs.getBigDecimal("average_price"))
                        .build());

        // Get total unique products sold
        Integer totalProducts = getTotalProductCount(targetShopIds, startDateTime, endDateTime);

        return TopProductsAnalyticsDto.builder()
                .products(products)
                .totalProducts(totalProducts)
                .build();
    }

    /**
     * Get shop performance comparison for a tenant.
     *
     * @param tenantId Tenant ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @return Shop performance analytics
     */
    public ShopPerformanceAnalyticsDto getShopPerformance(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate) {

        log.info("Getting shop performance for tenant: {} (period: {} to {})",
                tenantId, startDate, endDate);

        // Default to last 30 days if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get all shops for the tenant
        List<String> targetShopIds = shopRepository.findByTenantId(tenantId).stream()
                .map(shop -> shop.getId())
                .collect(Collectors.toList());

        if (targetShopIds.isEmpty()) {
            log.warn("No shops found for tenant: {}", tenantId);
            return ShopPerformanceAnalyticsDto.builder()
                    .shops(new ArrayList<>())
                    .totalShops(0)
                    .bestPerformingShop(null)
                    .worstPerformingShop(null)
                    .build();
        }

        // Get performance metrics for each shop
        String sql = """
            SELECT
                s.id as shop_id,
                s.name as shop_name,
                COALESCE(SUM(st.total_amount), 0) as revenue,
                COUNT(st.id) as transaction_count,
                COALESCE(AVG(st.total_amount), 0) as average_order_value
            FROM shops s
            LEFT JOIN sales_transactions st ON st.shop_id = s.id
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            WHERE s.id IN (%s)
            GROUP BY s.id, s.name
            ORDER BY revenue DESC
            """;

        String inClause = targetShopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.add(startDateTime);
        params.add(endDateTime);
        params.addAll(targetShopIds);

        List<ShopPerformanceAnalyticsDto.ShopPerformance> shops = jdbcTemplate.query(finalSql, params.toArray(),
                (rs, rowNum) -> {
                    String shopId = rs.getString("shop_id");
                    String topProduct = getTopProductForShop(shopId, startDateTime, endDateTime);
                    BigDecimal growthPercentage = getShopGrowthPercentage(shopId, startDateTime, endDateTime);

                    return ShopPerformanceAnalyticsDto.ShopPerformance.builder()
                            .shopId(shopId)
                            .shopName(rs.getString("shop_name"))
                            .revenue(rs.getBigDecimal("revenue"))
                            .transactionCount(rs.getLong("transaction_count"))
                            .averageOrderValue(rs.getBigDecimal("average_order_value"))
                            .topProduct(topProduct)
                            .growthPercentage(growthPercentage)
                            .build();
                });

        ShopPerformanceAnalyticsDto.ShopPerformance bestShop = shops.isEmpty() ? null : shops.get(0);
        ShopPerformanceAnalyticsDto.ShopPerformance worstShop = shops.isEmpty() ? null : shops.get(shops.size() - 1);

        return ShopPerformanceAnalyticsDto.builder()
                .shops(shops)
                .totalShops(shops.size())
                .bestPerformingShop(bestShop)
                .worstPerformingShop(worstShop)
                .build();
    }

    /**
     * Export analytics data to CSV format.
     *
     * @param tenantId Tenant ID
     * @param startDate Start date (optional)
     * @param endDate End date (optional)
     * @param shopIds List of shop IDs to include (optional, null = all shops)
     * @return CSV content as string
     */
    public String exportAnalyticsToCSV(
            String tenantId,
            LocalDate startDate,
            LocalDate endDate,
            List<String> shopIds) {

        log.info("Exporting analytics to CSV for tenant: {} (shops: {}, period: {} to {})",
                tenantId, shopIds != null ? shopIds.size() : "all", startDate, endDate);

        // Default to last 30 days if not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Get shop IDs for the tenant
        List<String> targetShopIds = getTargetShopIds(tenantId, shopIds);

        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Shop Manager - Analytics Export\n");
        csv.append("Tenant ID,").append(tenantId).append("\n");
        csv.append("Period,").append(startDate).append(" to ").append(endDate).append("\n");
        csv.append("Generated,").append(LocalDateTime.now()).append("\n");
        csv.append("\n");

        if (targetShopIds.isEmpty()) {
            csv.append("No shops found for this tenant\n");
            return csv.toString();
        }

        // Revenue data
        csv.append("=== Daily Revenue ===\n");
        csv.append("Date,Revenue,Transactions,Average Order Value\n");

        List<RevenueAnalyticsDto.RevenueDataPoint> dataPoints = getDailyRevenueDataPoints(
                targetShopIds, startDateTime, endDateTime);

        for (RevenueAnalyticsDto.RevenueDataPoint dp : dataPoints) {
            BigDecimal aov = dp.getTransactionCount() > 0
                    ? dp.getRevenue().divide(BigDecimal.valueOf(dp.getTransactionCount()), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            csv.append(dp.getDate()).append(",")
                    .append(dp.getRevenue()).append(",")
                    .append(dp.getTransactionCount()).append(",")
                    .append(aov).append("\n");
        }

        csv.append("\n");

        // Shop performance
        csv.append("=== Shop Performance ===\n");
        csv.append("Shop Name,Revenue,Transactions,Average Order Value,Top Product\n");

        ShopPerformanceAnalyticsDto shopPerformance = getShopPerformance(tenantId, startDate, endDate);
        for (ShopPerformanceAnalyticsDto.ShopPerformance shop : shopPerformance.getShops()) {
            csv.append(shop.getShopName()).append(",")
                    .append(shop.getRevenue()).append(",")
                    .append(shop.getTransactionCount()).append(",")
                    .append(shop.getAverageOrderValue()).append(",")
                    .append(shop.getTopProduct() != null ? shop.getTopProduct() : "N/A").append("\n");
        }

        csv.append("\n");

        // Top products
        csv.append("=== Top Products ===\n");
        csv.append("Product Name,Category,Quantity Sold,Revenue,Average Price\n");

        TopProductsAnalyticsDto topProducts = getTopProducts(tenantId, startDate, endDate, shopIds, 20);
        for (TopProductsAnalyticsDto.TopProduct product : topProducts.getProducts()) {
            csv.append(product.getProductName()).append(",")
                    .append(product.getCategory() != null ? product.getCategory() : "Uncategorized").append(",")
                    .append(product.getQuantitySold()).append(",")
                    .append(product.getRevenue()).append(",")
                    .append(product.getAveragePrice()).append("\n");
        }

        return csv.toString();
    }

    // ==================== Helper Methods ====================

    /**
     * Get target shop IDs - either provided list or all shops for tenant
     */
    private List<String> getTargetShopIds(String tenantId, List<String> shopIds) {
        if (shopIds != null && !shopIds.isEmpty()) {
            return shopIds;
        }
        return shopRepository.findByTenantId(tenantId).stream()
                .map(shop -> shop.getId())
                .collect(Collectors.toList());
    }

    /**
     * Get daily revenue data points for the given period
     */
    private List<RevenueAnalyticsDto.RevenueDataPoint> getDailyRevenueDataPoints(
            List<String> shopIds, LocalDateTime startDate, LocalDateTime endDate) {

        String sql = """
            SELECT
                DATE(st.transaction_date) as date,
                SUM(st.total_amount) as revenue,
                COUNT(*) as transaction_count
            FROM sales_transactions st
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            GROUP BY DATE(st.transaction_date)
            ORDER BY DATE(st.transaction_date)
            """;

        String inClause = shopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(shopIds);
        params.add(startDate);
        params.add(endDate);

        return jdbcTemplate.query(finalSql, params.toArray(),
                (rs, rowNum) -> RevenueAnalyticsDto.RevenueDataPoint.builder()
                        .date(rs.getDate("date").toLocalDate())
                        .revenue(rs.getBigDecimal("revenue"))
                        .transactionCount(rs.getLong("transaction_count"))
                        .build());
    }

    /**
     * Get total revenue for a period
     */
    private BigDecimal getTotalRevenue(List<String> shopIds, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COALESCE(SUM(st.total_amount), 0) as total_revenue
            FROM sales_transactions st
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            """;

        String inClause = shopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(shopIds);
        params.add(startDate);
        params.add(endDate);

        BigDecimal result = jdbcTemplate.queryForObject(finalSql, params.toArray(), BigDecimal.class);
        return result != null ? result : BigDecimal.ZERO;
    }

    /**
     * Calculate growth percentage
     */
    private BigDecimal calculateGrowthPercentage(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Build empty revenue analytics response
     */
    private RevenueAnalyticsDto buildEmptyRevenueAnalytics() {
        return RevenueAnalyticsDto.builder()
                .dataPoints(new ArrayList<>())
                .totalRevenue(BigDecimal.ZERO)
                .totalTransactions(0L)
                .averageOrderValue(BigDecimal.ZERO)
                .previousPeriodRevenue(BigDecimal.ZERO)
                .growthPercentage(BigDecimal.ZERO)
                .build();
    }

    /**
     * Build empty sales metrics response
     */
    private SalesMetricsDto buildEmptySalesMetrics() {
        return SalesMetricsDto.builder()
                .totalSales(0L)
                .totalRevenue(BigDecimal.ZERO)
                .averageOrderValue(BigDecimal.ZERO)
                .topSellingDay(LocalDate.now())
                .peakHour(12)
                .previousPeriodSales(0L)
                .salesGrowth(BigDecimal.ZERO)
                .build();
    }

    /**
     * Get total transaction count for a period
     */
    private Long getTotalTransactionCount(List<String> shopIds, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COUNT(*) as count
            FROM sales_transactions st
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
            """;

        String inClause = shopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(shopIds);
        params.add(startDate);
        params.add(endDate);

        Long result = jdbcTemplate.queryForObject(finalSql, params.toArray(), Long.class);
        return result != null ? result : 0L;
    }

    /**
     * Get the top selling day in the period
     */
    private LocalDate getTopSellingDay(List<String> shopIds, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT DATE(st.transaction_date) as date
            FROM sales_transactions st
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            GROUP BY DATE(st.transaction_date)
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """;

        String inClause = shopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(shopIds);
        params.add(startDate);
        params.add(endDate);

        try {
            java.sql.Date result = jdbcTemplate.queryForObject(finalSql, params.toArray(), java.sql.Date.class);
            return result != null ? result.toLocalDate() : LocalDate.now();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    /**
     * Get the peak hour (0-23) for sales activity
     */
    private Integer getPeakHour(List<String> shopIds, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT EXTRACT(HOUR FROM st.transaction_date) as hour
            FROM sales_transactions st
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            GROUP BY EXTRACT(HOUR FROM st.transaction_date)
            ORDER BY COUNT(*) DESC
            LIMIT 1
            """;

        String inClause = shopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(shopIds);
        params.add(startDate);
        params.add(endDate);

        try {
            Double result = jdbcTemplate.queryForObject(finalSql, params.toArray(), Double.class);
            return result != null ? result.intValue() : 12;
        } catch (Exception e) {
            return 12;
        }
    }

    /**
     * Get total count of unique products sold
     */
    private Integer getTotalProductCount(List<String> shopIds, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT COUNT(DISTINCT li.product_id) as count
            FROM sales_transactions st
            JOIN line_items li ON li.transaction_id = st.id
            WHERE st.shop_id IN (%s)
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            """;

        String inClause = shopIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String finalSql = String.format(sql, inClause);

        List<Object> params = new ArrayList<>();
        params.addAll(shopIds);
        params.add(startDate);
        params.add(endDate);

        Integer result = jdbcTemplate.queryForObject(finalSql, params.toArray(), Integer.class);
        return result != null ? result : 0;
    }

    /**
     * Get the top selling product for a specific shop
     */
    private String getTopProductForShop(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT p.name
            FROM sales_transactions st
            JOIN line_items li ON li.transaction_id = st.id
            JOIN products p ON p.id = li.product_id
            WHERE st.shop_id = ?
                AND st.transaction_date >= ?
                AND st.transaction_date <= ?
                AND st.status = 'COMPLETED'
                AND st.is_voided = false
            GROUP BY p.id, p.name
            ORDER BY SUM(li.quantity) DESC
            LIMIT 1
            """;

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{shopId, startDate, endDate}, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get growth percentage for a specific shop
     */
    private BigDecimal getShopGrowthPercentage(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate period length
        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        LocalDateTime previousStart = startDate.minusDays(periodDays);
        LocalDateTime previousEnd = startDate.minusSeconds(1);

        // Get current period revenue
        BigDecimal currentRevenue = getTotalRevenue(List.of(shopId), startDate, endDate);

        // Get previous period revenue
        BigDecimal previousRevenue = getTotalRevenue(List.of(shopId), previousStart, previousEnd);

        return calculateGrowthPercentage(currentRevenue, previousRevenue);
    }
}
