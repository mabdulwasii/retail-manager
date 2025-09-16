package com.princely.shopmanager.expenses.repository;

import com.princely.shopmanager.expenses.domain.ExpenseCategory;
import com.princely.shopmanager.shared.repository.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, UUID> {

    /**
     * Find category by ID and shop ID for tenant isolation
     */
    Optional<ExpenseCategory> findByIdAndShopId(UUID id, UUID shopId);

    /**
     * Find all active categories for a shop
     */
    List<ExpenseCategory> findByShopIdAndIsActiveTrueOrderByName(UUID shopId);

    /**
     * Find all categories for a shop (including inactive)
     */
    List<ExpenseCategory> findByShopIdOrderByName(UUID shopId);

    /**
     * Find category by name and shop ID
     */
    Optional<ExpenseCategory> findByShopIdAndNameIgnoreCase(UUID shopId, String name);

    /**
     * Check if category name exists for a shop (excluding specific category ID)
     */
    @Query("SELECT COUNT(c) > 0 FROM ExpenseCategory c WHERE c.shopId = :shopId " +
           "AND LOWER(c.name) = LOWER(:name) AND c.id != :excludeId")
    boolean existsByShopIdAndNameIgnoreCaseAndIdNot(@Param("shopId") UUID shopId,
                                                    @Param("name") String name,
                                                    @Param("excludeId") UUID excludeId);

    /**
     * Check if category name exists for a shop
     */
    boolean existsByShopIdAndNameIgnoreCase(UUID shopId, String name);

    /**
     * Find categories that require approval
     */
    List<ExpenseCategory> findByShopIdAndRequiresApprovalTrueAndIsActiveTrueOrderByName(UUID shopId);

    /**
     * Find categories that allow auto-approval
     */
    List<ExpenseCategory> findByShopIdAndAutoApprovalEnabledTrueAndIsActiveTrueOrderByName(UUID shopId);

    /**
     * Count active categories for a shop
     */
    long countByShopIdAndIsActiveTrue(UUID shopId);

    /**
     * Find categories with approval limit less than or equal to specified amount
     */
    @Query("SELECT c FROM ExpenseCategory c WHERE c.shopId = :shopId " +
           "AND c.isActive = true " +
           "AND c.approvalLimit IS NOT NULL " +
           "AND c.approvalLimit <= :amount " +
           "ORDER BY c.name")
    List<ExpenseCategory> findCategoriesWithLimitLessThanOrEqual(@Param("shopId") UUID shopId,
                                                               @Param("amount") java.math.BigDecimal amount);
}