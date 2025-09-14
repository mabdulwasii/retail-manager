package com.princely.shopmanager.investment.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.investment.domain.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}