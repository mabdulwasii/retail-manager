package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ShopRepository extends JpaRepository<Shop, String> {

    List<Shop> findByTenant_Id(String tenantId);

    Optional<Shop> findByName(String name);

    List<Shop> findByStatus(Shop.ShopStatus status);

    @Query("SELECT s FROM Shop s WHERE s.email = :email")
    Optional<Shop> findByEmail(@Param("email") String email);

    @Query("SELECT s FROM Shop s WHERE s.city = :city AND s.status = :status")
    List<Shop> findByCityAndStatus(@Param("city") String city, @Param("status") Shop.ShopStatus status);

    boolean existsByTenant_Id(String tenantId);

    boolean existsByName(String name);

    // Paginated queries for ShopService
    Page<Shop> findByTenant_Id(String tenantId, Pageable pageable);

    List<Shop> findByTenant_IdAndStatus(String tenantId, Shop.ShopStatus status);

    // Optimized queries with fetch joins to avoid N+1 problems
    @Query("""
        SELECT s FROM Shop s
        LEFT JOIN FETCH s.products p
        LEFT JOIN FETCH s.tenant t
        WHERE s.id = :shopId
        """)
    Optional<Shop> findByIdWithDetails(@Param("shopId") String shopId);

    @Query("""
        SELECT s FROM Shop s
        LEFT JOIN FETCH s.tenant t
        WHERE s.tenant.id = :tenantId AND s.status = :status
        """)
    List<Shop> findByTenantIdAndStatusWithDetails(@Param("tenantId") String tenantId, @Param("status") Shop.ShopStatus status);

    @Query("""
        SELECT s FROM Shop s
        LEFT JOIN FETCH s.products p
        LEFT JOIN FETCH p.category c
        WHERE s.id = :shopId
        """)
    Optional<Shop> findByIdWithProductsAndCategories(@Param("shopId") String shopId);

    @Query("""
        SELECT s FROM Shop s
        LEFT JOIN FETCH s.tenant t
        WHERE s.status = 'ACTIVE'
        ORDER BY s.createdAt DESC
        """)
    List<Shop> findActiveShopsWithTenant();
}