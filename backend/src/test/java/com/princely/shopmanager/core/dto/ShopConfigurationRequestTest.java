package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopConfiguration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ShopConfigurationRequest DTO.
 *
 * Tests validation rules, mapping methods, and edge cases.
 */
@DisplayName("ShopConfigurationRequest Tests")
class ShopConfigurationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid configuration request")
        void shouldAcceptValidConfiguration() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .investmentEnabled(true)
                .analyticsEnabled(true)
                .fraudDetectionEnabled(false)
                .autoBackupEnabled(true)
                .currency("NGN")
                .taxRate(7.5)
                .maxDiscountPercentage(20.0)
                .receiptFooter("Thank you!")
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject invalid currency code")
        void shouldRejectInvalidCurrencyCode() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .currency("US") // Too short
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Currency must be a 3-letter code");
        }

        @Test
        @DisplayName("Should reject negative tax rate")
        void shouldRejectNegativeTaxRate() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .taxRate(-5.0)
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Tax rate must be non-negative");
        }

        @Test
        @DisplayName("Should reject tax rate over 100%")
        void shouldRejectTaxRateOver100() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .taxRate(105.0)
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Tax rate cannot exceed 100%");
        }

        @Test
        @DisplayName("Should reject negative discount percentage")
        void shouldRejectNegativeDiscountPercentage() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .maxDiscountPercentage(-10.0)
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Discount percentage must be non-negative");
        }

        @Test
        @DisplayName("Should reject discount percentage over 100%")
        void shouldRejectDiscountPercentageOver100() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .maxDiscountPercentage(150.0)
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Discount percentage cannot exceed 100%");
        }

        @Test
        @DisplayName("Should reject receipt footer exceeding max length")
        void shouldRejectLongReceiptFooter() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .receiptFooter("A".repeat(501)) // Exceeds 500 character limit
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("Receipt footer must not exceed 500 characters");
        }

        @Test
        @DisplayName("Should accept null values for optional fields")
        void shouldAcceptNullValues() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder().build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mapping Tests")
    class MappingTests {

        @Test
        @DisplayName("Should apply configuration to existing entity")
        void shouldApplyToExistingEntity() {
            ShopConfiguration existing = new ShopConfiguration();
            existing.setInvestmentEnabled(false);
            existing.setAnalyticsEnabled(false);
            existing.setCurrency("USD");
            existing.setTaxRate(0.0);

            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .investmentEnabled(true)
                .currency("NGN")
                .taxRate(7.5)
                .build();

            request.applyTo(existing);

            assertThat(existing.isInvestmentEnabled()).isTrue();
            assertThat(existing.isAnalyticsEnabled()).isFalse(); // Not changed
            assertThat(existing.getCurrency()).isEqualTo("NGN");
            assertThat(existing.getTaxRate()).isEqualTo(7.5);
        }

        @Test
        @DisplayName("Should convert currency to uppercase when applying")
        void shouldConvertCurrencyToUppercase() {
            ShopConfiguration config = new ShopConfiguration();

            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .currency("usd")
                .build();

            request.applyTo(config);

            assertThat(config.getCurrency()).isEqualTo("USD");
        }

        @Test
        @DisplayName("Should convert to entity")
        void shouldConvertToEntity() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .investmentEnabled(true)
                .analyticsEnabled(true)
                .fraudDetectionEnabled(true)
                .autoBackupEnabled(false)
                .currency("EUR")
                .taxRate(20.0)
                .maxDiscountPercentage(15.0)
                .receiptFooter("Thank you for shopping!")
                .build();

            ShopConfiguration entity = request.toEntity();

            assertThat(entity.isInvestmentEnabled()).isTrue();
            assertThat(entity.isAnalyticsEnabled()).isTrue();
            assertThat(entity.isFraudDetectionEnabled()).isTrue();
            assertThat(entity.isAutoBackupEnabled()).isFalse();
            assertThat(entity.getCurrency()).isEqualTo("EUR");
            assertThat(entity.getTaxRate()).isEqualTo(20.0);
            assertThat(entity.getMaxDiscountPercentage()).isEqualTo(15.0);
            assertThat(entity.getReceiptFooter()).isEqualTo("Thank you for shopping!");
        }

        @Test
        @DisplayName("Should not override fields with null values")
        void shouldNotOverrideWithNullValues() {
            ShopConfiguration existing = new ShopConfiguration();
            existing.setInvestmentEnabled(true);
            existing.setAnalyticsEnabled(true);
            existing.setCurrency("NGN");
            existing.setTaxRate(7.5);

            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .investmentEnabled(null)
                .analyticsEnabled(null)
                .currency(null)
                .taxRate(null)
                .build();

            request.applyTo(existing);

            assertThat(existing.isInvestmentEnabled()).isTrue();
            assertThat(existing.isAnalyticsEnabled()).isTrue();
            assertThat(existing.getCurrency()).isEqualTo("NGN");
            assertThat(existing.getTaxRate()).isEqualTo(7.5);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle zero tax rate")
        void shouldHandleZeroTaxRate() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .taxRate(0.0)
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle 100% tax rate")
        void shouldHandle100PercentTaxRate() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .taxRate(100.0)
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty receipt footer")
        void shouldHandleEmptyReceiptFooter() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .receiptFooter("")
                .build();

            Set<ConstraintViolation<ShopConfigurationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle all boolean combinations")
        void shouldHandleAllBooleanCombinations() {
            ShopConfigurationRequest request = ShopConfigurationRequest.builder()
                .investmentEnabled(true)
                .analyticsEnabled(false)
                .fraudDetectionEnabled(true)
                .autoBackupEnabled(false)
                .build();

            ShopConfiguration entity = request.toEntity();

            assertThat(entity.isInvestmentEnabled()).isTrue();
            assertThat(entity.isAnalyticsEnabled()).isFalse();
            assertThat(entity.isFraudDetectionEnabled()).isTrue();
            assertThat(entity.isAutoBackupEnabled()).isFalse();
        }
    }
}
