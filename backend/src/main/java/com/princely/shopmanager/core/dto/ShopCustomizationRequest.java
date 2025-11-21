package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.ShopCustomization;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating shop customization settings.
 *
 * This DTO contains branding and visual customization settings for a shop
 * including colors, logos, themes, and layout preferences.
 * All fields are optional to allow partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating shop customization settings")
public class ShopCustomizationRequest {

    // Brand Colors
    @Schema(description = "Primary brand color in hexadecimal format", example = "#007bff")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hex color (e.g., #007bff)")
    private String primaryColor;

    @Schema(description = "Secondary brand color in hexadecimal format", example = "#6c757d")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Secondary color must be a valid hex color")
    private String secondaryColor;

    @Schema(description = "Accent color for highlights and buttons", example = "#28a745")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Accent color must be a valid hex color")
    private String accentColor;

    @Schema(description = "Background color for the main interface", example = "#ffffff")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Background color must be a valid hex color")
    private String backgroundColor;

    @Schema(description = "Text color for primary content", example = "#212529")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Text color must be a valid hex color")
    private String textColor;

    // Logos and Images
    @Schema(description = "URL or path to the shop's logo image", example = "https://cdn.example.com/logo.png")
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Schema(description = "URL or path to the shop's favicon", example = "https://cdn.example.com/favicon.ico")
    @Size(max = 500, message = "Favicon URL must not exceed 500 characters")
    private String faviconUrl;

    @Schema(description = "URL or path to the shop's banner image", example = "https://cdn.example.com/banner.jpg")
    @Size(max = 500, message = "Banner image URL must not exceed 500 characters")
    private String bannerImageUrl;

    @Schema(description = "URL or path to the shop's background image", example = "https://cdn.example.com/bg.jpg")
    @Size(max = 500, message = "Background image URL must not exceed 500 characters")
    private String backgroundImageUrl;

    // Contact and Website
    @Schema(description = "Shop's website URL", example = "https://www.myshop.com")
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    private String websiteUrl;

    @Schema(description = "Social media links in JSON format",
            example = "{\"facebook\":\"https://facebook.com/myshop\",\"instagram\":\"@myshop\"}")
    private String socialMediaLinks;

    // Theme Settings
    @Schema(description = "Theme variant", example = "LIGHT", allowableValues = {"LIGHT", "DARK", "AUTO"})
    private String themeVariant;

    @Schema(description = "Font family for the UI", example = "Inter, sans-serif")
    @Size(max = 100, message = "Font family must not exceed 100 characters")
    private String fontFamily;

    @Schema(description = "Font size preference", example = "MEDIUM", allowableValues = {"SMALL", "MEDIUM", "LARGE"})
    private String fontSize;

    // Layout and Styling
    @Schema(description = "Border radius for UI elements in pixels", example = "8")
    private Integer borderRadius;

    @Schema(description = "Custom CSS styles in JSON format for advanced customization")
    private String customStyles;

    @Schema(description = "Layout preference for the dashboard", example = "GRID",
            allowableValues = {"GRID", "LIST", "CARD"})
    private String dashboardLayout;

    // Receipt Customization
    @Schema(description = "Header text for receipts", example = "Welcome to Our Store!")
    @Size(max = 1000, message = "Receipt header must not exceed 1000 characters")
    private String receiptHeader;

    @Schema(description = "Footer text for receipts", example = "Thank you for your business!")
    @Size(max = 1000, message = "Receipt footer must not exceed 1000 characters")
    private String receiptFooter;

    @Schema(description = "Whether to show logo on receipts", example = "true")
    private Boolean receiptShowLogo;

    // Feature Toggles
    @Schema(description = "Whether to show the shop banner", example = "true")
    private Boolean showBanner;

    @Schema(description = "Whether to enable animations in the UI", example = "true")
    private Boolean enableAnimations;

    @Schema(description = "Whether to show advanced features in the UI", example = "false")
    private Boolean showAdvancedFeatures;

    /**
     * Applies the customization request to an existing ShopCustomization entity.
     * Only updates fields that are not null in the request.
     *
     * @param customization The existing customization to update
     */
    public void applyTo(ShopCustomization customization) {
        if (primaryColor != null) customization.setPrimaryColor(primaryColor);
        if (secondaryColor != null) customization.setSecondaryColor(secondaryColor);
        if (accentColor != null) customization.setAccentColor(accentColor);
        if (backgroundColor != null) customization.setBackgroundColor(backgroundColor);
        if (textColor != null) customization.setTextColor(textColor);

        if (logoUrl != null) customization.setLogoUrl(logoUrl);
        if (faviconUrl != null) customization.setFaviconUrl(faviconUrl);
        if (bannerImageUrl != null) customization.setBannerImageUrl(bannerImageUrl);
        if (backgroundImageUrl != null) customization.setBackgroundImageUrl(backgroundImageUrl);

        if (websiteUrl != null) customization.setWebsiteUrl(websiteUrl);
        if (socialMediaLinks != null) customization.setSocialMediaLinks(socialMediaLinks);

        if (themeVariant != null) {
            customization.setThemeVariant(ShopCustomization.ThemeVariant.valueOf(themeVariant.toUpperCase()));
        }
        if (fontFamily != null) customization.setFontFamily(fontFamily);
        if (fontSize != null) {
            customization.setFontSize(ShopCustomization.FontSize.valueOf(fontSize.toUpperCase()));
        }

        if (borderRadius != null) customization.setBorderRadius(borderRadius);
        if (customStyles != null) customization.setCustomStyles(customStyles);
        if (dashboardLayout != null) {
            customization.setDashboardLayout(ShopCustomization.DashboardLayout.valueOf(dashboardLayout.toUpperCase()));
        }

        if (receiptHeader != null) customization.setReceiptHeader(receiptHeader);
        if (receiptFooter != null) customization.setReceiptFooter(receiptFooter);
        if (receiptShowLogo != null) customization.setReceiptShowLogo(receiptShowLogo);

        if (showBanner != null) customization.setShowBanner(showBanner);
        if (enableAnimations != null) customization.setEnableAnimations(enableAnimations);
        if (showAdvancedFeatures != null) customization.setShowAdvancedFeatures(showAdvancedFeatures);
    }
}
