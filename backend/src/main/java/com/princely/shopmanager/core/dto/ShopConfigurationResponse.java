package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for shop configuration response.
 *
 * This DTO represents the shop configuration settings returned in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shop configuration settings response")
public class ShopConfigurationResponse {

    @Schema(description = "Investment tracking features enabled", example = "true")
    private boolean investmentEnabled;

    @Schema(description = "Analytics features enabled", example = "true")
    private boolean analyticsEnabled;

    @Schema(description = "Fraud detection features enabled", example = "false")
    private boolean fraudDetectionEnabled;

    @Schema(description = "Automatic backup enabled", example = "true")
    private boolean autoBackupEnabled;

    @Schema(description = "Shop currency code", example = "NGN")
    private String currency;

    @Schema(description = "Tax rate as percentage", example = "7.5")
    private Double taxRate;

    @Schema(description = "Maximum allowed discount percentage", example = "20.0")
    private Double maxDiscountPercentage;

    @Schema(description = "Receipt footer text", example = "Thank you for your patronage!")
    private String receiptFooter;

    /**
     * Factory method to create ShopConfigurationResponse from ShopConfiguration entity.
     *
     * @param config The shop configuration entity to convert
     * @return ShopConfigurationResponse DTO with mapped data
     */
    public static ShopConfigurationResponse fromEntity(ShopConfiguration config) {
        if (config == null) {
            // Return default configuration
            return ShopConfigurationResponse.builder()
                .investmentEnabled(true)
                .analyticsEnabled(true)
                .fraudDetectionEnabled(false)
                .autoBackupEnabled(true)
                .currency("NGN")
                .taxRate(0.0)
                .maxDiscountPercentage(20.0)
                .build();
        }

        return ShopConfigurationResponse.builder()
            .investmentEnabled(config.isInvestmentEnabled())
            .analyticsEnabled(config.isAnalyticsEnabled())
            .fraudDetectionEnabled(config.isFraudDetectionEnabled())
            .autoBackupEnabled(config.isAutoBackupEnabled())
            .currency(config.getCurrency())
            .taxRate(config.getTaxRate())
            .maxDiscountPercentage(config.getMaxDiscountPercentage())
            .receiptFooter(config.getReceiptFooter())
            .build();
    }
}
