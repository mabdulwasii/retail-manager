package com.princely.shopmanager.embedded.repository;

import com.princely.shopmanager.embedded.domain.SystemSettings;
import com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SystemSettings entity
 *
 * Provides database access for system configuration settings in embedded mode.
 *
 * NOTE: This repository is ONLY used in embedded mode via @Profile("embedded")
 * in the SystemSettingsService.
 *
 * @see com.princely.shopmanager.embedded.domain.SystemSettings
 * @see com.princely.shopmanager.embedded.service.SystemSettingsService
 * @author Claude Code
 * @since 1.0.0
 */
@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, String> {

    /**
     * Find setting by its unique key
     *
     * @param key Setting key (e.g., "custom.domain")
     * @return Optional containing the setting if found
     */
    Optional<SystemSettings> findByKey(String key);

    /**
     * Find all settings in a specific category
     *
     * @param category Setting category (SYSTEM, DOMAIN, SYNC, etc.)
     * @return List of settings in the category
     */
    List<SystemSettings> findByCategory(SettingCategory category);

    /**
     * Find all settings in a specific category, ordered by key
     *
     * @param category Setting category
     * @return Ordered list of settings in the category
     */
    List<SystemSettings> findByCategoryOrderByKeyAsc(SettingCategory category);

    /**
     * Find all sensitive settings
     *
     * @param isSensitive Whether to find sensitive or non-sensitive settings
     * @return List of settings matching the sensitive filter
     */
    List<SystemSettings> findByIsSensitive(Boolean isSensitive);

    /**
     * Find all settings that require restart when modified
     *
     * @param requiresRestart Whether to find settings requiring restart
     * @return List of settings matching the restart requirement
     */
    List<SystemSettings> findByRequiresRestart(Boolean requiresRestart);

    /**
     * Check if a setting exists by key
     *
     * @param key Setting key
     * @return true if setting exists, false otherwise
     */
    boolean existsByKey(String key);

    /**
     * Count settings in a specific category
     *
     * @param category Setting category
     * @return Number of settings in the category
     */
    long countByCategory(SettingCategory category);

    /**
     * Find all modified settings (where value != defaultValue)
     *
     * @return List of modified settings
     */
    @Query("SELECT s FROM SystemSettings s WHERE s.value IS NOT NULL AND s.value != s.defaultValue")
    List<SystemSettings> findAllModifiedSettings();

    /**
     * Search settings by key or description containing text
     *
     * @param searchTerm Search term to find in key or description
     * @return List of matching settings
     */
    @Query("SELECT s FROM SystemSettings s WHERE " +
           "LOWER(s.key) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<SystemSettings> searchSettings(@Param("searchTerm") String searchTerm);

    /**
     * Find all settings ordered by category and key
     *
     * @return All settings ordered by category and key
     */
    List<SystemSettings> findAllByOrderByCategoryAscKeyAsc();
}
