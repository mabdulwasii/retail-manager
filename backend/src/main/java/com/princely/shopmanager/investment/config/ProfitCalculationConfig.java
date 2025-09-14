package com.princely.shopmanager.investment.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "business.profit")
@ConditionalOnProperty(name = "features.investment.configurable-profit.enabled", havingValue = "true", matchIfMissing = true)
@Data
public class ProfitCalculationConfig {
    private BigDecimal defaultProfitMargin = BigDecimal.valueOf(0.30);
    private Map<String, BigDecimal> categoryProfitMargins = new HashMap<>();
    private BigDecimal operationalCostPercentage = BigDecimal.valueOf(0.15);
    private BigDecimal minimumProfitThreshold = BigDecimal.valueOf(100);

    public BigDecimal getProfitMarginForCategory(String categoryId) {
        return categoryProfitMargins.getOrDefault(categoryId, defaultProfitMargin);
    }
}