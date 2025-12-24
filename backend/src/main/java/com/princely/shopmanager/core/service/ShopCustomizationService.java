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
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing shop customization settings.
 *
 * This service provides comprehensive functionality for:
 * - Managing shop branding and visual customization
 * - Handling logo and image uploads via ShopImageUploadService
 * - Updating theme and styling preferences via ShopThemeService
 * - Validating customization settings via ShopCustomizationValidationService
 * - Audit logging for customization changes
 *
 * All operations are tenant-aware and include proper security checks.
 * This service now delegates specialized operations to focused services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopCustomizationService {

    public static final String SHOP_NOT_FOUND = "Shop not found: ";
    private final ShopCustomizationRepository customizationRepository;
    private final ShopRepository shopRepository;
    private final AuditService auditService;
    private final ShopImageUploadService imageUploadService;
    private final ShopThemeService themeService;
    private final ShopCustomizationValidationService validationService;

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
        validationService.validateCustomization(customization);

        // Check if customization already exists
        Optional<ShopCustomization> existingCustomization = customizationRepository.findByShop(shop);

        ShopCustomization savedCustomization;
        if (existingCustomization.isPresent()) {
            // Update existing customization
            ShopCustomization existing = existingCustomization.get();
            themeService.updateCustomizationFields(existing, customization);
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
        return themeService.updateColorScheme(customization, primaryColor, secondaryColor, accentColor);
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
        return themeService.updateThemeSettings(customization, themeVariant, fontSize);
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

        // Upload the image file
        String logoUrl = imageUploadService.uploadImage(shopId, logoFile, "logo");

        // Update customization with new logo URL
        ShopCustomization customization = getOrCreateCustomization(shopId);
        customization.setLogoUrl(logoUrl);

        return customizationRepository.save(customization);
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
        return themeService.updateContactInfo(customization, websiteUrl, socialMediaLinks);
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
        customizationRepository.findByShop(shop).ifPresent(existing -> {
            customizationRepository.delete(existing);
            // Flush to avoid unique constraint violation on shop_id
            customizationRepository.flush();
        });

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

}