package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.ShopCustomization;
import com.princely.shopmanager.core.repository.ShopCustomizationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing shop theme and branding operations.
 *
 * This service provides:
 * - Color scheme management
 * - Theme settings configuration
 * - Visual customization updates
 * - Contact and branding information management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopThemeService {

    private final ShopCustomizationRepository customizationRepository;
    private final ShopCustomizationValidationService validationService;

    /**
     * Updates color scheme for a shop customization.
     *
     * @param customization The customization to update
     * @param primaryColor Primary brand color
     * @param secondaryColor Secondary brand color
     * @param accentColor Accent color for highlights
     * @return Updated customization
     */
    @Transactional
    public ShopCustomization updateColorScheme(ShopCustomization customization, String primaryColor,
                                             String secondaryColor, String accentColor) {
        log.info("Updating color scheme for shop customization: {}", customization.getId());

        // Validate and set colors
        if (primaryColor != null) {
            validationService.validateColor(primaryColor, "Primary color");
            customization.setPrimaryColor(primaryColor);
        }

        if (secondaryColor != null) {
            validationService.validateColor(secondaryColor, "Secondary color");
            customization.setSecondaryColor(secondaryColor);
        }

        if (accentColor != null) {
            validationService.validateColor(accentColor, "Accent color");
            customization.setAccentColor(accentColor);
        }

        return customizationRepository.save(customization);
    }

    /**
     * Updates theme settings for a shop customization.
     *
     * @param customization The customization to update
     * @param themeVariant The theme variant (LIGHT, DARK, AUTO)
     * @param fontSize The font size preference
     * @return Updated customization
     */
    @Transactional
    public ShopCustomization updateThemeSettings(ShopCustomization customization,
                                                ShopCustomization.ThemeVariant themeVariant,
                                                ShopCustomization.FontSize fontSize) {
        log.info("Updating theme settings for shop customization: {}", customization.getId());

        if (themeVariant != null) {
            customization.setThemeVariant(themeVariant);
        }

        if (fontSize != null) {
            customization.setFontSize(fontSize);
        }

        return customizationRepository.save(customization);
    }

    /**
     * Updates website and contact information for a shop customization.
     *
     * @param customization The customization to update
     * @param websiteUrl The shop's website URL
     * @param socialMediaLinks Social media links in JSON format
     * @return Updated customization
     */
    @Transactional
    public ShopCustomization updateContactInfo(ShopCustomization customization, String websiteUrl, String socialMediaLinks) {
        log.info("Updating contact info for shop customization: {}", customization.getId());

        if (websiteUrl != null) {
            validationService.validateUrl(websiteUrl, "Website URL");
            customization.setWebsiteUrl(websiteUrl);
        }

        if (socialMediaLinks != null) {
            customization.setSocialMediaLinks(socialMediaLinks);
        }

        return customizationRepository.save(customization);
    }

    /**
     * Updates customization fields from source to target.
     *
     * @param target The customization to update
     * @param source The source customization with new values
     */
    public void updateCustomizationFields(ShopCustomization target, ShopCustomization source) {
        if (source.getPrimaryColor() != null) target.setPrimaryColor(source.getPrimaryColor());
        if (source.getSecondaryColor() != null) target.setSecondaryColor(source.getSecondaryColor());
        if (source.getAccentColor() != null) target.setAccentColor(source.getAccentColor());
        if (source.getBackgroundColor() != null) target.setBackgroundColor(source.getBackgroundColor());
        if (source.getTextColor() != null) target.setTextColor(source.getTextColor());
        if (source.getLogoUrl() != null) target.setLogoUrl(source.getLogoUrl());
        if (source.getFaviconUrl() != null) target.setFaviconUrl(source.getFaviconUrl());
        if (source.getBannerImageUrl() != null) target.setBannerImageUrl(source.getBannerImageUrl());
        if (source.getBackgroundImageUrl() != null) target.setBackgroundImageUrl(source.getBackgroundImageUrl());
        if (source.getWebsiteUrl() != null) target.setWebsiteUrl(source.getWebsiteUrl());
        if (source.getSocialMediaLinks() != null) target.setSocialMediaLinks(source.getSocialMediaLinks());
        if (source.getThemeVariant() != null) target.setThemeVariant(source.getThemeVariant());
        if (source.getFontFamily() != null) target.setFontFamily(source.getFontFamily());
        if (source.getFontSize() != null) target.setFontSize(source.getFontSize());
        if (source.getBorderRadius() != null) target.setBorderRadius(source.getBorderRadius());
        if (source.getCustomStyles() != null) target.setCustomStyles(source.getCustomStyles());
        if (source.getDashboardLayout() != null) target.setDashboardLayout(source.getDashboardLayout());
        if (source.getReceiptHeader() != null) target.setReceiptHeader(source.getReceiptHeader());
        if (source.getReceiptFooter() != null) target.setReceiptFooter(source.getReceiptFooter());
        if (source.getReceiptShowLogo() != null) target.setReceiptShowLogo(source.getReceiptShowLogo());
        if (source.getShowBanner() != null) target.setShowBanner(source.getShowBanner());
        if (source.getEnableAnimations() != null) target.setEnableAnimations(source.getEnableAnimations());
        if (source.getShowAdvancedFeatures() != null) target.setShowAdvancedFeatures(source.getShowAdvancedFeatures());
    }
}