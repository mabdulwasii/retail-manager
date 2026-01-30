package com.princely.shopmanager.inventory.repository;

import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.domain.InventoryHistory;
import com.princely.shopmanager.shared.repository.base.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String>, JpaSpecificationExecutor<Inventory> {

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId")
    List<Inventory> findByShopId(@Param("shopId") String shopId);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.product.id = :productId")
    List<Inventory> findByShopIdAndProductId(@Param("shopId") String shopId, @Param("productId") String productId);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.product.id = :productId AND i.batchNumber = :batchNumber")
    Optional<Inventory> findByShopIdAndProductIdAndBatchNumber(
        @Param("shopId") String shopId,
        @Param("productId") String productId,
        @Param("batchNumber") String batchNumber
    );

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND (i.purchaseQuantity - i.reservedStock) <= i.minimumStock")
    List<Inventory> findLowStockItems(@Param("shopId") String shopId);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND (i.purchaseQuantity - i.reservedStock) <= 0")
    List<Inventory> findOutOfStockItems(@Param("shopId") String shopId);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.expiryDate BETWEEN :startDate AND :endDate")
    List<Inventory> findExpiringItems(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.expiryDate < :currentDate")
    List<Inventory> findExpiredItems(@Param("shopId") String shopId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.location = :location")
    List<Inventory> findByLocation(@Param("shopId") String shopId, @Param("location") String location);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.status = :status")
    List<Inventory> findByStatus(@Param("shopId") String shopId, @Param("status") Inventory.InventoryStatus status);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.product.category.id = :categoryId")
    List<Inventory> findByCategory(@Param("shopId") String shopId, @Param("categoryId") String categoryId);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.product.name ILIKE %:productName%")
    List<Inventory> findByProductNameContaining(@Param("shopId") String shopId, @Param("productName") String productName);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND i.sellingPrice BETWEEN :minPrice AND :maxPrice")
    List<Inventory> findByPriceRange(
        @Param("shopId") String shopId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );

    @Query("SELECT SUM(COALESCE(i.totalPurchaseCost, i.purchaseQuantity * i.costPrice)) FROM Inventory i WHERE i.shop.id = :shopId AND i.status = 'ACTIVE'")
    BigDecimal calculateTotalInventoryValue(@Param("shopId") String shopId);

    @Query("SELECT COUNT(DISTINCT i.product.id) FROM Inventory i WHERE i.shop.id = :shopId AND i.status = 'ACTIVE'")
    Long countActiveProducts(@Param("shopId") String shopId);

    @Query("SELECT i FROM Inventory i WHERE i.shop.id = :shopId AND (i.purchaseQuantity - i.reservedStock) >= :quantity AND i.status = 'ACTIVE' AND (i.expiryDate IS NULL OR i.expiryDate > CURRENT_DATE)")
    List<Inventory> findAvailableForSale(@Param("shopId") String shopId, @Param("quantity") Integer quantity);

    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    List<Inventory> findByProductId(@Param("productId") String productId);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.product.id = :productId AND i.createdAt >= :startOfDay")
    long countByProductIdAndCreatedAtAfter(@Param("productId") String productId, @Param("startOfDay") LocalDateTime startOfDay);
}