package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Investment Domain Edge Cases Tests")
class InvestmentEdgeCasesTest {

    private Investment investment;
    private Shop shop;
    private User investor;

    @BeforeEach
    void setUp() {
        shop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        investor = User.builder()
            .id("investor-1")
            .username("investor")
            .email("investor@test.com")
            .build();

        InvestmentRound round = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TEST-2025-Q1-001")
            .shop(shop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .build();

        investment = Investment.builder()
            .id("investment-1")
            .investmentNumber("INV-001")
            .shop(shop)
            .investor(investor)
            .amount(new BigDecimal("10000.00"))
            .investmentRound(round)
            .investmentDate(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should calculate available balance with zero profit and withdrawals")
    void shouldCalculateAvailableBalanceWithZeroProfitAndWithdrawals() {
        // When
        BigDecimal availableBalance = investment.getAvailableBalance();

        // Then
        assertEquals(0, new BigDecimal("10000.00").compareTo(availableBalance));
    }

    @Test
    @DisplayName("Should calculate available balance with profits")
    void shouldCalculateAvailableBalanceWithProfits() {
        // Given
        investment.setTotalProfitEarned(new BigDecimal("1000.00"));

        // When
        BigDecimal availableBalance = investment.getAvailableBalance();

        // Then
        assertEquals(0, new BigDecimal("11000.00").compareTo(availableBalance));
    }

    @Test
    @DisplayName("Should allow withdrawal when sufficient balance")
    void shouldAllowWithdrawalWhenSufficientBalance() {
        // Given
        investment.setTotalProfitEarned(new BigDecimal("1000.00"));
        BigDecimal withdrawAmount = new BigDecimal("500.00");

        // When
        boolean canWithdraw = investment.canWithdraw(withdrawAmount);

        // Then
        assertTrue(canWithdraw);
    }

    @Test
    @DisplayName("Should deny withdrawal when insufficient balance")
    void shouldDenyWithdrawalWhenInsufficientBalance() {
        // Given
        investment.setTotalProfitEarned(new BigDecimal("100.00"));
        BigDecimal withdrawAmount = new BigDecimal("15000.00"); // More than amount + profit

        // When
        boolean canWithdraw = investment.canWithdraw(withdrawAmount);

        // Then
        assertFalse(canWithdraw);
    }

    @Test
    @DisplayName("Should have default values from builder")
    void shouldHaveDefaultValuesFromBuilder() {
        // When
        Investment newInvestment = Investment.builder()
            .id("test-id")
            .amount(new BigDecimal("5000.00"))
            .build();

        // Then - products removed from Investment entity
        assertEquals(Investment.InvestmentStatus.ACTIVE, newInvestment.getStatus());
        assertEquals(BigDecimal.ZERO, newInvestment.getTotalProfitEarned());
        assertEquals(BigDecimal.ZERO, newInvestment.getTotalWithdrawn());
        assertNotNull(newInvestment.getShares());
    }
}