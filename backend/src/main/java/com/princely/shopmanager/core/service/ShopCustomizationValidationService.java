package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.ShopCustomization;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for validating shop customization data.
 *
 * This service provides:
 * - Color validation (hexadecimal format)
 * - URL validation (HTTP/HTTPS)
 * - Comprehensive customization settings validation
 */
@Service
@Slf4j
public class ShopCustomizationValidationService {

    /**
     * Validates all customization settings.
     *
     * @param customization The customization to validate
     * @throws IllegalArgumentException if any validation fails
     */
    public void validateCustomization(ShopCustomization customization) {
        log.debug("Validating shop customization settings");

        if (customization.getPrimaryColor() != null) {
            validateColor(customization.getPrimaryColor(), "Primary color");
        }
        if (customization.getSecondaryColor() != null) {
            validateColor(customization.getSecondaryColor(), "Secondary color");
        }
        if (customization.getAccentColor() != null) {
            validateColor(customization.getAccentColor(), "Accent color");
        }
        if (customization.getBackgroundColor() != null) {
            validateColor(customization.getBackgroundColor(), "Background color");
        }
        if (customization.getTextColor() != null) {
            validateColor(customization.getTextColor(), "Text color");
        }
        if (customization.getWebsiteUrl() != null) {
            validateUrl(customization.getWebsiteUrl(), "Website URL");
        }

        log.debug("Shop customization validation completed successfully");
    }

    /**
     * Validates a color value (hexadecimal format).
     *
     * @param color The color to validate
     * @param fieldName The name of the field being validated
     * @throws IllegalArgumentException if color format is invalid
     */
    public void validateColor(String color, String fieldName) {
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException(fieldName + " must be in valid hexadecimal format (e.g., #FF5733)");
        }
    }

    /**
     * Validates a URL.
     *
     * @param url The URL to validate
     * @param fieldName The name of the field being validated
     * @throws IllegalArgumentException if URL format is invalid
     */
    public void validateUrl(String url, String fieldName) {
        if (url != null && !url.matches("^https?://.*")) {
            throw new IllegalArgumentException(fieldName + " must be a valid HTTP or HTTPS URL");
        }
    }
}