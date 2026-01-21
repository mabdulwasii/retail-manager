package com.princely.shopmanager.inventory.repository;

import com.princely.shopmanager.inventory.domain.InventoryUnitPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing inventory unit prices.
 * Handles CRUD operations for batch-specific selling prices per unit type.
 */
@Repository
public interface InventoryUnitPriceRepository extends JpaRepository<InventoryUnitPrice, String>, JpaSpecificationExecutor<InventoryUnitPrice> {

    /**
     * Find all unit prices for a specific inventory batch.
     * @param inventoryId the inventory ID
     * @return list of unit prices
     */
    @Query("SELECT iup FROM InventoryUnitPrice iup WHERE iup.inventory.id = :inventoryId")
    List<InventoryUnitPrice> findByInventoryId(@Param("inventoryId") String inventoryId);

    /**
     * Find a specific unit price by inventory ID and unit type.
     * @param inventoryId the inventory ID
     * @param unitType the unit type (e.g., "piece", "pack", "carton")
     * @return optional unit price
     */
    @Query("SELECT iup FROM InventoryUnitPrice iup WHERE iup.inventory.id = :inventoryId AND iup.unitType = :unitType")
    Optional<InventoryUnitPrice> findByInventoryIdAndUnitType(@Param("inventoryId") String inventoryId, @Param("unitType") String unitType);

    /**
     * Check if a unit price exists for an inventory batch and unit type.
     * @param inventoryId the inventory ID
     * @param unitType the unit type
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(iup) > 0 THEN true ELSE false END FROM InventoryUnitPrice iup WHERE iup.inventory.id = :inventoryId AND iup.unitType = :unitType")
    boolean existsByInventoryIdAndUnitType(@Param("inventoryId") String inventoryId, @Param("unitType") String unitType);

    /**
     * Delete a specific unit price by inventory ID and unit type.
     * @param inventoryId the inventory ID
     * @param unitType the unit type
     */
    @Query("DELETE FROM InventoryUnitPrice iup WHERE iup.inventory.id = :inventoryId AND iup.unitType = :unitType")
    void deleteByInventoryIdAndUnitType(@Param("inventoryId") String inventoryId, @Param("unitType") String unitType);

    /**
     * Count unit prices for an inventory batch.
     * @param inventoryId the inventory ID
     * @return count of unit prices
     */
    @Query("SELECT COUNT(iup) FROM InventoryUnitPrice iup WHERE iup.inventory.id = :inventoryId")
    Long countByInventoryId(@Param("inventoryId") String inventoryId);

    /**
     * Delete all unit prices for a specific inventory batch.
     * This is typically called when cascading is not handling the deletion.
     * @param inventoryId the inventory ID
     */
    @Query("DELETE FROM InventoryUnitPrice iup WHERE iup.inventory.id = :inventoryId")
    void deleteAllByInventoryId(@Param("inventoryId") String inventoryId);
}
