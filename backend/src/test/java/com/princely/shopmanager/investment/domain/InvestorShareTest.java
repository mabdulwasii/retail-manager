package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvestorShare Domain Tests")
class InvestorShareTest {

    private InvestorShare share;
    private Investment testInvestment;
    private SalesTransaction testTransaction;

    @BeforeEach
    void setUp() {
        Shop testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        User testInvestor = User.builder()
            .id("user-1")
            .username("investor1")
            .email("investor1@example.com")
            .build();

        testInvestment = Investment.builder()
            .id("investment-1")
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .amount(BigDecimal.valueOf(100000))
            .build();

        testTransaction = SalesTransaction.builder()
            .id("txn-1")
            .transactionNumber("TXN-001")
            .shop(testShop)
            .subtotal(BigDecimal.valueOf(1000))
            .totalAmount(BigDecimal.valueOf(1000))
            .build();

        share = InvestorShare.builder()
            .investment(testInvestment)
            .transaction(testTransaction)
            .transactionAmount(BigDecimal.valueOf(1000))
            .profitAmount(BigDecimal.valueOf(200))
            .sharePercentage(BigDecimal.valueOf(25))
            .shareAmount(BigDecimal.valueOf(50))
            .calculationDate(LocalDateTime.now())
            .build();
    }

    // Default values tests
    @Test
    @DisplayName("Should have default status as PENDING")
    void shouldHaveDefaultStatusAsPending() {
        // Given
        InvestorShare newShare = InvestorShare.builder()
            .investment(testInvestment)
            .transaction(testTransaction)
            .transactionAmount(BigDecimal.valueOf(500))
            .profitAmount(BigDecimal.valueOf(100))
            .shareAmount(BigDecimal.valueOf(25))
            .sharePercentage(BigDecimal.valueOf(25))
            .calculationDate(LocalDateTime.now())
            .build();

        // Then
        assertThat(newShare.getStatus()).isEqualTo(InvestorShare.ShareStatus.PENDING);
    }

    // ShareStatus enum tests
    @Test
    @DisplayName("ShareStatus - All enum values should exist")
    void shareStatus_allEnumValuesShouldExist() {
        InvestorShare.ShareStatus[] statuses = InvestorShare.ShareStatus.values();

        assertThat(statuses)
            .hasSize(5)
            .contains(
                InvestorShare.ShareStatus.PENDING,
                InvestorShare.ShareStatus.CALCULATED,
                InvestorShare.ShareStatus.DISTRIBUTED,
                InvestorShare.ShareStatus.REINVESTED,
                InvestorShare.ShareStatus.CANCELLED
            );
    }

    @Test
    @DisplayName("ShareStatus - Should have correct display names")
    void shareStatus_shouldHaveCorrectDisplayNames() {
        assertThat(InvestorShare.ShareStatus.PENDING.getDisplayName()).isEqualTo("Pending");
        assertThat(InvestorShare.ShareStatus.CALCULATED.getDisplayName()).isEqualTo("Calculated");
        assertThat(InvestorShare.ShareStatus.DISTRIBUTED.getDisplayName()).isEqualTo("Distributed");
        assertThat(InvestorShare.ShareStatus.REINVESTED.getDisplayName()).isEqualTo("Reinvested");
        assertThat(InvestorShare.ShareStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
    }

    // Field storage tests
    @Test
    @DisplayName("Should store all financial fields correctly")
    void shouldStoreAllFinancialFieldsCorrectly() {
        // Given
        BigDecimal txnAmount = BigDecimal.valueOf(5000.50);
        BigDecimal profit = BigDecimal.valueOf(1000.25);
        BigDecimal shareAmt = BigDecimal.valueOf(250.06);
        BigDecimal percentage = BigDecimal.valueOf(25.50);

        this.share.setTransactionAmount(txnAmount);
        this.share.setProfitAmount(profit);
        this.share.setShareAmount(shareAmt);
        this.share.setSharePercentage(percentage);

        // Then
        assertThat(this.share.getTransactionAmount()).isEqualByComparingTo(txnAmount);
        assertThat(this.share.getProfitAmount()).isEqualByComparingTo(profit);
        assertThat(this.share.getShareAmount()).isEqualByComparingTo(shareAmt);
        assertThat(this.share.getSharePercentage()).isEqualByComparingTo(percentage);
    }

    @Test
    @DisplayName("Should store calculation date correctly")
    void shouldStoreCalculationDateCorrectly() {
        // Given
        LocalDateTime calcDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        share.setCalculationDate(calcDate);

        // Then
        assertThat(share.getCalculationDate()).isEqualTo(calcDate);
    }

    @Test
    @DisplayName("Should store distribution date when distributed")
    void shouldStoreDistributionDateWhenDistributed() {
        // Given
        LocalDateTime distDate = LocalDateTime.of(2024, 1, 20, 14, 0);
        share.setDistributionDate(distDate);

        // Then
        assertThat(share.getDistributionDate()).isEqualTo(distDate);
    }

    @Test
    @DisplayName("Should store notes")
    void shouldStoreNotes() {
        // Given
        String notes = "Investor share from promotional sales campaign";
        share.setNotes(notes);

        // Then
        assertThat(share.getNotes()).isEqualTo(notes);
    }

    // Status transition tests
    @Test
    @DisplayName("Should transition from PENDING to CALCULATED")
    void shouldTransitionFromPendingToCalculated() {
        // Given
        share.setStatus(InvestorShare.ShareStatus.PENDING);

        // When
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.CALCULATED);
    }

    @Test
    @DisplayName("Should transition from CALCULATED to DISTRIBUTED")
    void shouldTransitionFromCalculatedToDistributed() {
        // Given
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);

        // When
        share.setStatus(InvestorShare.ShareStatus.DISTRIBUTED);
        share.setDistributionDate(LocalDateTime.now());

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.DISTRIBUTED);
        assertThat(share.getDistributionDate()).isNotNull();
    }

    @Test
    @DisplayName("Should transition from CALCULATED to REINVESTED")
    void shouldTransitionFromCalculatedToReinvested() {
        // Given
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);

        // When
        share.setStatus(InvestorShare.ShareStatus.REINVESTED);

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.REINVESTED);
    }

    @Test
    @DisplayName("Should transition to CANCELLED from any status")
    void shouldTransitionToCancelledFromAnyStatus() {
        // From PENDING
        share.setStatus(InvestorShare.ShareStatus.PENDING);
        share.setStatus(InvestorShare.ShareStatus.CANCELLED);
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.CANCELLED);

        // From CALCULATED
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);
        share.setStatus(InvestorShare.ShareStatus.CANCELLED);
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.CANCELLED);
    }

    // Relationship tests
    @Test
    @DisplayName("Should maintain relationship with investment")
    void shouldMaintainRelationshipWithInvestment() {
        // Then
        assertThat(share.getInvestment()).isNotNull();
        assertThat(share.getInvestment()).isEqualTo(testInvestment);
        assertThat(share.getInvestment().getInvestmentNumber()).isEqualTo("INV-001");
    }

    @Test
    @DisplayName("Should maintain relationship with transaction")
    void shouldMaintainRelationshipWithTransaction() {
        // Then
        assertThat(share.getTransaction()).isNotNull();
        assertThat(share.getTransaction()).isEqualTo(testTransaction);
        assertThat(share.getTransaction().getTransactionNumber()).isEqualTo("TXN-001");
    }

    // Calculation tests
    @Test
    @DisplayName("Should correctly represent share percentage calculation")
    void shouldCorrectlyRepresentSharePercentageCalculation() {
        // Given: Transaction profit = 200, Investor share = 50, Percentage = 25%
        // Verification: 200 * 25% = 50

        // Then
        BigDecimal expectedShare = share.getProfitAmount()
            .multiply(share.getSharePercentage())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        assertThat(share.getShareAmount()).isEqualByComparingTo(expectedShare);
    }

    @Test
    @DisplayName("Should handle zero profit amount")
    void shouldHandleZeroProfitAmount() {
        // Given
        share.setProfitAmount(BigDecimal.ZERO);
        share.setShareAmount(BigDecimal.ZERO);

        // Then
        assertThat(share.getProfitAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(share.getShareAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should handle very small share amounts")
    void shouldHandleVerySmallShareAmounts() {
        // Given
        share.setProfitAmount(BigDecimal.valueOf(1.00));
        share.setSharePercentage(BigDecimal.valueOf(0.5));
        share.setShareAmount(BigDecimal.valueOf(0.005));

        // Then
        assertThat(share.getShareAmount()).isEqualByComparingTo(BigDecimal.valueOf(0.005));
    }

    @Test
    @DisplayName("Should handle large share amounts")
    void shouldHandleLargeShareAmounts() {
        // Given
        share.setTransactionAmount(BigDecimal.valueOf(1000000));
        share.setProfitAmount(BigDecimal.valueOf(200000));
        share.setSharePercentage(BigDecimal.valueOf(30));
        share.setShareAmount(BigDecimal.valueOf(60000));

        // Then
        assertThat(share.getShareAmount()).isEqualByComparingTo(BigDecimal.valueOf(60000));
    }

    // Workflow scenario tests
    @Test
    @DisplayName("Should support complete share lifecycle - Distribution")
    void shouldSupportCompleteShareLifecycleDistribution() {
        // Given - Initial pending state
        share.setStatus(InvestorShare.ShareStatus.PENDING);
        share.setDistributionDate(null);

        // When - Calculate share
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);
        share.setCalculationDate(LocalDateTime.now());

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.CALCULATED);

        // When - Distribute to investor
        share.setStatus(InvestorShare.ShareStatus.DISTRIBUTED);
        share.setDistributionDate(LocalDateTime.now());

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.DISTRIBUTED);
        assertThat(share.getDistributionDate()).isNotNull();
    }

    @Test
    @DisplayName("Should support complete share lifecycle - Reinvestment")
    void shouldSupportCompleteShareLifecycleReinvestment() {
        // Given - Initial pending state
        share.setStatus(InvestorShare.ShareStatus.PENDING);

        // When - Calculate share
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);
        share.setCalculationDate(LocalDateTime.now());

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.CALCULATED);

        // When - Reinvest profits
        share.setStatus(InvestorShare.ShareStatus.REINVESTED);
        share.setNotes("Profits reinvested into new investment round");

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.REINVESTED);
        assertThat(share.getNotes()).contains("reinvested");
    }

    @Test
    @DisplayName("Should support cancellation workflow")
    void shouldSupportCancellationWorkflow() {
        // Given
        share.setStatus(InvestorShare.ShareStatus.CALCULATED);

        // When - Share cancelled (e.g., transaction refunded)
        share.setStatus(InvestorShare.ShareStatus.CANCELLED);
        share.setNotes("Transaction was refunded");

        // Then
        assertThat(share.getStatus()).isEqualTo(InvestorShare.ShareStatus.CANCELLED);
        assertThat(share.getNotes()).isEqualTo("Transaction was refunded");
    }

    // Constructor and builder tests
    @Test
    @DisplayName("Should create with no-args constructor")
    void shouldCreateWithNoArgsConstructor() {
        // When
        InvestorShare newShare = new InvestorShare();

        // Then
        assertThat(newShare).isNotNull();
    }

    @Test
    @DisplayName("Should create with all-args constructor")
    void shouldCreateWithAllArgsConstructor() {
        // Given
        LocalDateTime calcDate = LocalDateTime.now();
        LocalDateTime distDate = LocalDateTime.now();

        // When
        InvestorShare newShare = new InvestorShare(
            "share-1",
            testInvestment,
            testTransaction,
            BigDecimal.valueOf(2000),
            BigDecimal.valueOf(400),
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(25),
            calcDate,
            InvestorShare.ShareStatus.DISTRIBUTED,
            distDate,
            "Test share"
        );

        // Then
        assertThat(newShare.getId()).isEqualTo("share-1");
        assertThat(newShare.getTransactionAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(newShare.getStatus()).isEqualTo(InvestorShare.ShareStatus.DISTRIBUTED);
    }

    @Test
    @DisplayName("Should create with builder")
    void shouldCreateWithBuilder() {
        // When
        InvestorShare newShare = InvestorShare.builder()
            .investment(testInvestment)
            .transaction(testTransaction)
            .transactionAmount(BigDecimal.valueOf(3000))
            .profitAmount(BigDecimal.valueOf(600))
            .shareAmount(BigDecimal.valueOf(150))
            .sharePercentage(BigDecimal.valueOf(25))
            .calculationDate(LocalDateTime.now())
            .status(InvestorShare.ShareStatus.CALCULATED)
            .notes("Builder test")
            .build();

        // Then
        assertThat(newShare.getTransactionAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(newShare.getShareAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(newShare.getStatus()).isEqualTo(InvestorShare.ShareStatus.CALCULATED);
        assertThat(newShare.getNotes()).isEqualTo("Builder test");
    }

    // Edge cases
    @Test
    @DisplayName("Should handle 100% share percentage")
    void shouldHandle100PercentSharePercentage() {
        // Given
        share.setSharePercentage(BigDecimal.valueOf(100));
        share.setProfitAmount(BigDecimal.valueOf(1000));
        share.setShareAmount(BigDecimal.valueOf(1000));

        // Then
        assertThat(share.getSharePercentage()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(share.getShareAmount()).isEqualByComparingTo(share.getProfitAmount());
    }

    @Test
    @DisplayName("Should handle decimal percentages")
    void shouldHandleDecimalPercentages() {
        // Given
        share.setSharePercentage(BigDecimal.valueOf(12.5));
        share.setProfitAmount(BigDecimal.valueOf(1000));
        share.setShareAmount(BigDecimal.valueOf(125));

        // Then
        assertThat(share.getSharePercentage()).isEqualByComparingTo(BigDecimal.valueOf(12.5));
        assertThat(share.getShareAmount()).isEqualByComparingTo(BigDecimal.valueOf(125));
    }

    @Test
    @DisplayName("Should handle transaction with negative profit (loss)")
    void shouldHandleTransactionWithNegativeProfit() {
        // Given - Transaction resulted in a loss
        share.setProfitAmount(BigDecimal.valueOf(-100));
        share.setShareAmount(BigDecimal.valueOf(-25));

        // Then - Share amount should be negative (loss for investor)
        assertThat(share.getProfitAmount()).isLessThan(BigDecimal.ZERO);
        assertThat(share.getShareAmount()).isLessThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should maintain precision for financial calculations")
    void shouldMaintainPrecisionForFinancialCalculations() {
        // Given
        share.setTransactionAmount(BigDecimal.valueOf(1234.56));
        share.setProfitAmount(BigDecimal.valueOf(246.91));
        share.setSharePercentage(BigDecimal.valueOf(33.33));
        share.setShareAmount(BigDecimal.valueOf(82.29));

        // Then - Values should maintain 2 decimal precision
        assertThat(share.getTransactionAmount().scale()).isEqualTo(2);
        assertThat(share.getProfitAmount().scale()).isEqualTo(2);
        assertThat(share.getShareAmount().scale()).isEqualTo(2);
    }
}
