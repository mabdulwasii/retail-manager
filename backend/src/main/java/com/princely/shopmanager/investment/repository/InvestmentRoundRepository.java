package com.princely.shopmanager.investment.repository;

import com.princely.shopmanager.investment.domain.InvestmentRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for InvestmentRound entity operations.
 */
@Repository
public interface InvestmentRoundRepository extends JpaRepository<InvestmentRound, String> {

    /**
     * Find all investment rounds for a specific shop.
     *
     * @param shopId Shop ID
     * @param pageable Pagination parameters
     * @return Page of investment rounds
     */
    @Query("SELECT ir FROM InvestmentRound ir WHERE ir.shop.id = :shopId")
    Page<InvestmentRound> findByShopId(@Param("shopId") String shopId, Pageable pageable);

    /**
     * Find investment rounds by shop and status.
     *
     * @param shopId Shop ID
     * @param status Round status
     * @param pageable Pagination parameters
     * @return Page of investment rounds
     */
    @Query("SELECT ir FROM InvestmentRound ir WHERE ir.shop.id = :shopId AND ir.status = :status")
    Page<InvestmentRound> findByShopIdAndStatus(
        @Param("shopId") String shopId,
        @Param("status") InvestmentRound.RoundStatus status,
        Pageable pageable
    );

    /**
     * Find an investment round by round number and shop.
     *
     * @param roundNumber Round number
     * @param shopId Shop ID
     * @return Optional of investment round
     */
    @Query("SELECT ir FROM InvestmentRound ir WHERE ir.roundNumber = :roundNumber AND ir.shop.id = :shopId")
    Optional<InvestmentRound> findByRoundNumberAndShopId(
        @Param("roundNumber") String roundNumber,
        @Param("shopId") String shopId
    );

    /**
     * Check if a round number already exists for a shop.
     *
     * @param roundNumber Round number
     * @param shopId Shop ID
     * @return true if exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(ir) > 0 THEN true ELSE false END " +
           "FROM InvestmentRound ir WHERE ir.roundNumber = :roundNumber AND ir.shop.id = :shopId")
    boolean existsByRoundNumberAndShopId(
        @Param("roundNumber") String roundNumber,
        @Param("shopId") String shopId
    );

    /**
     * Find all open investment rounds for a shop.
     *
     * @param shopId Shop ID
     * @return List of open investment rounds
     */
    @Query("SELECT ir FROM InvestmentRound ir " +
           "WHERE ir.shop.id = :shopId AND ir.status = 'OPEN' " +
           "ORDER BY ir.createdAt DESC")
    List<InvestmentRound> findOpenRoundsByShopId(@Param("shopId") String shopId);

    /**
     * Count investment rounds by shop.
     *
     * @param shopId Shop ID
     * @return Count of rounds
     */
    @Query("SELECT COUNT(ir) FROM InvestmentRound ir WHERE ir.shop.id = :shopId")
    long countByShopId(@Param("shopId") String shopId);
}
