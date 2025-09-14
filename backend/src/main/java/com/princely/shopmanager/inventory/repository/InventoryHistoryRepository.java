package com.princely.shopmanager.inventory.repository;

import com.princely.shopmanager.inventory.domain.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, String> {

    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.inventory.id = :inventoryId ORDER BY ih.createdAt DESC")
    List<InventoryHistory> findByInventoryIdOrderByCreatedAtDesc(@Param("inventoryId") String inventoryId);

    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.inventory.shop.id = :shopId ORDER BY ih.createdAt DESC")
    List<InventoryHistory> findByShopIdOrderByCreatedAtDesc(@Param("shopId") String shopId);

    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.inventory.shop.id = :shopId AND ih.createdAt BETWEEN :startDate AND :endDate ORDER BY ih.createdAt DESC")
    List<InventoryHistory> findByShopIdAndDateRange(
        @Param("shopId") String shopId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.referenceId = :referenceId AND ih.referenceType = :referenceType")
    List<InventoryHistory> findByReferenceIdAndType(
        @Param("referenceId") String referenceId,
        @Param("referenceType") InventoryHistory.ReferenceType referenceType
    );

    @Query("SELECT ih FROM InventoryHistory ih WHERE ih.changeType = :changeType AND ih.inventory.shop.id = :shopId ORDER BY ih.createdAt DESC")
    List<InventoryHistory> findByChangeTypeAndShopId(
        @Param("changeType") InventoryHistory.ChangeType changeType,
        @Param("shopId") String shopId
    );
}