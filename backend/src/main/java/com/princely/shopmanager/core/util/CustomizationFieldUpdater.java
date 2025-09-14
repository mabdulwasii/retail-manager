package com.princely.shopmanager.core.util;

import com.princely.shopmanager.core.domain.ShopCustomization;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Utility class for updating customization fields with reduced cognitive complexity.
 */
public final class CustomizationFieldUpdater {

    private CustomizationFieldUpdater() {
        // Private constructor to prevent instantiation
    }

    /**
     * Updates a field if the source value is not null.
     */
    public static <T> void updateIfNotNull(T sourceValue, BiConsumer<ShopCustomization, T> setter, ShopCustomization target) {
        if (sourceValue != null) {
            setter.accept(target, sourceValue);
        }
    }

    /**
     * Updates all customization fields from source to target.
     */
    public static void updateAllFields(ShopCustomization target, ShopCustomization source) {
        updateIfNotNull(source.getPrimaryColor(), ShopCustomization::setPrimaryColor, target);
        updateIfNotNull(source.getSecondaryColor(), ShopCustomization::setSecondaryColor, target);
        updateIfNotNull(source.getAccentColor(), ShopCustomization::setAccentColor, target);
        updateIfNotNull(source.getBackgroundColor(), ShopCustomization::setBackgroundColor, target);
        updateIfNotNull(source.getTextColor(), ShopCustomization::setTextColor, target);
        updateIfNotNull(source.getLogoUrl(), ShopCustomization::setLogoUrl, target);
        updateIfNotNull(source.getFaviconUrl(), ShopCustomization::setFaviconUrl, target);
        updateIfNotNull(source.getBannerImageUrl(), ShopCustomization::setBannerImageUrl, target);
        updateIfNotNull(source.getBackgroundImageUrl(), ShopCustomization::setBackgroundImageUrl, target);
        updateIfNotNull(source.getWebsiteUrl(), ShopCustomization::setWebsiteUrl, target);
        updateIfNotNull(source.getSocialMediaLinks(), ShopCustomization::setSocialMediaLinks, target);
        updateIfNotNull(source.getThemeVariant(), ShopCustomization::setThemeVariant, target);
        updateIfNotNull(source.getFontFamily(), ShopCustomization::setFontFamily, target);
        updateIfNotNull(source.getFontSize(), ShopCustomization::setFontSize, target);
        updateIfNotNull(source.getBorderRadius(), ShopCustomization::setBorderRadius, target);
        updateIfNotNull(source.getCustomStyles(), ShopCustomization::setCustomStyles, target);
        updateIfNotNull(source.getDashboardLayout(), ShopCustomization::setDashboardLayout, target);
        updateIfNotNull(source.getReceiptHeader(), ShopCustomization::setReceiptHeader, target);
        updateIfNotNull(source.getReceiptFooter(), ShopCustomization::setReceiptFooter, target);
        updateIfNotNull(source.getReceiptShowLogo(), ShopCustomization::setReceiptShowLogo, target);
        updateIfNotNull(source.getShowBanner(), ShopCustomization::setShowBanner, target);
        updateIfNotNull(source.getEnableAnimations(), ShopCustomization::setEnableAnimations, target);
    }
}