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
}