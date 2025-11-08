package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.investment.config.ProfitCalculationConfig;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestmentRound;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvestmentProfitServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestorDistributionRepository distributionRepository;

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ProfitCalculationConfig profitConfig;

    @InjectMocks
    private InvestmentProfitService investmentProfitService;

    private Shop testShop;
    private User testInvestor;
    private Investment testInvestment;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        testInvestor = new User();
        testInvestor.setId("investor-1");
        testInvestor.setUsername("test-investor");

        Category testCategory = new Category();
        testCategory.setId("category-1");
        testCategory.setName("Test Category");

        testProduct = new Product();
        testProduct.setId("product-1");
        testProduct.setName("Test Product");
        testProduct.setCategory(testCategory);

        InvestmentRound testRound = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TEST-2025-Q1-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .build();

        testInvestment = Investment.builder()
            .id("investment-1")
            .investmentNumber("INV-001")
            .investor(testInvestor)
            .shop(testShop)
            .investmentRound(testRound)
            .amount(BigDecimal.valueOf(10000))
            .status(Investment.InvestmentStatus.ACTIVE)
            .totalProfitEarned(BigDecimal.ZERO)
            .build();

        // Setup ProfitCalculationConfig mocks with lenient stubbing
        lenient().when(profitConfig.getOperationalCostPercentage()).thenReturn(BigDecimal.valueOf(0.70)); // 70% of revenue
        lenient().when(profitConfig.getDefaultProfitMargin()).thenReturn(BigDecimal.valueOf(0.30)); // 30% profit margin
        lenient().when(profitConfig.getProfitMarginForCategory(anyString())).thenReturn(BigDecimal.valueOf(0.30));
    }

    @Test
    void testCalculateInvestmentDistribution_ShopWideInvestment() {
        // Given
        LocalDateTime periodStart = LocalDateTime.now().minusDays(30);
        LocalDateTime periodEnd = LocalDateTime.now();
        BigDecimal totalRevenue = BigDecimal.valueOf(50000);

        // Mock round aggregation: testInvestment has 10,000, total round is 50,000 = 20%
        when(investmentRepository.sumAmountByInvestmentRoundId("round-1"))
            .thenReturn(BigDecimal.valueOf(50000));

        when(distributionRepository.existsByInvestmentAndPeriodStartAndPeriodEnd(testInvestment, periodStart, periodEnd))
            .thenReturn(false);
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(testShop.getId(), periodStart, periodEnd))
            .thenReturn(Optional.of(totalRevenue));
        when(distributionRepository.save(any(InvestorDistribution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<InvestorDistribution> result = investmentProfitService
            .calculateInvestmentDistribution(testInvestment, periodStart, periodEnd);

        // Then
        assertTrue(result.isPresent());
        InvestorDistribution distribution = result.get();

        assertEquals(totalRevenue, distribution.getTotalSalesRevenue());
        assertEquals(0, BigDecimal.valueOf(4500).compareTo(distribution.getTotalProfit())); // Net profit: (50000 - 35000) * 0.30 = 4500
        assertEquals(0, BigDecimal.valueOf(20).compareTo(distribution.getInvestorSharePercentage()));
        assertEquals(0, BigDecimal.valueOf(900).compareTo(distribution.getInvestorProfitAmount())); // 20% of 4500
        assertEquals(InvestorDistribution.DistributionStatus.CALCULATED, distribution.getStatus());

        verify(distributionRepository).save(any(InvestorDistribution.class));
        verify(investmentRepository).save(testInvestment);
        verify(auditService).logFinancialTransaction(any(), any(), any(), any(), any(), any(), eq(true));
    }

    @Test
    void testCalculateInvestmentDistribution_ProductSpecificInvestment() {
        // Given - Product-specific now uses shop-wide revenue (see InvestmentProfitService)
        testInvestment.getInvestmentRound().setInvestmentType(Investment.InvestmentType.PRODUCT_SPECIFIC);

        LocalDateTime periodStart = LocalDateTime.now().minusDays(30);
        LocalDateTime periodEnd = LocalDateTime.now();
        BigDecimal shopRevenue = BigDecimal.valueOf(20000);

        // Mock round aggregation: testInvestment has 10,000, total round is 50,000 = 20%
        when(investmentRepository.sumAmountByInvestmentRoundId("round-1"))
            .thenReturn(BigDecimal.valueOf(50000));

        when(distributionRepository.existsByInvestmentAndPeriodStartAndPeriodEnd(testInvestment, periodStart, periodEnd))
            .thenReturn(false);
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(eq(testShop.getId()), eq(periodStart), eq(periodEnd)))
            .thenReturn(Optional.of(shopRevenue));
        when(distributionRepository.save(any(InvestorDistribution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<InvestorDistribution> result = investmentProfitService
            .calculateInvestmentDistribution(testInvestment, periodStart, periodEnd);

        // Then
        assertTrue(result.isPresent());
        InvestorDistribution distribution = result.get();

        assertEquals(shopRevenue, distribution.getTotalSalesRevenue());
        assertEquals(0, BigDecimal.valueOf(1800).compareTo(distribution.getTotalProfit())); // Net profit: (20000 - 14000) * 0.30 = 1800
        assertEquals(0, BigDecimal.valueOf(20).compareTo(distribution.getInvestorSharePercentage()));
        assertEquals(0, BigDecimal.valueOf(360).compareTo(distribution.getInvestorProfitAmount())); // 20% of 1800
    }

    @Test
    void testCalculateInvestmentDistribution_AlreadyExists() {
        // Given
        LocalDateTime periodStart = LocalDateTime.now().minusDays(30);
        LocalDateTime periodEnd = LocalDateTime.now();

        when(distributionRepository.existsByInvestmentAndPeriodStartAndPeriodEnd(testInvestment, periodStart, periodEnd))
            .thenReturn(true);

        // When
        Optional<InvestorDistribution> result = investmentProfitService
            .calculateInvestmentDistribution(testInvestment, periodStart, periodEnd);

        // Then
        assertFalse(result.isPresent());
        verify(distributionRepository, never()).save(any(InvestorDistribution.class));
        verify(investmentRepository, never()).save(any(Investment.class));
    }

    @Test
    void testCalculateInvestmentDistribution_NoProfit() {
        // Given
        LocalDateTime periodStart = LocalDateTime.now().minusDays(30);
        LocalDateTime periodEnd = LocalDateTime.now();

        when(distributionRepository.existsByInvestmentAndPeriodStartAndPeriodEnd(testInvestment, periodStart, periodEnd))
            .thenReturn(false);
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(testShop.getId(), periodStart, periodEnd))
            .thenReturn(Optional.of(BigDecimal.ZERO));

        // When
        Optional<InvestorDistribution> result = investmentProfitService
            .calculateInvestmentDistribution(testInvestment, periodStart, periodEnd);

        // Then
        assertFalse(result.isPresent());
        verify(distributionRepository, never()).save(any(InvestorDistribution.class));
        verify(investmentRepository, never()).save(any(Investment.class));
    }

    @Test
    void testApproveDistribution() {
        // Given
        InvestorDistribution distribution = InvestorDistribution.builder()
            .id("dist-1")
            .investment(testInvestment)
            .status(InvestorDistribution.DistributionStatus.CALCULATED)
            .distributionAmount(BigDecimal.valueOf(1000))
            .build();

        when(distributionRepository.findById("dist-1")).thenReturn(Optional.of(distribution));
        when(distributionRepository.save(any(InvestorDistribution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        investmentProfitService.approveDistribution("dist-1", "approver");

        // Then
        assertEquals(InvestorDistribution.DistributionStatus.APPROVED, distribution.getStatus());
        assertEquals("Approved by approver", distribution.getNotes());
        verify(distributionRepository).save(distribution);
        verify(auditService).logFinancialTransaction(any(), any(), any(), any(), any(), any(), eq(true));
    }

    @Test
    void testApproveDistribution_InvalidStatus() {
        // Given
        InvestorDistribution distribution = InvestorDistribution.builder()
            .id("dist-1")
            .status(InvestorDistribution.DistributionStatus.PAID)
            .build();

        when(distributionRepository.findById("dist-1")).thenReturn(Optional.of(distribution));

        // When & Then
        assertThrows(IllegalStateException.class,
            () -> investmentProfitService.approveDistribution("dist-1", "approver"));
    }

    @Test
    void testMarkDistributionAsPaid() {
        // Given
        InvestorDistribution distribution = InvestorDistribution.builder()
            .id("dist-1")
            .investment(testInvestment)
            .status(InvestorDistribution.DistributionStatus.APPROVED)
            .distributionAmount(BigDecimal.valueOf(1000))
            .build();

        when(distributionRepository.findById("dist-1")).thenReturn(Optional.of(distribution));
        when(distributionRepository.save(any(InvestorDistribution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        investmentProfitService.markDistributionAsPaid("dist-1", "PAY-12345", "payer");

        // Then
        assertEquals(InvestorDistribution.DistributionStatus.PAID, distribution.getStatus());
        assertEquals("PAY-12345", distribution.getPaymentReference());
        assertNotNull(distribution.getDistributionDate());

        assertEquals(0, BigDecimal.valueOf(1000).compareTo(testInvestment.getTotalWithdrawn()));

        verify(distributionRepository).save(distribution);
        verify(investmentRepository).save(testInvestment);
        verify(auditService).logFinancialTransaction(any(), any(), any(), any(), any(), any(), eq(true));
    }

    @Test
    void testCalculateProfitDistributions() {
        // Given
        LocalDateTime periodStart = LocalDateTime.now().minusDays(30);
        LocalDateTime periodEnd = LocalDateTime.now();

        // Mock round aggregation: testInvestment has 10,000, total round is 50,000 = 20%
        when(investmentRepository.sumAmountByInvestmentRoundId("round-1"))
            .thenReturn(BigDecimal.valueOf(50000));

        when(investmentRepository.findActiveInvestments())
            .thenReturn(List.of(testInvestment));
        when(distributionRepository.existsByInvestmentAndPeriodStartAndPeriodEnd(testInvestment, periodStart, periodEnd))
            .thenReturn(false);
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(testShop.getId(), periodStart, periodEnd))
            .thenReturn(Optional.of(BigDecimal.valueOf(50000)));
        when(distributionRepository.save(any(InvestorDistribution.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<InvestorDistribution> result = investmentProfitService
            .calculateProfitDistributions(periodStart, periodEnd);

        // Then
        assertEquals(1, result.size());
        InvestorDistribution distribution = result.get(0);
        assertEquals(testInvestment, distribution.getInvestment());
        assertEquals(0, BigDecimal.valueOf(900).compareTo(distribution.getInvestorProfitAmount()));
    }
}