package com.princely.shopmanager.investment.repository;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.investment.domain.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface InvestmentRepository extends JpaRepository<Investment, String> {

    List<Investment> findByShop(Shop shop);

    List<Investment> findByInvestor(User investor);

    List<Investment> findByShopAndStatus(Shop shop, Investment.InvestmentStatus status);

    List<Investment> findByInvestorAndStatus(User investor, Investment.InvestmentStatus status);

    Optional<Investment> findByInvestmentNumber(String investmentNumber);

    @Query("SELECT i FROM Investment i WHERE i.status = 'ACTIVE'")
    List<Investment> findActiveInvestments();

    @Query("SELECT i FROM Investment i WHERE i.shop.id = :shopId AND i.status = 'ACTIVE'")
    List<Investment> findActiveInvestmentsByShop(@Param("shopId") String shopId);

    @Query("SELECT i FROM Investment i WHERE i.investor.id = :investorId AND i.status = 'ACTIVE'")
    List<Investment> findActiveInvestmentsByInvestor(@Param("investorId") String investorId);

    @Query("SELECT i FROM Investment i JOIN i.products p WHERE p = :product AND i.status = 'ACTIVE'")
    List<Investment> findActiveInvestmentsByProduct(@Param("product") Product product);

    @Query("SELECT SUM(i.amount) FROM Investment i WHERE i.shop.id = :shopId AND i.status = 'ACTIVE'")
    Optional<java.math.BigDecimal> getTotalActiveInvestmentAmount(@Param("shopId") String shopId);

    @Query("SELECT COUNT(i) FROM Investment i WHERE i.investor.id = :investorId")
    long countByInvestor(@Param("investorId") String investorId);

    @Query("SELECT i FROM Investment i WHERE i.maturityDate IS NOT NULL AND i.maturityDate <= CURRENT_TIMESTAMP " +
           "AND i.status = 'ACTIVE'")
    List<Investment> findMaturedInvestments();

    boolean existsByInvestmentNumber(String investmentNumber);

    @Query("SELECT i FROM Investment i WHERE i.shop.id = :shopId")
    Page<Investment> findByShopId(@Param("shopId") String shopId, Pageable pageable);

    @Query("SELECT i FROM Investment i WHERE i.investor.id = :investorId")
    Page<Investment> findByInvestorId(@Param("investorId") String investorId, Pageable pageable);

    /**
     * Calculate total investment amount in a specific round.
     * Used for PROPORTIONAL_BY_AMOUNT profit calculations.
     *
     * @param roundId Investment round ID
     * @return Total amount invested in the round
     */
    @Query("SELECT SUM(i.amount) FROM Investment i WHERE i.investmentRound.id = :roundId")
    java.math.BigDecimal sumAmountByInvestmentRoundId(@Param("roundId") String roundId);

    /**
     * Calculate total fixed shares in a specific round.
     * Used for FIXED_SHARES profit calculations.
     *
     * @param roundId Investment round ID
     * @return Total shares allocated in the round
     */
    @Query("SELECT SUM(i.fixedShares) FROM Investment i WHERE i.investmentRound.id = :roundId")
    Integer sumFixedSharesByInvestmentRoundId(@Param("roundId") String roundId);
}