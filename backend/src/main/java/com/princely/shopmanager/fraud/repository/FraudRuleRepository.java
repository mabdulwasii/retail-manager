package com.princely.shopmanager.fraud.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.fraud.domain.FraudRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, String> {

    List<FraudRule> findByShopAndEnabled(Shop shop, boolean enabled);

    @Query("SELECT fr FROM FraudRule fr WHERE fr.shop IS NULL AND fr.enabled = :enabled")
    List<FraudRule> findGlobalEnabledRules(@Param("enabled") boolean enabled);

    @Query("SELECT fr FROM FraudRule fr WHERE fr.shop IS NULL AND fr.enabled = true")
    List<FraudRule> findGlobalEnabledRules();

    List<FraudRule> findByRuleTypeAndEnabled(FraudRule.FraudRuleType ruleType, boolean enabled);

    @Query("SELECT fr FROM FraudRule fr WHERE (fr.shop IS NULL OR fr.shop = :shop) " +
           "AND fr.ruleType = :ruleType AND fr.enabled = true")
    List<FraudRule> findApplicableRules(@Param("shop") Shop shop, @Param("ruleType") FraudRule.FraudRuleType ruleType);

    @Query("SELECT fr FROM FraudRule fr WHERE fr.shop = :shop OR fr.shop IS NULL " +
           "ORDER BY fr.shop NULLS FIRST, fr.ruleName")
    List<FraudRule> findAllApplicableRules(@Param("shop") Shop shop);

    long countByShop(Shop shop);

    long countByShopIsNull(); // Global rules count

    boolean existsByRuleNameAndShop(String ruleName, Shop shop);

    @Query("SELECT fr FROM FraudRule fr WHERE fr.autoBlock = true AND fr.enabled = true")
    List<FraudRule> findAutoBlockRules();

    // Additional queries for the enhanced fraud module
    Page<FraudRule> findByEnabledOrderByRuleName(boolean enabled, Pageable pageable);

    @Query("SELECT fr FROM FraudRule fr WHERE (fr.shop IS NULL OR fr.shop = :shop) AND fr.enabled = true ORDER BY fr.severity DESC, fr.ruleName")
    List<FraudRule> findEnabledRulesByShopOrderedBySeverity(@Param("shop") Shop shop);

    Optional<FraudRule> findByRuleNameAndShop(String ruleName, Shop shop);

    @Query("SELECT fr FROM FraudRule fr WHERE fr.shop = :shop AND fr.createdAt >= :since")
    List<FraudRule> findByShopAndCreatedAtAfter(@Param("shop") Shop shop, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(fr) FROM FraudRule fr WHERE fr.enabled = true")
    long countEnabledRules();

    @Query("SELECT fr.ruleType, COUNT(fr) FROM FraudRule fr WHERE fr.enabled = true GROUP BY fr.ruleType")
    List<Object[]> countEnabledRulesByType();
}