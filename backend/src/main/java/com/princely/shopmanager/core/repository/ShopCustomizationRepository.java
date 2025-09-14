package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.ShopCustomization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing ShopCustomization entities.
 *
 * Provides data access methods for shop customization settings including:
 * - CRUD operations for customizations
 * - Queries for theme and branding preferences
 * - Bulk operations for managing multiple shop customizations
 */
@Repository
public interface ShopCustomizationRepository extends JpaRepository<ShopCustomization, String> {

    /**
     * Finds customization settings for a specific shop.
     *
     * @param shop The shop entity
     * @return Optional containing the customization if found
     */
    Optional<ShopCustomization> findByShop(Shop shop);

    /**
     * Finds customization settings by shop ID.
     *
     * @param shopId The shop ID
     * @return Optional containing the customization if found
     */
    @Query("SELECT sc FROM ShopCustomization sc WHERE sc.shop.id = :shopId")
    Optional<ShopCustomization> findByShopId(@Param("shopId") String shopId);

    /**
     * Finds all customizations with a specific theme variant.
     *
     * @param themeVariant The theme variant to search for
     * @return List of customizations with the specified theme
     */
    List<ShopCustomization> findByThemeVariant(ShopCustomization.ThemeVariant themeVariant);

    /**
     * Finds all customizations that have a custom logo configured.
     *
     * @return List of customizations with logo URLs
     */
    @Query("SELECT sc FROM ShopCustomization sc WHERE sc.logoUrl IS NOT NULL AND sc.logoUrl != ''")
    List<ShopCustomization> findAllWithCustomLogos();

    /**
     * Finds all customizations that have custom colors configured.
     *
     * @return List of customizations with custom color schemes
     */
    @Query("SELECT sc FROM ShopCustomization sc WHERE sc.primaryColor IS NOT NULL OR sc.secondaryColor IS NOT NULL")
    List<ShopCustomization> findAllWithCustomColors();

    /**
     * Finds all customizations with dark theme enabled.
     *
     * @return List of dark theme customizations
     */
    @Query("SELECT sc FROM ShopCustomization sc WHERE sc.themeVariant = 'DARK'")
    List<ShopCustomization> findAllWithDarkTheme();

    /**
     * Checks if a shop has customization settings.
     *
     * @param shop The shop to check
     * @return true if customization exists, false otherwise
     */
    boolean existsByShop(Shop shop);

    /**
     * Counts total number of shops with customizations.
     *
     * @return Count of shops with customization settings
     */
    @Query("SELECT COUNT(sc) FROM ShopCustomization sc")
    long countShopsWithCustomizations();

    /**
     * Finds customizations by dashboard layout preference.
     *
     * @param layout The dashboard layout type
     * @return List of customizations with the specified layout
     */
    List<ShopCustomization> findByDashboardLayout(ShopCustomization.DashboardLayout layout);

    /**
     * Finds all customizations with custom styles defined.
     *
     * @return List of customizations with custom CSS styles
     */
    @Query("SELECT sc FROM ShopCustomization sc WHERE sc.customStyles IS NOT NULL AND sc.customStyles != ''")
    List<ShopCustomization> findAllWithCustomStyles();

    /**
     * Deletes customization for a specific shop.
     *
     * @param shop The shop whose customization should be deleted
     */
    void deleteByShop(Shop shop);
}