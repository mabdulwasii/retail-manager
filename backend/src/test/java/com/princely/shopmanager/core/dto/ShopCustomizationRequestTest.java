package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopCustomization;
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
 * Unit tests for ShopCustomizationRequest DTO.
 *
 * Tests validation rules, mapping methods, and edge cases.
 */
@DisplayName("ShopCustomizationRequest Tests")
class ShopCustomizationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Color Validation Tests")
    class ColorValidationTests {

        @Test
        @DisplayName("Should accept valid hex colors")
        void shouldAcceptValidHexColors() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#007bff")
                .secondaryColor("#6c757d")
                .accentColor("#28a745")
                .backgroundColor("#ffffff")
                .textColor("#212529")
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should accept uppercase hex colors")
        void shouldAcceptUppercaseHexColors() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#FF5733")
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject invalid hex color format")
        void shouldRejectInvalidHexColor() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("007bff") // Missing #
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("must be a valid hex color");
        }

        @Test
        @DisplayName("Should reject short hex color")
        void shouldRejectShortHexColor() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#FFF") // Too short
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("Should reject long hex color")
        void shouldRejectLongHexColor() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#FF5733AA") // Too long
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
        }

        @Test
        @DisplayName("Should reject invalid characters in hex color")
        void shouldRejectInvalidCharactersInHexColor() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#GGGGGG") // Invalid characters
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    @DisplayName("URL Validation Tests")
    class URLValidationTests {

        @Test
        @DisplayName("Should accept valid URLs")
        void shouldAcceptValidURLs() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .logoUrl("https://cdn.example.com/logo.png")
                .faviconUrl("https://cdn.example.com/favicon.ico")
                .bannerImageUrl("https://cdn.example.com/banner.jpg")
                .backgroundImageUrl("https://cdn.example.com/bg.jpg")
                .websiteUrl("https://www.myshop.com")
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject URL exceeding max length")
        void shouldRejectLongURL() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .logoUrl("https://example.com/" + "a".repeat(500)) // Exceeds 500 chars
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 500 characters");
        }
    }

    @Nested
    @DisplayName("Enum Validation Tests")
    class EnumValidationTests {

        @Test
        @DisplayName("Should accept valid theme variant")
        void shouldAcceptValidThemeVariant() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .themeVariant("LIGHT")
                .build();

            assertThat(request.getThemeVariant()).isEqualTo("LIGHT");
        }

        @Test
        @DisplayName("Should accept valid font size")
        void shouldAcceptValidFontSize() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .fontSize("MEDIUM")
                .build();

            assertThat(request.getFontSize()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("Should accept valid dashboard layout")
        void shouldAcceptValidDashboardLayout() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .dashboardLayout("GRID")
                .build();

            assertThat(request.getDashboardLayout()).isEqualTo("GRID");
        }
    }

    @Nested
    @DisplayName("Text Field Validation Tests")
    class TextFieldValidationTests {

        @Test
        @DisplayName("Should accept valid receipt header")
        void shouldAcceptValidReceiptHeader() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .receiptHeader("Welcome to our store!")
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject receipt header exceeding max length")
        void shouldRejectLongReceiptHeader() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .receiptHeader("A".repeat(1001)) // Exceeds 1000 chars
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .contains("must not exceed 1000 characters");
        }

        @Test
        @DisplayName("Should accept valid font family")
        void shouldAcceptValidFontFamily() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .fontFamily("Inter, sans-serif")
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject font family exceeding max length")
        void shouldRejectLongFontFamily() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .fontFamily("A".repeat(101)) // Exceeds 100 chars
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Mapping Tests")
    class MappingTests {

        @Test
        @DisplayName("Should apply customization to existing entity")
        void shouldApplyToExistingEntity() {
            ShopCustomization existing = ShopCustomization.builder()
                .primaryColor("#000000")
                .themeVariant(ShopCustomization.ThemeVariant.LIGHT)
                .fontSize(ShopCustomization.FontSize.SMALL)
                .build();

            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#FF5733")
                .themeVariant("DARK")
                .fontSize("LARGE")
                .build();

            request.applyTo(existing);

            assertThat(existing.getPrimaryColor()).isEqualTo("#FF5733");
            assertThat(existing.getThemeVariant()).isEqualTo(ShopCustomization.ThemeVariant.DARK);
            assertThat(existing.getFontSize()).isEqualTo(ShopCustomization.FontSize.LARGE);
        }

        @Test
        @DisplayName("Should convert enum strings to uppercase")
        void shouldConvertEnumsToUppercase() {
            ShopCustomization existing = ShopCustomization.builder().build();

            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .themeVariant("light")
                .fontSize("medium")
                .dashboardLayout("grid")
                .build();

            request.applyTo(existing);

            assertThat(existing.getThemeVariant()).isEqualTo(ShopCustomization.ThemeVariant.LIGHT);
            assertThat(existing.getFontSize()).isEqualTo(ShopCustomization.FontSize.MEDIUM);
            assertThat(existing.getDashboardLayout()).isEqualTo(ShopCustomization.DashboardLayout.GRID);
        }

        @Test
        @DisplayName("Should not override fields with null values")
        void shouldNotOverrideWithNullValues() {
            ShopCustomization existing = ShopCustomization.builder()
                .primaryColor("#FF5733")
                .secondaryColor("#6c757d")
                .logoUrl("https://example.com/logo.png")
                .themeVariant(ShopCustomization.ThemeVariant.DARK)
                .build();

            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor(null)
                .secondaryColor(null)
                .logoUrl(null)
                .themeVariant(null)
                .build();

            request.applyTo(existing);

            assertThat(existing.getPrimaryColor()).isEqualTo("#FF5733");
            assertThat(existing.getSecondaryColor()).isEqualTo("#6c757d");
            assertThat(existing.getLogoUrl()).isEqualTo("https://example.com/logo.png");
            assertThat(existing.getThemeVariant()).isEqualTo(ShopCustomization.ThemeVariant.DARK);
        }

        @Test
        @DisplayName("Should apply all color fields")
        void shouldApplyAllColorFields() {
            ShopCustomization existing = ShopCustomization.builder().build();

            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .primaryColor("#007bff")
                .secondaryColor("#6c757d")
                .accentColor("#28a745")
                .backgroundColor("#ffffff")
                .textColor("#212529")
                .build();

            request.applyTo(existing);

            assertThat(existing.getPrimaryColor()).isEqualTo("#007bff");
            assertThat(existing.getSecondaryColor()).isEqualTo("#6c757d");
            assertThat(existing.getAccentColor()).isEqualTo("#28a745");
            assertThat(existing.getBackgroundColor()).isEqualTo("#ffffff");
            assertThat(existing.getTextColor()).isEqualTo("#212529");
        }

        @Test
        @DisplayName("Should apply all boolean toggles")
        void shouldApplyAllBooleanToggles() {
            ShopCustomization existing = ShopCustomization.builder().build();

            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .receiptShowLogo(false)
                .showBanner(false)
                .enableAnimations(false)
                .showAdvancedFeatures(true)
                .build();

            request.applyTo(existing);

            assertThat(existing.getReceiptShowLogo()).isFalse();
            assertThat(existing.getShowBanner()).isFalse();
            assertThat(existing.getEnableAnimations()).isFalse();
            assertThat(existing.getShowAdvancedFeatures()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should accept null values for all optional fields")
        void shouldAcceptAllNullValues() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder().build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .fontFamily("")
                .receiptHeader("")
                .receiptFooter("")
                .build();

            Set<ConstraintViolation<ShopCustomizationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle zero border radius")
        void shouldHandleZeroBorderRadius() {
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .borderRadius(0)
                .build();

            ShopCustomization entity = ShopCustomization.builder().build();
            request.applyTo(entity);

            assertThat(entity.getBorderRadius()).isZero();
        }

        @Test
        @DisplayName("Should handle complex social media JSON")
        void shouldHandleComplexSocialMediaJSON() {
            String socialMediaJson = "{\"facebook\":\"https://facebook.com/shop\",\"instagram\":\"@myshop\",\"twitter\":\"@myshop\"}";
            ShopCustomizationRequest request = ShopCustomizationRequest.builder()
                .socialMediaLinks(socialMediaJson)
                .build();

            ShopCustomization entity = ShopCustomization.builder().build();
            request.applyTo(entity);

            assertThat(entity.getSocialMediaLinks()).isEqualTo(socialMediaJson);
        }
    }
}
