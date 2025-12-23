package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvestmentRound Domain Tests")
class InvestmentRoundTest {

    private InvestmentRound round;
    private Shop testShop;
    private User testInvestor;

    @BeforeEach
    void setUp() {
        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        testInvestor = User.builder()
            .id("user-1")
            .username("investor1")
            .email("investor1@example.com")
            .build();

        round = InvestmentRound.builder()
            .roundNumber("ROUND-2024-Q1")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDateTime.now().plusYears(1))
            .build();
    }

    // Default values tests
    @Test
    @DisplayName("Should have default status as OPEN")
    void shouldHaveDefaultStatusAsOpen() {
        // Given
        InvestmentRound newRound = InvestmentRound.builder()
            .roundNumber("ROUND-2024-Q2")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .build();

        // Then
        assertThat(newRound.getStatus()).isEqualTo(InvestmentRound.RoundStatus.OPEN);
    }

    @Test
    @DisplayName("Should initialize empty investments set")
    void shouldInitializeEmptyInvestmentsSet() {
        // Given
        InvestmentRound newRound = InvestmentRound.builder()
            .roundNumber("ROUND-2024-Q2")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .build();

        // Then
        assertThat(newRound.getInvestments()).isNotNull();
        assertThat(newRound.getInvestments()).isEmpty();
    }

    // addInvestment tests
    @Test
    @DisplayName("addInvestment - Should add investment to round")
    void addInvestment_shouldAddInvestmentToRound() {
        // Given
        Investment investment = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        // When
        round.addInvestment(investment);

        // Then
        assertThat(round.getInvestments()).hasSize(1);
        assertThat(round.getInvestments()).contains(investment);
    }

    @Test
    @DisplayName("addInvestment - Should set bidirectional relationship")
    void addInvestment_shouldSetBidirectionalRelationship() {
        // Given
        Investment investment = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        // When
        round.addInvestment(investment);

        // Then
        assertThat(investment.getInvestmentRound()).isEqualTo(round);
    }

    @Test
    @DisplayName("addInvestment - Should add multiple investments")
    void addInvestment_shouldAddMultipleInvestments() {
        // Given
        Investment investment1 = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        Investment investment2 = Investment.builder()
            .investmentNumber("INV-002")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(75000))
            .build();

        // When
        round.addInvestment(investment1);
        round.addInvestment(investment2);

        // Then
        assertThat(round.getInvestments()).hasSize(2);
        assertThat(round.getInvestments()).containsExactlyInAnyOrder(investment1, investment2);
    }

    @Test
    @DisplayName("addInvestment - Should maintain set uniqueness")
    void addInvestment_shouldMaintainSetUniqueness() {
        // Given
        Investment investment = Investment.builder()
            .id("inv-1")
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        // When
        round.addInvestment(investment);
        round.addInvestment(investment); // Add same investment again

        // Then - Set should only contain one instance
        assertThat(round.getInvestments()).hasSize(1);
    }

    // removeInvestment tests
    @Test
    @DisplayName("removeInvestment - Should remove investment from round")
    void removeInvestment_shouldRemoveInvestmentFromRound() {
        // Given
        Investment investment = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();
        round.addInvestment(investment);

        // When
        round.removeInvestment(investment);

        // Then
        assertThat(round.getInvestments()).isEmpty();
    }

    @Test
    @DisplayName("removeInvestment - Should clear bidirectional relationship")
    void removeInvestment_shouldClearBidirectionalRelationship() {
        // Given
        Investment investment = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();
        round.addInvestment(investment);

        // When
        round.removeInvestment(investment);

        // Then
        assertThat(investment.getInvestmentRound()).isNull();
    }

    @Test
    @DisplayName("removeInvestment - Should only remove specified investment")
    void removeInvestment_shouldOnlyRemoveSpecifiedInvestment() {
        // Given
        Investment investment1 = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        Investment investment2 = Investment.builder()
            .investmentNumber("INV-002")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(75000))
            .build();

        round.addInvestment(investment1);
        round.addInvestment(investment2);

        // When
        round.removeInvestment(investment1);

        // Then
        assertThat(round.getInvestments()).hasSize(1);
        assertThat(round.getInvestments()).contains(investment2);
        assertThat(round.getInvestments()).doesNotContain(investment1);
    }

    @Test
    @DisplayName("removeInvestment - Should handle removing non-existent investment gracefully")
    void removeInvestment_shouldHandleNonExistentInvestmentGracefully() {
        // Given
        Investment investment = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        // When - Remove investment that was never added
        round.removeInvestment(investment);

        // Then - Should not throw exception
        assertThat(round.getInvestments()).isEmpty();
    }

    // canAcceptInvestors tests
    @Test
    @DisplayName("canAcceptInvestors - Should return true when status is OPEN")
    void canAcceptInvestors_shouldReturnTrueWhenStatusIsOpen() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.OPEN);

        // When
        boolean canAccept = round.canAcceptInvestors();

        // Then
        assertThat(canAccept).isTrue();
    }

    @Test
    @DisplayName("canAcceptInvestors - Should return false when status is CLOSED")
    void canAcceptInvestors_shouldReturnFalseWhenStatusIsClosed() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.CLOSED);

        // When
        boolean canAccept = round.canAcceptInvestors();

        // Then
        assertThat(canAccept).isFalse();
    }

    @Test
    @DisplayName("canAcceptInvestors - Should return false when status is COMPLETED")
    void canAcceptInvestors_shouldReturnFalseWhenStatusIsCompleted() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.COMPLETED);

        // When
        boolean canAccept = round.canAcceptInvestors();

        // Then
        assertThat(canAccept).isFalse();
    }

    @Test
    @DisplayName("canAcceptInvestors - Should return false when status is CANCELLED")
    void canAcceptInvestors_shouldReturnFalseWhenStatusIsCancelled() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.CANCELLED);

        // When
        boolean canAccept = round.canAcceptInvestors();

        // Then
        assertThat(canAccept).isFalse();
    }

    // close tests
    @Test
    @DisplayName("close - Should change status to CLOSED")
    void close_shouldChangeStatusToClosed() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.OPEN);

        // When
        round.close();

        // Then
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CLOSED);
    }

    @Test
    @DisplayName("close - Should make canAcceptInvestors return false")
    void close_shouldMakeCanAcceptInvestorsReturnFalse() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.OPEN);
        assertThat(round.canAcceptInvestors()).isTrue();

        // When
        round.close();

        // Then
        assertThat(round.canAcceptInvestors()).isFalse();
    }

    @Test
    @DisplayName("close - Should work from any status")
    void close_shouldWorkFromAnyStatus() {
        // From OPEN
        round.setStatus(InvestmentRound.RoundStatus.OPEN);
        round.close();
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CLOSED);

        // From COMPLETED
        round.setStatus(InvestmentRound.RoundStatus.COMPLETED);
        round.close();
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CLOSED);
    }

    // complete tests
    @Test
    @DisplayName("complete - Should change status to COMPLETED")
    void complete_shouldChangeStatusToCompleted() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.CLOSED);

        // When
        round.complete();

        // Then
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.COMPLETED);
    }

    @Test
    @DisplayName("complete - Should make canAcceptInvestors return false")
    void complete_shouldMakeCanAcceptInvestorsReturnFalse() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.CLOSED);

        // When
        round.complete();

        // Then
        assertThat(round.canAcceptInvestors()).isFalse();
    }

    // cancel tests
    @Test
    @DisplayName("cancel - Should change status to CANCELLED")
    void cancel_shouldChangeStatusToCancelled() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.OPEN);

        // When
        round.cancel();

        // Then
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel - Should make canAcceptInvestors return false")
    void cancel_shouldMakeCanAcceptInvestorsReturnFalse() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.OPEN);

        // When
        round.cancel();

        // Then
        assertThat(round.canAcceptInvestors()).isFalse();
    }

    // RoundStatus enum tests
    @Test
    @DisplayName("RoundStatus - All enum values should exist")
    void roundStatus_allEnumValuesShouldExist() {
        InvestmentRound.RoundStatus[] statuses = InvestmentRound.RoundStatus.values();

        assertThat(statuses).hasSize(4);
        assertThat(statuses).contains(
            InvestmentRound.RoundStatus.OPEN,
            InvestmentRound.RoundStatus.CLOSED,
            InvestmentRound.RoundStatus.COMPLETED,
            InvestmentRound.RoundStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("RoundStatus - Should have correct display names")
    void roundStatus_shouldHaveCorrectDisplayNames() {
        assertThat(InvestmentRound.RoundStatus.OPEN.getDisplayName()).isEqualTo("Open");
        assertThat(InvestmentRound.RoundStatus.CLOSED.getDisplayName()).isEqualTo("Closed");
        assertThat(InvestmentRound.RoundStatus.COMPLETED.getDisplayName()).isEqualTo("Completed");
        assertThat(InvestmentRound.RoundStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
    }

    // Embedded configuration tests
    @Test
    @DisplayName("Should store tier configuration for TIERED model")
    void shouldStoreTierConfigurationForTieredModel() {
        // Given
        TierConfiguration tierConfig = TierConfiguration.builder()
            .tier1Threshold(BigDecimal.ZERO)
            .tier1Multiplier(BigDecimal.valueOf(1.0))
            .tier2Threshold(BigDecimal.valueOf(50000))
            .tier2Multiplier(BigDecimal.valueOf(1.1))
            .tier3Threshold(BigDecimal.valueOf(100000))
            .tier3Multiplier(BigDecimal.valueOf(1.2))
            .build();

        round.setProfitSharingModel(Investment.ProfitSharingModel.TIERED);
        round.setTierConfiguration(tierConfig);

        // Then
        assertThat(round.getTierConfiguration()).isNotNull();
        assertThat(round.getTierConfiguration().getTier1Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(round.getTierConfiguration().getTier2Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.1));
        assertThat(round.getTierConfiguration().getTier3Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.2));
    }

    @Test
    @DisplayName("Should store time weighting rules for TIME_WEIGHTED model")
    void shouldStoreTimeWeightingRulesForTimeWeightedModel() {
        // Given
        TimeWeightingRules timeRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.2))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        round.setProfitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED);
        round.setTimeWeightingRules(timeRules);

        // Then
        assertThat(round.getTimeWeightingRules()).isNotNull();
        assertThat(round.getTimeWeightingRules().getBaseMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(round.getTimeWeightingRules().getYear2Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.2));
        assertThat(round.getTimeWeightingRules().getYear3Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    // Field storage tests
    @Test
    @DisplayName("Should store all basic fields correctly")
    void shouldStoreAllBasicFieldsCorrectly() {
        // Given
        LocalDateTime maturity = LocalDateTime.of(2025, 12, 31, 0, 0);
        String notes = "Q1 2024 investment round for shop expansion";

        round.setRoundNumber("ROUND-2024-Q2");
        round.setInvestmentType(Investment.InvestmentType.PRODUCT_SPECIFIC);
        round.setProfitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES);
        round.setMaturityDate(maturity);
        round.setNotes(notes);

        // Then
        assertThat(round.getRoundNumber()).isEqualTo("ROUND-2024-Q2");
        assertThat(round.getInvestmentType()).isEqualTo(Investment.InvestmentType.PRODUCT_SPECIFIC);
        assertThat(round.getProfitSharingModel()).isEqualTo(Investment.ProfitSharingModel.FIXED_SHARES);
        assertThat(round.getMaturityDate()).isEqualTo(maturity);
        assertThat(round.getNotes()).isEqualTo(notes);
    }

    // Workflow scenario tests
    @Test
    @DisplayName("Should support complete round lifecycle")
    void shouldSupportCompleteRoundLifecycle() {
        // Given - Round opens
        round.setStatus(InvestmentRound.RoundStatus.OPEN);
        assertThat(round.canAcceptInvestors()).isTrue();

        // When - Add investors
        Investment inv1 = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();

        round.addInvestment(inv1);
        assertThat(round.getInvestments()).hasSize(1);

        // When - Close round to new investors
        round.close();
        assertThat(round.canAcceptInvestors()).isFalse();
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CLOSED);

        // When - Complete round at maturity
        round.complete();
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.COMPLETED);
        assertThat(round.canAcceptInvestors()).isFalse();
    }

    @Test
    @DisplayName("Should support cancellation workflow")
    void shouldSupportCancellationWorkflow() {
        // Given
        round.setStatus(InvestmentRound.RoundStatus.OPEN);
        Investment inv1 = Investment.builder()
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(50000))
            .build();
        round.addInvestment(inv1);

        // When - Cancel round
        round.cancel();

        // Then
        assertThat(round.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CANCELLED);
        assertThat(round.canAcceptInvestors()).isFalse();
        assertThat(round.getInvestments()).hasSize(1); // Investments remain but round is cancelled
    }

    // Constructor and builder tests
    @Test
    @DisplayName("Should create with no-args constructor")
    void shouldCreateWithNoArgsConstructor() {
        // When
        InvestmentRound newRound = new InvestmentRound();

        // Then
        assertThat(newRound).isNotNull();
    }

    @Test
    @DisplayName("Should create with builder")
    void shouldCreateWithBuilder() {
        // When
        InvestmentRound newRound = InvestmentRound.builder()
            .roundNumber("ROUND-2024-Q3")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.CATEGORY_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .maturityDate(LocalDateTime.now().plusYears(2))
            .status(InvestmentRound.RoundStatus.OPEN)
            .notes("Test round")
            .build();

        // Then
        assertThat(newRound.getRoundNumber()).isEqualTo("ROUND-2024-Q3");
        assertThat(newRound.getInvestmentType()).isEqualTo(Investment.InvestmentType.CATEGORY_SPECIFIC);
        assertThat(newRound.getProfitSharingModel()).isEqualTo(Investment.ProfitSharingModel.TIME_WEIGHTED);
        assertThat(newRound.getStatus()).isEqualTo(InvestmentRound.RoundStatus.OPEN);
    }
}
