package com.princely.shopmanager.expenses.repository;

import com.princely.shopmanager.expenses.domain.Expense;
import com.princely.shopmanager.expenses.domain.ExpenseStatus;
import com.princely.shopmanager.shared.repository.base.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID>,
                                          JpaSpecificationExecutor<Expense> {

    /**
     * Find expense by ID and shop ID for tenant isolation
     */
    Optional<Expense> findByIdAndShopId(UUID id, String shopId);

    /**
     * Find all expenses for a specific shop
     */
    Page<Expense> findByShopIdOrderByExpenseDateDesc(String shopId, Pageable pageable);

    /**
     * Find expenses by shop ID and status
     */
    List<Expense> findByShopIdAndStatus(String shopId, ExpenseStatus status);

    /**
     * Find expenses by shop ID and category
     */
    List<Expense> findByShopIdAndCategoryId(String shopId, UUID categoryId);

    /**
     * Find expenses by shop ID within date range
     */
    @Query("SELECT e FROM Expense e WHERE e.shopId = :shopId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> findByShopIdAndDateRange(@Param("shopId") String shopId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * Find pending expenses that require approval
     */
    @Query("SELECT e FROM Expense e WHERE e.shopId = :shopId " +
           "AND e.status = 'PENDING_APPROVAL' " +
           "ORDER BY e.createdAt ASC")
    List<Expense> findPendingApprovalsByShopId(@Param("shopId") String shopId);

    /**
     * Find expenses by created user
     */
    List<Expense> findByShopIdAndExpenseCreatedByOrderByExpenseDateDesc(String shopId, UUID expenseCreatedBy);

    /**
     * Calculate total expenses for a shop within date range
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.shopId = :shopId " +
           "AND e.status IN ('APPROVED', 'PAID') " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalExpensesByShopAndDateRange(@Param("shopId") String shopId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Calculate total expenses by category for a shop
     */
    @Query("SELECT e.categoryId, COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.shopId = :shopId " +
           "AND e.status IN ('APPROVED', 'PAID') " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.categoryId")
    List<Object[]> calculateExpensesByCategoryAndDateRange(@Param("shopId") String shopId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * Count expenses by status for a shop
     */
    @Query("SELECT e.status, COUNT(e) FROM Expense e " +
           "WHERE e.shopId = :shopId " +
           "GROUP BY e.status")
    List<Object[]> countExpensesByStatus(@Param("shopId") String shopId);

    /**
     * Find expenses with amount greater than specified limit
     */
    @Query("SELECT e FROM Expense e WHERE e.shopId = :shopId " +
           "AND e.amount > :amount " +
           "ORDER BY e.amount DESC")
    List<Expense> findExpensesAboveAmount(@Param("shopId") String shopId,
                                        @Param("amount") BigDecimal amount);

    /**
     * Find recent expenses for a shop
     */
    @Query("SELECT e FROM Expense e WHERE e.shopId = :shopId " +
           "ORDER BY e.createdAt DESC")
    List<Expense> findRecentExpensesByShopId(@Param("shopId") String shopId, Pageable pageable);

    /**
     * Search expenses by title or description
     */
    @Query("SELECT e FROM Expense e WHERE e.shopId = :shopId " +
           "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(e.vendorName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> searchExpenses(@Param("shopId") String shopId, @Param("query") String query);

    /**
     * Find expenses by tags
     */
    @Query("SELECT DISTINCT e FROM Expense e JOIN e.tags t " +
           "WHERE e.shopId = :shopId AND t IN :tags " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> findByShopIdAndTagsIn(@Param("shopId") String shopId, @Param("tags") List<String> tags);

    /**
     * Calculate monthly expense totals for trend analysis
     */
    @Query("SELECT EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate), " +
           "COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.shopId = :shopId " +
           "AND e.status IN ('APPROVED', 'PAID') " +
           "AND e.expenseDate >= :fromDate " +
           "GROUP BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate) " +
           "ORDER BY EXTRACT(YEAR FROM e.expenseDate), EXTRACT(MONTH FROM e.expenseDate)")
    List<Object[]> calculateMonthlyExpenseTrends(@Param("shopId") String shopId,
                                               @Param("fromDate") LocalDate fromDate);

    /**
     * Find top vendors by expense amount
     */
    @Query("SELECT e.vendorName, COALESCE(SUM(e.amount), 0), COUNT(e) FROM Expense e " +
           "WHERE e.shopId = :shopId " +
           "AND e.status IN ('APPROVED', 'PAID') " +
           "AND e.vendorName IS NOT NULL " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.vendorName " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> findTopVendorsByAmount(@Param("shopId") String shopId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         Pageable pageable);
}