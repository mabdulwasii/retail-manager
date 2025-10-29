package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.shop.id = :shopId")
    List<Category> findByShopId(@Param("shopId") String shopId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name AND c.shop.id = :shopId")
    boolean existsByNameAndShopId(@Param("name") String name, @Param("shopId") String shopId);
}
