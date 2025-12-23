package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvestorDistribution Domain Tests")
class InvestorDistributionTest {

    private InvestorDistribution distribution;
    private Investment testInvestment;

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

        distribution = InvestorDistribution.builder()
            .investment(testInvestment)
            .periodStart(LocalDateTime.now().minusMonths(1))
            .periodEnd(LocalDateTime.now())
            .totalSalesRevenue(BigDecimal.valueOf(500000))
            .totalProfit(BigDecimal.valueOf(100000))
            .investorSharePercentage(BigDecimal.valueOf(20))
            .investorProfitAmount(BigDecimal.valueOf(20000))
            .distributionAmount(BigDecimal.valueOf(20000))
            .build();
    }

    // Default values tests
    @Test
    @DisplayName("Should have default status as CALCULATED")
    void shouldHaveDefaultStatusAsCalculated() {
        // Given
        InvestorDistribution newDistribution = InvestorDistribution.builder()
            .investment(testInvestment)
            .periodStart(LocalDateTime.now())
            .periodEnd(LocalDateTime.now())
            .totalSalesRevenue(BigDecimal.ZERO)
            .totalProfit(BigDecimal.ZERO)
            .investorSharePercentage(BigDecimal.ZERO)
            .investorProfitAmount(BigDecimal.ZERO)
            .distributionAmount(BigDecimal.ZERO)
            .build();

        // Then
        assertThat(newDistribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.CALCULATED);
    }

    // markAsPaid tests
    @Test
    @DisplayName("markAsPaid - Should change status to PAID")
    void markAsPaid_shouldChangeStatusToPaid() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        String paymentRef = "PAY-20240115-001";

        // When
        distribution.markAsPaid(paymentRef);

        // Then
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.PAID);
    }

    @Test
    @DisplayName("markAsPaid - Should set payment reference")
    void markAsPaid_shouldSetPaymentReference() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        String paymentRef = "PAY-20240115-001";

        // When
        distribution.markAsPaid(paymentRef);

        // Then
        assertThat(distribution.getPaymentReference()).isEqualTo(paymentRef);
    }

    @Test
    @DisplayName("markAsPaid - Should set distribution date to current time")
    void markAsPaid_shouldSetDistributionDateToCurrentTime() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        LocalDateTime beforePaid = LocalDateTime.now().minusSeconds(1);

        // When
        distribution.markAsPaid("PAY-001");

        // Then
        assertThat(distribution.getDistributionDate()).isNotNull();
        assertThat(distribution.getDistributionDate()).isAfter(beforePaid);
        assertThat(distribution.getDistributionDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("markAsPaid - Should update all fields together")
    void markAsPaid_shouldUpdateAllFieldsTogether() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        String paymentRef = "PAY-20240115-001";
        LocalDateTime beforePaid = LocalDateTime.now().minusSeconds(1);

        // When
        distribution.markAsPaid(paymentRef);

        // Then
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.PAID);
        assertThat(distribution.getPaymentReference()).isEqualTo(paymentRef);
        assertThat(distribution.getDistributionDate()).isAfter(beforePaid);
    }

    @Test
    @DisplayName("markAsPaid - Should work from CALCULATED status")
    void markAsPaid_shouldWorkFromCalculatedStatus() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.CALCULATED);

        // When
        distribution.markAsPaid("PAY-001");

        // Then
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.PAID);
    }

    @Test
    @DisplayName("markAsPaid - Should override previous payment reference")
    void markAsPaid_shouldOverridePreviousPaymentReference() {
        // Given
        distribution.setPaymentReference("OLD-REF");

        // When
        distribution.markAsPaid("NEW-REF");

        // Then
        assertThat(distribution.getPaymentReference()).isEqualTo("NEW-REF");
    }

    // markAsFailed tests
    @Test
    @DisplayName("markAsFailed - Should change status to FAILED")
    void markAsFailed_shouldChangeStatusToFailed() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        String reason = "Insufficient funds";

        // When
        distribution.markAsFailed(reason);

        // Then
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.FAILED);
    }

    @Test
    @DisplayName("markAsFailed - Should set notes with failure reason")
    void markAsFailed_shouldSetNotesWithFailureReason() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        String reason = "Payment gateway timeout";

        // When
        distribution.markAsFailed(reason);

        // Then
        assertThat(distribution.getNotes()).isEqualTo(reason);
    }

    @Test
    @DisplayName("markAsFailed - Should override previous notes")
    void markAsFailed_shouldOverridePreviousNotes() {
        // Given
        distribution.setNotes("Previous notes");
        String reason = "New failure reason";

        // When
        distribution.markAsFailed(reason);

        // Then
        assertThat(distribution.getNotes()).isEqualTo(reason);
    }

    @Test
    @DisplayName("markAsFailed - Should work from any status")
    void markAsFailed_shouldWorkFromAnyStatus() {
        // Test from CALCULATED
        distribution.setStatus(InvestorDistribution.DistributionStatus.CALCULATED);
        distribution.markAsFailed("Test failure");
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.FAILED);

        // Test from APPROVED
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        distribution.markAsFailed("Test failure");
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.FAILED);
    }

    // canBePaid tests
    @Test
    @DisplayName("canBePaid - Should return true when status is APPROVED")
    void canBePaid_shouldReturnTrueWhenStatusIsApproved() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);

        // When
        boolean canBePaid = distribution.canBePaid();

        // Then
        assertThat(canBePaid).isTrue();
    }

    @Test
    @DisplayName("canBePaid - Should return false when status is CALCULATED")
    void canBePaid_shouldReturnFalseWhenStatusIsCalculated() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.CALCULATED);

        // When
        boolean canBePaid = distribution.canBePaid();

        // Then
        assertThat(canBePaid).isFalse();
    }

    @Test
    @DisplayName("canBePaid - Should return false when status is PAID")
    void canBePaid_shouldReturnFalseWhenStatusIsPaid() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.PAID);

        // When
        boolean canBePaid = distribution.canBePaid();

        // Then
        assertThat(canBePaid).isFalse();
    }

    @Test
    @DisplayName("canBePaid - Should return false when status is FAILED")
    void canBePaid_shouldReturnFalseWhenStatusIsFailed() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.FAILED);

        // When
        boolean canBePaid = distribution.canBePaid();

        // Then
        assertThat(canBePaid).isFalse();
    }

    @Test
    @DisplayName("canBePaid - Should return false when status is CANCELLED")
    void canBePaid_shouldReturnFalseWhenStatusIsCancelled() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.CANCELLED);

        // When
        boolean canBePaid = distribution.canBePaid();

        // Then
        assertThat(canBePaid).isFalse();
    }

    // isPaid tests
    @Test
    @DisplayName("isPaid - Should return true when status is PAID")
    void isPaid_shouldReturnTrueWhenStatusIsPaid() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.PAID);

        // When
        boolean isPaid = distribution.isPaid();

        // Then
        assertThat(isPaid).isTrue();
    }

    @Test
    @DisplayName("isPaid - Should return false when status is CALCULATED")
    void isPaid_shouldReturnFalseWhenStatusIsCalculated() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.CALCULATED);

        // When
        boolean isPaid = distribution.isPaid();

        // Then
        assertThat(isPaid).isFalse();
    }

    @Test
    @DisplayName("isPaid - Should return false when status is APPROVED")
    void isPaid_shouldReturnFalseWhenStatusIsApproved() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);

        // When
        boolean isPaid = distribution.isPaid();

        // Then
        assertThat(isPaid).isFalse();
    }

    @Test
    @DisplayName("isPaid - Should return false when status is FAILED")
    void isPaid_shouldReturnFalseWhenStatusIsFailed() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.FAILED);

        // When
        boolean isPaid = distribution.isPaid();

        // Then
        assertThat(isPaid).isFalse();
    }

    @Test
    @DisplayName("isPaid - Should return false when status is CANCELLED")
    void isPaid_shouldReturnFalseWhenStatusIsCancelled() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.CANCELLED);

        // When
        boolean isPaid = distribution.isPaid();

        // Then
        assertThat(isPaid).isFalse();
    }

    // DistributionStatus enum tests
    @Test
    @DisplayName("DistributionStatus - All enum values should exist")
    void distributionStatus_allEnumValuesShouldExist() {
        InvestorDistribution.DistributionStatus[] statuses = InvestorDistribution.DistributionStatus.values();

        assertThat(statuses).hasSize(5);
        assertThat(statuses).contains(
            InvestorDistribution.DistributionStatus.CALCULATED,
            InvestorDistribution.DistributionStatus.APPROVED,
            InvestorDistribution.DistributionStatus.PAID,
            InvestorDistribution.DistributionStatus.FAILED,
            InvestorDistribution.DistributionStatus.CANCELLED
        );
    }

    @Test
    @DisplayName("DistributionStatus - Should have correct display names")
    void distributionStatus_shouldHaveCorrectDisplayNames() {
        assertThat(InvestorDistribution.DistributionStatus.CALCULATED.getDisplayName()).isEqualTo("Calculated");
        assertThat(InvestorDistribution.DistributionStatus.APPROVED.getDisplayName()).isEqualTo("Approved");
        assertThat(InvestorDistribution.DistributionStatus.PAID.getDisplayName()).isEqualTo("Paid");
        assertThat(InvestorDistribution.DistributionStatus.FAILED.getDisplayName()).isEqualTo("Failed");
        assertThat(InvestorDistribution.DistributionStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
    }

    // Field storage tests
    @Test
    @DisplayName("Should store all financial fields correctly")
    void shouldStoreAllFinancialFieldsCorrectly() {
        // Given
        BigDecimal revenue = BigDecimal.valueOf(1000000.50);
        BigDecimal profit = BigDecimal.valueOf(250000.75);
        BigDecimal percentage = BigDecimal.valueOf(25.50);
        BigDecimal profitAmount = BigDecimal.valueOf(63750.19);
        BigDecimal distAmount = BigDecimal.valueOf(63750.19);

        distribution.setTotalSalesRevenue(revenue);
        distribution.setTotalProfit(profit);
        distribution.setInvestorSharePercentage(percentage);
        distribution.setInvestorProfitAmount(profitAmount);
        distribution.setDistributionAmount(distAmount);

        // Then
        assertThat(distribution.getTotalSalesRevenue()).isEqualByComparingTo(revenue);
        assertThat(distribution.getTotalProfit()).isEqualByComparingTo(profit);
        assertThat(distribution.getInvestorSharePercentage()).isEqualByComparingTo(percentage);
        assertThat(distribution.getInvestorProfitAmount()).isEqualByComparingTo(profitAmount);
        assertThat(distribution.getDistributionAmount()).isEqualByComparingTo(distAmount);
    }

    @Test
    @DisplayName("Should store period dates correctly")
    void shouldStorePeriodDatesCorrectly() {
        // Given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59);

        distribution.setPeriodStart(start);
        distribution.setPeriodEnd(end);

        // Then
        assertThat(distribution.getPeriodStart()).isEqualTo(start);
        assertThat(distribution.getPeriodEnd()).isEqualTo(end);
    }

    @Test
    @DisplayName("Should store calculation details")
    void shouldStoreCalculationDetails() {
        // Given
        String details = "Profit calculation: Revenue (500000) * Margin (20%) * Investor Share (20%) = 20000";
        distribution.setCalculationDetails(details);

        // Then
        assertThat(distribution.getCalculationDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("Should store notes")
    void shouldStoreNotes() {
        // Given
        String notes = "Distribution delayed due to bank holiday";
        distribution.setNotes(notes);

        // Then
        assertThat(distribution.getNotes()).isEqualTo(notes);
    }

    // Workflow scenario tests
    @Test
    @DisplayName("Should support complete distribution workflow")
    void shouldSupportCompleteDistributionWorkflow() {
        // Given - Initial calculation
        distribution.setStatus(InvestorDistribution.DistributionStatus.CALCULATED);
        assertThat(distribution.canBePaid()).isFalse();
        assertThat(distribution.isPaid()).isFalse();

        // When - Approved for payment
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);

        // Then
        assertThat(distribution.canBePaid()).isTrue();
        assertThat(distribution.isPaid()).isFalse();

        // When - Payment executed
        distribution.markAsPaid("PAY-001");

        // Then
        assertThat(distribution.canBePaid()).isFalse();
        assertThat(distribution.isPaid()).isTrue();
        assertThat(distribution.getPaymentReference()).isEqualTo("PAY-001");
        assertThat(distribution.getDistributionDate()).isNotNull();
    }

    @Test
    @DisplayName("Should support failure workflow")
    void shouldSupportFailureWorkflow() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.APPROVED);
        assertThat(distribution.canBePaid()).isTrue();

        // When - Payment fails
        distribution.markAsFailed("Bank transfer failed");

        // Then
        assertThat(distribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.FAILED);
        assertThat(distribution.canBePaid()).isFalse();
        assertThat(distribution.isPaid()).isFalse();
        assertThat(distribution.getNotes()).isEqualTo("Bank transfer failed");
    }

    @Test
    @DisplayName("Should support cancellation workflow")
    void shouldSupportCancellationWorkflow() {
        // Given
        distribution.setStatus(InvestorDistribution.DistributionStatus.CALCULATED);

        // When - Distribution cancelled
        distribution.setStatus(InvestorDistribution.DistributionStatus.CANCELLED);

        // Then
        assertThat(distribution.canBePaid()).isFalse();
        assertThat(distribution.isPaid()).isFalse();
    }

    // Constructor and builder tests
    @Test
    @DisplayName("Should create with no-args constructor")
    void shouldCreateWithNoArgsConstructor() {
        // When
        InvestorDistribution newDistribution = new InvestorDistribution();

        // Then
        assertThat(newDistribution).isNotNull();
    }

    @Test
    @DisplayName("Should create with all-args constructor")
    void shouldCreateWithAllArgsConstructor() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime distDate = LocalDateTime.now();

        // When
        InvestorDistribution newDistribution = new InvestorDistribution(
            "dist-1",
            testInvestment,
            start,
            end,
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(20000),
            BigDecimal.valueOf(25),
            BigDecimal.valueOf(5000),
            BigDecimal.valueOf(5000),
            InvestorDistribution.DistributionStatus.PAID,
            distDate,
            "PAY-001",
            "Test notes",
            "Test calculation"
        );

        // Then
        assertThat(newDistribution.getId()).isEqualTo("dist-1");
        assertThat(newDistribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.PAID);
        assertThat(newDistribution.getPaymentReference()).isEqualTo("PAY-001");
    }

    @Test
    @DisplayName("Should create with builder")
    void shouldCreateWithBuilder() {
        // When
        InvestorDistribution newDistribution = InvestorDistribution.builder()
            .investment(testInvestment)
            .periodStart(LocalDateTime.now())
            .periodEnd(LocalDateTime.now())
            .totalSalesRevenue(BigDecimal.valueOf(50000))
            .totalProfit(BigDecimal.valueOf(10000))
            .investorSharePercentage(BigDecimal.valueOf(30))
            .investorProfitAmount(BigDecimal.valueOf(3000))
            .distributionAmount(BigDecimal.valueOf(3000))
            .status(InvestorDistribution.DistributionStatus.APPROVED)
            .build();

        // Then
        assertThat(newDistribution.getInvestorSharePercentage()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(newDistribution.getStatus()).isEqualTo(InvestorDistribution.DistributionStatus.APPROVED);
    }
}
