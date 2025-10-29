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

    @Query("SELECT c FROM Category i WHERE c.shop.id = :shopId")
    List<Category> findByShopId(@Param("shopId") String shopId);

    boolean existsByNameAndShopId(String name, String shopId);
}
