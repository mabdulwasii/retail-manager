package com.princely.shopmanager.returns.repository;

import com.princely.shopmanager.returns.domain.ProductReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductReturnRepository extends JpaRepository<ProductReturn, String> {

    @Query("SELECT pr FROM ProductReturn pr WHERE pr.shop.id = :shopId")
    Page<ProductReturn> findByShopId(@Param("shopId") String shopId, Pageable pageable);

    @Query("SELECT pr FROM ProductReturn pr WHERE pr.status = :status AND pr.shop.id = :shopId")
    List<ProductReturn> findByStatusAndShopId(@Param("status") ProductReturn.ReturnStatus status, @Param("shopId") String shopId);

    @Query("SELECT pr FROM ProductReturn pr WHERE pr.fraudCheckStatus = :fraudStatus AND pr.shop.id = :shopId")
    List<ProductReturn> findByFraudCheckStatusAndShopId(@Param("fraudStatus") ProductReturn.FraudCheckStatus fraudStatus, @Param("shopId") String shopId);

    @Query("SELECT pr FROM ProductReturn pr WHERE pr.returnDate BETWEEN :startDate AND :endDate AND pr.shop.id = :shopId")
    List<ProductReturn> findByReturnDateBetweenAndShopId(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("shopId") String shopId
    );
}