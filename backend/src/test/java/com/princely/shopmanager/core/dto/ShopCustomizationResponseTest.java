package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.ShopCustomization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ShopCustomizationResponse DTO.
 *
 * Tests entity-to-DTO mapping and null handling.
 */
@DisplayName("ShopCustomizationResponse Tests")
class ShopCustomizationResponseTest {

    @Test
    @DisplayName("Should create response from entity with all fields")
    void shouldCreateResponseFromCompleteEntity() {
        Shop shop = Shop.builder().id("shop-123").build();

        ShopCustomization customization = ShopCustomization.builder()
            .id("custom-123")
            .shop(shop)
            .primaryColor("#007bff")
            .secondaryColor("#6c757d")
            .accentColor("#28a745")
            .backgroundColor("#ffffff")
            .textColor("#212529")
            .logoUrl("https://cdn.example.com/logo.png")
            .faviconUrl("https://cdn.example.com/favicon.ico")
            .bannerImageUrl("https://cdn.example.com/banner.jpg")
            .backgroundImageUrl("https://cdn.example.com/bg.jpg")
            .websiteUrl("https://www.myshop.com")
            .socialMediaLinks("{\"facebook\":\"@myshop\"}")
            .themeVariant(ShopCustomization.ThemeVariant.DARK)
            .fontFamily("Inter, sans-serif")
            .fontSize(ShopCustomization.FontSize.LARGE)
            .borderRadius(8)
            .customStyles("{\"border\":\"solid\"}")
            .dashboardLayout(ShopCustomization.DashboardLayout.GRID)
            .receiptHeader("Welcome!")
            .receiptFooter("Thank you!")
            .receiptShowLogo(true)
            .showBanner(false)
            .enableAnimations(true)
            .showAdvancedFeatures(false)
            .build();

        ShopCustomizationResponse response = ShopCustomizationResponse.fromEntity(customization);

        assertThat(response.getId()).isEqualTo("custom-123");
        assertThat(response.getShopId()).isEqualTo("shop-123");
        assertThat(response.getPrimaryColor()).isEqualTo("#007bff");
        assertThat(response.getSecondaryColor()).isEqualTo("#6c757d");
        assertThat(response.getAccentColor()).isEqualTo("#28a745");
        assertThat(response.getBackgroundColor()).isEqualTo("#ffffff");
        assertThat(response.getTextColor()).isEqualTo("#212529");
        assertThat(response.getLogoUrl()).isEqualTo("https://cdn.example.com/logo.png");
        assertThat(response.getFaviconUrl()).isEqualTo("https://cdn.example.com/favicon.ico");
        assertThat(response.getBannerImageUrl()).isEqualTo("https://cdn.example.com/banner.jpg");
        assertThat(response.getBackgroundImageUrl()).isEqualTo("https://cdn.example.com/bg.jpg");
        assertThat(response.getWebsiteUrl()).isEqualTo("https://www.myshop.com");
        assertThat(response.getSocialMediaLinks()).isEqualTo("{\"facebook\":\"@myshop\"}");
        assertThat(response.getThemeVariant()).isEqualTo("DARK");
        assertThat(response.getFontFamily()).isEqualTo("Inter, sans-serif");
        assertThat(response.getFontSize()).isEqualTo("LARGE");
        assertThat(response.getBorderRadius()).isEqualTo(8);
        assertThat(response.getCustomStyles()).isEqualTo("{\"border\":\"solid\"}");
        assertThat(response.getDashboardLayout()).isEqualTo("GRID");
        assertThat(response.getReceiptHeader()).isEqualTo("Welcome!");
        assertThat(response.getReceiptFooter()).isEqualTo("Thank you!");
        assertThat(response.getReceiptShowLogo()).isTrue();
        assertThat(response.getShowBanner()).isFalse();
        assertThat(response.getEnableAnimations()).isTrue();
        assertThat(response.getShowAdvancedFeatures()).isFalse();
    }

    @Test
    @DisplayName("Should return null for null entity")
    void shouldReturnNullForNullEntity() {
        ShopCustomizationResponse response = ShopCustomizationResponse.fromEntity(null);

        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle entity with null optional fields")
    void shouldHandleEntityWithNullFields() {
        ShopCustomization customization = new ShopCustomization();
        customization.setId("custom-123");

        ShopCustomizationResponse response = ShopCustomizationResponse.fromEntity(customization);

        assertThat(response.getId()).isEqualTo("custom-123");
        assertThat(response.getShopId()).isNull();
        assertThat(response.getPrimaryColor()).isNull();
        assertThat(response.getLogoUrl()).isNull();
        // Note: ThemeVariant, FontSize, and DashboardLayout have defaults from @Builder.Default
        // so they won't be null even when using builder
    }

    @Test
    @DisplayName("Should convert enum values to strings")
    void shouldConvertEnumsToStrings() {
        ShopCustomization customization = ShopCustomization.builder()
            .themeVariant(ShopCustomization.ThemeVariant.AUTO)
            .fontSize(ShopCustomization.FontSize.SMALL)
            .dashboardLayout(ShopCustomization.DashboardLayout.LIST)
            .build();

        ShopCustomizationResponse response = ShopCustomizationResponse.fromEntity(customization);

        assertThat(response.getThemeVariant()).isEqualTo("AUTO");
        assertThat(response.getFontSize()).isEqualTo("SMALL");
        assertThat(response.getDashboardLayout()).isEqualTo("LIST");
    }

    @Test
    @DisplayName("Should preserve boolean values")
    void shouldPreserveBooleanValues() {
        ShopCustomization customization = ShopCustomization.builder()
            .receiptShowLogo(false)
            .showBanner(false)
            .enableAnimations(false)
            .showAdvancedFeatures(true)
            .build();

        ShopCustomizationResponse response = ShopCustomizationResponse.fromEntity(customization);

        assertThat(response.getReceiptShowLogo()).isFalse();
        assertThat(response.getShowBanner()).isFalse();
        assertThat(response.getEnableAnimations()).isFalse();
        assertThat(response.getShowAdvancedFeatures()).isTrue();
    }

    @Test
    @DisplayName("Should handle entity with shop but no shop ID")
    void shouldHandleEntityWithNullShopId() {
        Shop shop = Shop.builder().build();
        ShopCustomization customization = ShopCustomization.builder()
            .id("custom-123")
            .shop(shop)
            .build();

        ShopCustomizationResponse response = ShopCustomizationResponse.fromEntity(customization);

        assertThat(response.getShopId()).isNull();
    }
}
