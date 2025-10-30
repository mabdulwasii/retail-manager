package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByBarcode(String barcode);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId")
    List<Product> findByShopId(@Param("shopId") String shopId);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.status = :status")
    List<Product> findByShopIdAndStatus(@Param("shopId") String shopId, @Param("status") Product.ProductStatus status);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.category.id = :categoryId")
    List<Product> findByShopIdAndCategoryId(@Param("shopId") String shopId, @Param("categoryId") String categoryId);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.name ILIKE %:name%")
    List<Product> findByShopIdAndNameContaining(@Param("shopId") String shopId, @Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByShopIdAndPriceBetween(
        @Param("shopId") String shopId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice
    );

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.location = :location")
    List<Product> findByShopIdAndLocation(@Param("shopId") String shopId, @Param("location") String location);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'ACTIVE'")
    Long countActiveProductsByShopId(@Param("shopId") String shopId);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.supplierName = :supplierName")
    List<Product> findByShopIdAndSupplierName(@Param("shopId") String shopId, @Param("supplierName") String supplierName);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku = :sku AND p.shop.id = :shopId")
    boolean existsBySkuAndShopId(@Param("sku") String sku, @Param("shopId") String shopId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.barcode = :barcode AND p.shop.id = :shopId")
    boolean existsByBarcodeAndShopId(@Param("barcode") String barcode, @Param("shopId") String shopId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countByCategory_Id(@Param("categoryId") String categoryId);
}