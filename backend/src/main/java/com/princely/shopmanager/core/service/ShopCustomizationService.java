package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.ShopCustomization;
import com.princely.shopmanager.core.repository.ShopCustomizationRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.service.AuditService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing shop customization settings.
 *
 * This service provides comprehensive functionality for:
 * - Managing shop branding and visual customization
 * - Handling logo and image uploads
 * - Updating theme and styling preferences
 * - Validating customization settings
 * - Audit logging for customization changes
 *
 * All operations are tenant-aware and include proper security checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopCustomizationService {

    public static final String SHOP_NOT_FOUND = "Shop not found: ";
    private final ShopCustomizationRepository customizationRepository;
    private final ShopRepository shopRepository;
    private final AuditService auditService;

    // Configuration for file uploads
    private static final String UPLOAD_DIR = "uploads/shop-assets/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/svg+xml"};

    /**
     * Retrieves the customization settings for a specific shop.
     *
     * @param shopId The ID of the shop
     * @return Optional containing the shop customization if found
     * @throws IllegalArgumentException if shop is not found
     */
    @Transactional(readOnly = true)
    public Optional<ShopCustomization> getShopCustomization(String shopId) {
        log.debug("Retrieving customization for shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException(SHOP_NOT_FOUND + shopId));

        Optional<ShopCustomization> customization = customizationRepository.findByShop(shop);

        if (customization.isPresent()) {
            log.debug("Found customization for shop: {}", shopId);
        } else {
            log.debug("No customization found for shop: {}, will use defaults", shopId);
        }

        return customization;
    }

    /**
     * Retrieves customization for the current tenant shop.
     *
     * @return Optional containing the current shop's customization
     * @throws IllegalStateException if no tenant context is available
     */
    @Transactional(readOnly = true)
    public Optional<ShopCustomization> getCurrentShopCustomization() {
        String tenantId = TenantContext.requireCurrentTenant();
        return this.getShopCustomization(tenantId);
    }

    /**
     * Creates or updates customization settings for a shop.
     *
     * @param shopId The shop ID
     * @param customization The customization settings to apply
     * @return The saved customization entity
     * @throws IllegalArgumentException if shop is not found or customization is invalid
     */
    @Transactional
    public ShopCustomization saveShopCustomization(String shopId, ShopCustomization customization) {
        log.info("Saving customization for shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException(SHOP_NOT_FOUND + shopId));

        // Validate customization settings
        validateCustomization(customization);

        // Check if customization already exists
        Optional<ShopCustomization> existingCustomization = customizationRepository.findByShop(shop);

        ShopCustomization savedCustomization;
        if (existingCustomization.isPresent()) {
            // Update existing customization
            ShopCustomization existing = existingCustomization.get();
            updateCustomizationFields(existing, customization);
            savedCustomization = customizationRepository.save(existing);
            log.info("Updated existing customization for shop: {}", shopId);
        } else {
            // Create new customization
            customization.setShop(shop);
            savedCustomization = customizationRepository.save(customization);
            log.info("Created new customization for shop: {}", shopId);
        }

        // Audit log the customization change
        auditService.logDataModification(
            shop,
            TenantContext.getCurrentUserId(),
            TenantContext.getCurrentUserName(),
            existingCustomization.isPresent() ?
                com.princely.shopmanager.shared.domain.AuditLog.ActionType.UPDATE :
                com.princely.shopmanager.shared.domain.AuditLog.ActionType.CREATE,
            "ShopCustomization",
            savedCustomization.getId(),
            "Shop customization modified",
            null,
            null
        );

        return savedCustomization;
    }

    /**
     * Updates color scheme for a shop.
     *
     * @param shopId The shop ID
     * @param primaryColor Primary brand color
     * @param secondaryColor Secondary brand color
     * @param accentColor Accent color for highlights
     * @return Updated customization
     */
    @Transactional
    public ShopCustomization updateColorScheme(String shopId, String primaryColor,
                                             String secondaryColor, String accentColor) {
        log.info("Updating color scheme for shop: {}", shopId);

        ShopCustomization customization = getOrCreateCustomization(shopId);

        // Validate colors
        if (primaryColor != null) {
            validateColor(primaryColor, "Primary color");
            customization.setPrimaryColor(primaryColor);
        }

        if (secondaryColor != null) {
            validateColor(secondaryColor, "Secondary color");
            customization.setSecondaryColor(secondaryColor);
        }

        if (accentColor != null) {
            validateColor(accentColor, "Accent color");
            customization.setAccentColor(accentColor);
        }

        return customizationRepository.save(customization);
    }

    /**
     * Updates theme settings for a shop.
     *
     * @param shopId The shop ID
     * @param themeVariant The theme variant (LIGHT, DARK, AUTO)
     * @param fontSize The font size preference
     * @return Updated customization
     */
    @Transactional
    public ShopCustomization updateThemeSettings(String shopId,
                                                ShopCustomization.ThemeVariant themeVariant,
                                                ShopCustomization.FontSize fontSize) {
        log.info("Updating theme settings for shop: {}", shopId);

        ShopCustomization customization = getOrCreateCustomization(shopId);

        if (themeVariant != null) {
            customization.setThemeVariant(themeVariant);
        }

        if (fontSize != null) {
            customization.setFontSize(fontSize);
        }

        return customizationRepository.save(customization);
    }

    /**
     * Uploads and sets a logo for a shop.
     *
     * @param shopId The shop ID
     * @param logoFile The logo file to upload
     * @return Updated customization with new logo URL
     * @throws IOException if file upload fails
     * @throws IllegalArgumentException if file is invalid
     */
    @Transactional
    public ShopCustomization uploadLogo(String shopId, MultipartFile logoFile) throws IOException {
        log.info("Uploading logo for shop: {}", shopId);

        // Validate file
        validateImageFile(logoFile, "logo");

        // Generate unique filename
        String originalFilename = logoFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = shopId + "_logo_" + UUID.randomUUID() + "." + fileExtension;

        // Save file to filesystem
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update customization with new logo URL
        ShopCustomization customization = getOrCreateCustomization(shopId);
        String logoUrl = "/api/assets/" + uniqueFilename;
        customization.setLogoUrl(logoUrl);

        ShopCustomization saved = customizationRepository.save(customization);

        log.info("Logo uploaded successfully for shop: {} at path: {}", shopId, logoUrl);
        return saved;
    }

    /**
     * Updates website and contact information for a shop.
     *
     * @param shopId The shop ID
     * @param websiteUrl The shop's website URL
     * @param socialMediaLinks Social media links in JSON format
     * @return Updated customization
     */
    @Transactional
    public ShopCustomization updateContactInfo(String shopId, String websiteUrl, String socialMediaLinks) {
        log.info("Updating contact info for shop: {}", shopId);

        ShopCustomization customization = getOrCreateCustomization(shopId);

        if (websiteUrl != null) {
            validateUrl(websiteUrl, "Website URL");
            customization.setWebsiteUrl(websiteUrl);
        }

        if (socialMediaLinks != null) {
            customization.setSocialMediaLinks(socialMediaLinks);
        }

        return customizationRepository.save(customization);
    }

    /**
     * Resets customization to default settings.
     *
     * @param shopId The shop ID
     * @return Reset customization with default values
     */
    @Transactional
    public ShopCustomization resetToDefaults(String shopId) {
        log.info("Resetting customization to defaults for shop: {}", shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException(SHOP_NOT_FOUND + shopId));

        // Delete existing customization if it exists
        customizationRepository.findByShop(shop).ifPresent(customizationRepository::delete);

        // Create new default customization
        ShopCustomization defaultCustomization = ShopCustomization.builder()
            .shop(shop)
            .themeVariant(ShopCustomization.ThemeVariant.LIGHT)
            .fontSize(ShopCustomization.FontSize.MEDIUM)
            .dashboardLayout(ShopCustomization.DashboardLayout.GRID)
            .showBanner(true)
            .enableAnimations(true)
            .showAdvancedFeatures(false)
            .receiptShowLogo(true)
            .build();

        return customizationRepository.save(defaultCustomization);
    }

    /**
     * Gets or creates a customization entity for a shop.
     * This is a helper method to ensure customization always exists.
     */
    private ShopCustomization getOrCreateCustomization(String shopId) {
        Optional<ShopCustomization> existing = getShopCustomization(shopId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new customization with defaults
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException(SHOP_NOT_FOUND + shopId));

        return ShopCustomization.builder()
            .shop(shop)
            .themeVariant(ShopCustomization.ThemeVariant.LIGHT)
            .fontSize(ShopCustomization.FontSize.MEDIUM)
            .dashboardLayout(ShopCustomization.DashboardLayout.GRID)
            .showBanner(true)
            .enableAnimations(true)
            .showAdvancedFeatures(false)
            .receiptShowLogo(true)
            .build();
    }

    /**
     * Updates customization fields from source to target.
     */
    private void updateCustomizationFields(ShopCustomization target, ShopCustomization source) {
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

    /**
     * Validates customization settings.
     */
    private void validateCustomization(ShopCustomization customization) {
        if (customization.getPrimaryColor() != null) {
            validateColor(customization.getPrimaryColor(), "Primary color");
        }
        if (customization.getSecondaryColor() != null) {
            validateColor(customization.getSecondaryColor(), "Secondary color");
        }
        if (customization.getWebsiteUrl() != null) {
            validateUrl(customization.getWebsiteUrl(), "Website URL");
        }
    }

    /**
     * Validates a color value (hexadecimal format).
     */
    private void validateColor(String color, String fieldName) {
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException(fieldName + " must be in valid hexadecimal format (e.g., #FF5733)");
        }
    }

    /**
     * Validates a URL.
     */
    private void validateUrl(String url, String fieldName) {
        if (url != null && !url.matches("^https?://.*")) {
            throw new IllegalArgumentException(fieldName + " must be a valid HTTP or HTTPS URL");
        }
    }

    /**
     * Validates an uploaded image file.
     */
    private void validateImageFile(MultipartFile file, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileType + " file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(fileType + " file size cannot exceed " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException(fileType + " file must be a valid image (JPEG, PNG, GIF, or SVG)");
        }
    }

    /**
     * Checks if the content type is allowed for image uploads.
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}