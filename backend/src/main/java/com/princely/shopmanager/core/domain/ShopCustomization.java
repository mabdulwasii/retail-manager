package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing shop customization settings including branding, UI themes, and visual elements.
 * This entity stores all customization preferences for a shop including:
 * - Brand colors and styling
 * - Logo and image assets
 * - Website and contact information
 * - UI theme preferences
 * - Custom styling options
 * Each shop can have one customization configuration that defines their unique brand identity.
 */
@Entity
@Table(name = "shop_customizations", indexes = {
    @Index(name = "idx_customization_shop", columnList = "shop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop"})
public class ShopCustomization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * The shop this customization belongs to (one-to-one relationship)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false, unique = true)
    private Shop shop;

    // Brand Identity
    /**
     * Primary brand color in hexadecimal format (e.g., #FF5733)
     */
    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    /**
     * Secondary brand color in hexadecimal format
     */
    @Column(name = "secondary_color", length = 7)
    private String secondaryColor;

    /**
     * Accent color for highlights and buttons
     */
    @Column(name = "accent_color", length = 7)
    private String accentColor;

    /**
     * Background color for the main interface
     */
    @Column(name = "background_color", length = 7)
    private String backgroundColor;

    /**
     * Text color for primary content
     */
    @Column(name = "text_color", length = 7)
    private String textColor;

    // Logo and Images
    /**
     * URL or path to the shop's logo image
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * URL or path to the shop's favicon
     */
    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    /**
     * URL or path to the shop's banner/header image
     */
    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    /**
     * URL or path to the shop's background image
     */
    @Column(name = "background_image_url", length = 500)
    private String backgroundImageUrl;

    // Contact and Website Information
    /**
     * Shop's website URL
     */
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    /**
     * Social media links in JSON format
     */
    @Column(name = "social_media_links", columnDefinition = "TEXT")
    private String socialMediaLinks;

    // UI Theme Settings
    /**
     * Theme variant (light, dark, auto)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "theme_variant", nullable = false)
    private ThemeVariant themeVariant = ThemeVariant.LIGHT;

    /**
     * Font family for the UI
     */
    @Column(name = "font_family", length = 100)
    private String fontFamily;

    /**
     * Font size preference (small, medium, large)
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "font_size")
    private FontSize fontSize = FontSize.MEDIUM;

    // Layout and Styling
    /**
     * Border radius for UI elements (in pixels)
     */
    @Column(name = "border_radius")
    private Integer borderRadius;

    /**
     * Custom CSS styles in JSON format for advanced customization
     */
    @Column(name = "custom_styles", columnDefinition = "TEXT")
    private String customStyles;

    /**
     * Layout preference for the dashboard
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "dashboard_layout")
    private DashboardLayout dashboardLayout = DashboardLayout.GRID;

    // Receipt Customization
    /**
     * Header text for receipts
     */
    @Column(name = "receipt_header", length = 1000)
    private String receiptHeader;

    /**
     * Footer text for receipts
     */
    @Column(name = "receipt_footer", length = 1000)
    private String receiptFooter;

    /**
     * Whether to show logo on receipts
     */
    @Column(name = "receipt_show_logo")
    @Builder.Default
    private Boolean receiptShowLogo = true;

    // Feature Toggles for UI Elements
    /**
     * Whether to show the shop banner
     */
    @Column(name = "show_banner")
    @Builder.Default
    private Boolean showBanner = true;

    /**
     * Whether to enable animations in the UI
     */
    @Builder.Default
    @Column(name = "enable_animations")
    private Boolean enableAnimations = true;

    /**
     * Whether to show advanced features in the UI
     */
    @Builder.Default
    @Column(name = "show_advanced_features")
    private Boolean showAdvancedFeatures = false;

    /**
     * Theme variant options for the user interface
     */
    @Getter
    public enum ThemeVariant {
        LIGHT("Light theme with bright background"),
        DARK("Dark theme with dark background"),
        AUTO("Automatically switch based on system preference");

        private final String description;

        ThemeVariant(String description) {
            this.description = description;
        }

    }

    /**
     * Font size options for the user interface
     */
    @Getter
    public enum FontSize {
        SMALL("Small font size"),
        MEDIUM("Medium font size (default)"),
        LARGE("Large font size for accessibility");

        private final String description;

        FontSize(String description) {
            this.description = description;
        }

    }

    /**
     * Dashboard layout options
     */
    @Getter
    public enum DashboardLayout {
        GRID("Grid-based dashboard layout"),
        LIST("List-based dashboard layout"),
        CARD("Card-based dashboard layout");

        private final String description;

        DashboardLayout(String description) {
            this.description = description;
        }

    }

    /**
     * Checks if dark theme is enabled
     * @return true if theme is dark, false otherwise
     */
    public boolean isDarkTheme() {
        return themeVariant == ThemeVariant.DARK;
    }

    /**
     * Gets the primary color with fallback to default
     * @return primary color or default blue if not set
     */
    public String getPrimaryColorWithDefault() {
        return primaryColor != null ? primaryColor : "#007bff";
    }

    /**
     * Gets the secondary color with fallback to default
     * @return secondary color or default gray if not set
     */
    public String getSecondaryColorWithDefault() {
        return secondaryColor != null ? secondaryColor : "#6c757d";
    }

    /**
     * Gets the accent color with fallback to default
     * @return accent color or default green if not set
     */
    public String getAccentColorWithDefault() {
        return accentColor != null ? accentColor : "#28a745";
    }

    /**
     * Gets the background color with fallback to default
     * @return background color or default white if not set
     */
    public String getBackgroundColorWithDefault() {
        return backgroundColor != null ? backgroundColor : "#ffffff";
    }

    /**
     * Gets the text color with fallback to default
     * @return text color or default dark gray if not set
     */
    public String getTextColorWithDefault() {
        return textColor != null ? textColor : "#212529";
    }

    /**
     * Checks if custom logo is configured
     * @return true if logo URL is set, false otherwise
     */
    public boolean hasCustomLogo() {
        return logoUrl != null && !logoUrl.trim().isEmpty();
    }

    /**
     * Checks if custom styling is applied
     * @return true if custom styles are defined, false otherwise
     */
    public boolean hasCustomStyles() {
        return customStyles != null && !customStyles.trim().isEmpty();
    }
}