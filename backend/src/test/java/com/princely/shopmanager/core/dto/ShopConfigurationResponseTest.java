package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ShopConfigurationResponse DTO.
 *
 * Tests entity-to-DTO mapping and default value handling.
 */
@DisplayName("ShopConfigurationResponse Tests")
class ShopConfigurationResponseTest {

    @Test
    @DisplayName("Should create response from entity")
    void shouldCreateResponseFromEntity() {
        ShopConfiguration config = new ShopConfiguration();
        config.setInvestmentEnabled(true);
        config.setAnalyticsEnabled(false);
        config.setFraudDetectionEnabled(true);
        config.setAutoBackupEnabled(false);
        config.setCurrency("USD");
        config.setTaxRate(8.5);
        config.setMaxDiscountPercentage(25.0);
        config.setReceiptFooter("Thank you!");

        ShopConfigurationResponse response = ShopConfigurationResponse.fromEntity(config);

        assertThat(response.isInvestmentEnabled()).isTrue();
        assertThat(response.isAnalyticsEnabled()).isFalse();
        assertThat(response.isFraudDetectionEnabled()).isTrue();
        assertThat(response.isAutoBackupEnabled()).isFalse();
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getTaxRate()).isEqualTo(8.5);
        assertThat(response.getMaxDiscountPercentage()).isEqualTo(25.0);
        assertThat(response.getReceiptFooter()).isEqualTo("Thank you!");
    }

    @Test
    @DisplayName("Should return default values when entity is null")
    void shouldReturnDefaultValuesForNullEntity() {
        ShopConfigurationResponse response = ShopConfigurationResponse.fromEntity(null);

        assertThat(response.isInvestmentEnabled()).isTrue();
        assertThat(response.isAnalyticsEnabled()).isTrue();
        assertThat(response.isFraudDetectionEnabled()).isFalse();
        assertThat(response.isAutoBackupEnabled()).isTrue();
        assertThat(response.getCurrency()).isEqualTo("NGN");
        assertThat(response.getTaxRate()).isEqualTo(0.0);
        assertThat(response.getMaxDiscountPercentage()).isEqualTo(20.0);
        assertThat(response.getReceiptFooter()).isNull();
    }

    @Test
    @DisplayName("Should handle entity with default constructor values")
    void shouldHandleEntityWithDefaultValues() {
        ShopConfiguration config = new ShopConfiguration();

        ShopConfigurationResponse response = ShopConfigurationResponse.fromEntity(config);

        assertThat(response.isInvestmentEnabled()).isTrue();
        assertThat(response.isAnalyticsEnabled()).isTrue();
        assertThat(response.isFraudDetectionEnabled()).isFalse();
        assertThat(response.isAutoBackupEnabled()).isTrue();
        assertThat(response.getCurrency()).isEqualTo("NGN");
        assertThat(response.getTaxRate()).isEqualTo(0.0);
        assertThat(response.getMaxDiscountPercentage()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Should preserve all field values during mapping")
    void shouldPreserveAllFieldValues() {
        ShopConfiguration config = new ShopConfiguration();
        config.setInvestmentEnabled(false);
        config.setAnalyticsEnabled(false);
        config.setFraudDetectionEnabled(false);
        config.setAutoBackupEnabled(false);
        config.setCurrency("EUR");
        config.setTaxRate(0.0);
        config.setMaxDiscountPercentage(0.0);
        config.setReceiptFooter("");

        ShopConfigurationResponse response = ShopConfigurationResponse.fromEntity(config);

        assertThat(response.isInvestmentEnabled()).isFalse();
        assertThat(response.isAnalyticsEnabled()).isFalse();
        assertThat(response.isFraudDetectionEnabled()).isFalse();
        assertThat(response.isAutoBackupEnabled()).isFalse();
        assertThat(response.getCurrency()).isEqualTo("EUR");
        assertThat(response.getTaxRate()).isEqualTo(0.0);
        assertThat(response.getMaxDiscountPercentage()).isEqualTo(0.0);
        assertThat(response.getReceiptFooter()).isEmpty();
    }
}
