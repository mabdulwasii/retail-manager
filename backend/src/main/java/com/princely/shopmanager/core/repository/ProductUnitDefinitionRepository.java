package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.ProductUnitDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing product unit definitions.
 * Handles CRUD operations for product unit structure (catalog level, no prices).
 */
@Repository
public interface ProductUnitDefinitionRepository extends JpaRepository<ProductUnitDefinition, String>, JpaSpecificationExecutor<ProductUnitDefinition> {

    /**
     * Find all unit definitions for a specific product.
     * @param productId the product ID
     * @return list of unit definitions ordered by sort_order
     */
    @Query("SELECT pud FROM ProductUnitDefinition pud WHERE pud.product.id = :productId ORDER BY pud.sortOrder ASC")
    List<ProductUnitDefinition> findByProductId(@Param("productId") String productId);

    /**
     * Find a specific unit definition by product ID and unit type.
     * @param productId the product ID
     * @param unitType the unit type (e.g., "piece", "pack", "carton")
     * @return optional unit definition
     */
    @Query("SELECT pud FROM ProductUnitDefinition pud WHERE pud.product.id = :productId AND pud.unitType = :unitType")
    Optional<ProductUnitDefinition> findByProductIdAndUnitType(@Param("productId") String productId, @Param("unitType") String unitType);

    /**
     * Find the base unit definition for a product.
     * @param productId the product ID
     * @return optional base unit definition
     */
    @Query("SELECT pud FROM ProductUnitDefinition pud WHERE pud.product.id = :productId AND pud.isBaseUnit = true")
    Optional<ProductUnitDefinition> findBaseUnitByProductId(@Param("productId") String productId);

    /**
     * Check if a unit type exists for a product.
     * @param productId the product ID
     * @param unitType the unit type
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(pud) > 0 THEN true ELSE false END FROM ProductUnitDefinition pud WHERE pud.product.id = :productId AND pud.unitType = :unitType")
    boolean existsByProductIdAndUnitType(@Param("productId") String productId, @Param("unitType") String unitType);

    /**
     * Delete a specific unit definition by product ID and unit type.
     * @param productId the product ID
     * @param unitType the unit type
     */
    @Query("DELETE FROM ProductUnitDefinition pud WHERE pud.product.id = :productId AND pud.unitType = :unitType")
    void deleteByProductIdAndUnitType(@Param("productId") String productId, @Param("unitType") String unitType);

    /**
     * Count unit definitions for a product.
     * @param productId the product ID
     * @return count of unit definitions
     */
    @Query("SELECT COUNT(pud) FROM ProductUnitDefinition pud WHERE pud.product.id = :productId")
    Long countByProductId(@Param("productId") String productId);
}
