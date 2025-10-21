package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating shop configuration settings.
 *
 * This DTO contains business-related settings for a shop including
 * feature toggles, currency, tax rates, and discount limits.
 * All fields are optional to allow partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating shop configuration settings")
public class ShopConfigurationRequest {

    @Schema(description = "Enable investment tracking features", example = "true")
    private Boolean investmentEnabled;

    @Schema(description = "Enable analytics features", example = "true")
    private Boolean analyticsEnabled;

    @Schema(description = "Enable fraud detection features", example = "false")
    private Boolean fraudDetectionEnabled;

    @Schema(description = "Enable automatic backup", example = "true")
    private Boolean autoBackupEnabled;

    @Schema(description = "Shop currency code", example = "NGN")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g., NGN, USD)")
    private String currency;

    @Schema(description = "Tax rate as percentage", example = "7.5")
    @DecimalMin(value = "0.0", message = "Tax rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Tax rate cannot exceed 100%")
    private Double taxRate;

    @Schema(description = "Maximum allowed discount percentage", example = "20.0")
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100%")
    private Double maxDiscountPercentage;

    @Schema(description = "Receipt footer text", example = "Thank you for your patronage!")
    @Size(max = 500, message = "Receipt footer must not exceed 500 characters")
    private String receiptFooter;

    /**
     * Applies the configuration request to an existing ShopConfiguration.
     * Only updates fields that are not null in the request.
     *
     * @param config The existing configuration to update
     */
    public void applyTo(ShopConfiguration config) {
        if (investmentEnabled != null) config.setInvestmentEnabled(investmentEnabled);
        if (analyticsEnabled != null) config.setAnalyticsEnabled(analyticsEnabled);
        if (fraudDetectionEnabled != null) config.setFraudDetectionEnabled(fraudDetectionEnabled);
        if (autoBackupEnabled != null) config.setAutoBackupEnabled(autoBackupEnabled);
        if (currency != null) config.setCurrency(currency.toUpperCase());
        if (taxRate != null) config.setTaxRate(taxRate);
        if (maxDiscountPercentage != null) config.setMaxDiscountPercentage(maxDiscountPercentage);
        if (receiptFooter != null) config.setReceiptFooter(receiptFooter);
    }

    /**
     * Converts this request to a ShopConfiguration entity.
     *
     * @return ShopConfiguration with values from this request
     */
    public ShopConfiguration toEntity() {
        ShopConfiguration config = new ShopConfiguration();
        applyTo(config);
        return config;
    }
}
