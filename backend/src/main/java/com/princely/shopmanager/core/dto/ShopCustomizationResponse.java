package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopCustomization;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for shop customization response.
 *
 * This DTO represents the shop customization settings returned in API responses,
 * including branding, visual elements, and UI preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shop customization settings response")
public class ShopCustomizationResponse {

    @Schema(description = "Customization ID")
    private String id;

    @Schema(description = "Shop ID")
    private String shopId;

    // Brand Colors
    @Schema(description = "Primary brand color", example = "#007bff")
    private String primaryColor;

    @Schema(description = "Secondary brand color", example = "#6c757d")
    private String secondaryColor;

    @Schema(description = "Accent color", example = "#28a745")
    private String accentColor;

    @Schema(description = "Background color", example = "#ffffff")
    private String backgroundColor;

    @Schema(description = "Text color", example = "#212529")
    private String textColor;

    // Logos and Images
    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "Favicon URL")
    private String faviconUrl;

    @Schema(description = "Banner image URL")
    private String bannerImageUrl;

    @Schema(description = "Background image URL")
    private String backgroundImageUrl;

    // Contact and Website
    @Schema(description = "Website URL")
    private String websiteUrl;

    @Schema(description = "Social media links (JSON)")
    private String socialMediaLinks;

    // Theme Settings
    @Schema(description = "Theme variant", example = "LIGHT")
    private String themeVariant;

    @Schema(description = "Font family", example = "Inter, sans-serif")
    private String fontFamily;

    @Schema(description = "Font size", example = "MEDIUM")
    private String fontSize;

    // Layout and Styling
    @Schema(description = "Border radius in pixels", example = "8")
    private Integer borderRadius;

    @Schema(description = "Custom styles (JSON)")
    private String customStyles;

    @Schema(description = "Dashboard layout", example = "GRID")
    private String dashboardLayout;

    // Receipt Customization
    @Schema(description = "Receipt header text")
    private String receiptHeader;

    @Schema(description = "Receipt footer text")
    private String receiptFooter;

    @Schema(description = "Show logo on receipts", example = "true")
    private Boolean receiptShowLogo;

    // Feature Toggles
    @Schema(description = "Show shop banner", example = "true")
    private Boolean showBanner;

    @Schema(description = "Enable animations", example = "true")
    private Boolean enableAnimations;

    @Schema(description = "Show advanced features", example = "false")
    private Boolean showAdvancedFeatures;

    // Computed Fields
    @Schema(description = "Whether dark theme is enabled", example = "false")
    private Boolean isDarkTheme;

    @Schema(description = "Whether custom logo is configured", example = "true")
    private Boolean hasCustomLogo;

    @Schema(description = "Whether custom styles are applied", example = "false")
    private Boolean hasCustomStyles;

    /**
     * Factory method to create ShopCustomizationResponse from ShopCustomization entity.
     *
     * @param customization The shop customization entity to convert
     * @return ShopCustomizationResponse DTO with mapped data
     */
    public static ShopCustomizationResponse fromEntity(ShopCustomization customization) {
        if (customization == null) {
            return null;
        }

        return ShopCustomizationResponse.builder()
            .id(customization.getId())
            .shopId(customization.getShop() != null ? customization.getShop().getId() : null)
            // Use default getters for colors to ensure non-null values
            .primaryColor(customization.getPrimaryColorWithDefault())
            .secondaryColor(customization.getSecondaryColorWithDefault())
            .accentColor(customization.getAccentColorWithDefault())
            .backgroundColor(customization.getBackgroundColorWithDefault())
            .textColor(customization.getTextColorWithDefault())
            .logoUrl(customization.getLogoUrl())
            .faviconUrl(customization.getFaviconUrl())
            .bannerImageUrl(customization.getBannerImageUrl())
            .backgroundImageUrl(customization.getBackgroundImageUrl())
            .websiteUrl(customization.getWebsiteUrl())
            .socialMediaLinks(customization.getSocialMediaLinks())
            .themeVariant(customization.getThemeVariant() != null ? customization.getThemeVariant().name() : null)
            .fontFamily(customization.getFontFamily())
            .fontSize(customization.getFontSize() != null ? customization.getFontSize().name() : null)
            .borderRadius(customization.getBorderRadius())
            .customStyles(customization.getCustomStyles())
            .dashboardLayout(customization.getDashboardLayout() != null ? customization.getDashboardLayout().name() : null)
            .receiptHeader(customization.getReceiptHeader())
            .receiptFooter(customization.getReceiptFooter())
            .receiptShowLogo(customization.getReceiptShowLogo())
            .showBanner(customization.getShowBanner())
            .enableAnimations(customization.getEnableAnimations())
            .showAdvancedFeatures(customization.getShowAdvancedFeatures())
            // Computed fields
            .isDarkTheme(customization.isDarkTheme())
            .hasCustomLogo(customization.hasCustomLogo())
            .hasCustomStyles(customization.hasCustomStyles())
            .build();
    }
}
