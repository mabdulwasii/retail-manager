package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestmentRound;
import com.princely.shopmanager.investment.dto.InvestmentRoundCreateRequest;
import com.princely.shopmanager.investment.dto.InvestmentRoundResponse;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestmentRoundRepository;
import com.princely.shopmanager.investment.validator.InvestmentRoundValidator;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvestmentRoundService - Uniqueness Tests")
class InvestmentRoundServiceTest {

    @Mock
    private InvestmentRoundRepository investmentRoundRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvestmentRoundValidator validator;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InvestmentRoundService investmentRoundService;

    private Tenant testTenant;
    private Shop testShop;
    private User testInvestor;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder()
            .id("tenant-1")
            .name("Test Tenant")
            .build();

        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .tenant(testTenant)
            .build();

        testInvestor = User.builder()
            .id("investor-1")
            .username("investor@test.com")
            .email("investor@test.com")
            .firstName("John")
            .lastName("Doe")
            .tenant(testTenant)
            .build();
    }

    @Test
    @DisplayName("Should generate unique round numbers for same shop")
    void shouldGenerateUniqueRoundNumbers() {
        // Given: Shop has 2 existing rounds
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(investmentRoundRepository.countByShopId("shop-1"))
            .thenReturn(0L)  // First call
            .thenReturn(1L)  // Second call
            .thenReturn(2L); // Third call
        when(userRepository.findById("investor-1")).thenReturn(Optional.of(testInvestor));
        when(validator.validate(any())).thenReturn(List.of());
        when(investmentRoundRepository.save(any(InvestmentRound.class)))
            .thenAnswer(invocation -> {
                InvestmentRound round = invocation.getArgument(0);
                round.setId("round-id-" + System.currentTimeMillis());
                return round;
            });
        when(investmentRepository.countByInvestmentRoundId(anyString())).thenReturn(0L);
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Create 3 investment rounds
        InvestmentRoundCreateRequest request1 = createTestRequest();
        InvestmentRoundCreateRequest request2 = createTestRequest();
        InvestmentRoundCreateRequest request3 = createTestRequest();

        InvestmentRoundResponse response1 = investmentRoundService.createInvestmentRound(request1, "admin");
        InvestmentRoundResponse response2 = investmentRoundService.createInvestmentRound(request2, "admin");
        InvestmentRoundResponse response3 = investmentRoundService.createInvestmentRound(request3, "admin");

        // Then: All round numbers should be unique
        assertThat(response1.getRoundNumber()).contains("-001");
        assertThat(response2.getRoundNumber()).contains("-002");
        assertThat(response3.getRoundNumber()).contains("-003");

        // Verify countByShopId was called for sequence generation
        verify(investmentRoundRepository, times(3)).countByShopId("shop-1");
    }

    @Test
    @DisplayName("Should generate unique investment numbers within same round")
    void shouldGenerateUniqueInvestmentNumbers() {
        // Given: Round with ID
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(investmentRoundRepository.countByShopId("shop-1")).thenReturn(0L);
        when(userRepository.findById("investor-1")).thenReturn(Optional.of(testInvestor));
        when(validator.validate(any())).thenReturn(List.of());

        InvestmentRound savedRound = InvestmentRound.builder()
            .id("round-123")
            .roundNumber("ROUND-TES-2025-Q4-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.save(any(InvestmentRound.class))).thenReturn(savedRound);

        // Mock countByInvestmentRoundId to return 0, 1, 2 for sequential investments
        when(investmentRepository.countByInvestmentRoundId("round-123"))
            .thenReturn(0L)  // First investor
            .thenReturn(1L)  // Second investor
            .thenReturn(2L); // Third investor

        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Create round with 3 investors
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(BigDecimal.valueOf(10000))
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(BigDecimal.valueOf(20000))
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(BigDecimal.valueOf(30000))
                    .build()
            ))
            .build();

        InvestmentRoundResponse response = investmentRoundService.createInvestmentRound(request, "admin");

        // Then: Investment numbers should be sequential
        assertThat(response.getInvestments()).hasSize(3);

        // Extract all investment numbers and verify they're unique and follow pattern
        List<String> investmentNumbers = response.getInvestments().stream()
            .map(InvestmentRoundResponse.InvestmentSummary::getInvestmentNumber)
            .sorted()
            .toList();

        // Investment numbers now include nanoTime suffix for collision prevention
        // Format: INV-{ROUND}-{SEQ}-{NANOTIME}
        assertThat(investmentNumbers).hasSize(3);
        assertThat(investmentNumbers.get(0)).matches("INV-ROUND-TES-2025-Q4-001-001-\\d+");
        assertThat(investmentNumbers.get(1)).matches("INV-ROUND-TES-2025-Q4-001-002-\\d+");
        assertThat(investmentNumbers.get(2)).matches("INV-ROUND-TES-2025-Q4-001-003-\\d+");

        // Verify all numbers are unique
        assertThat(investmentNumbers).doesNotHaveDuplicates();

        // Verify countByInvestmentRoundId was called 3 times
        verify(investmentRepository, times(3)).countByInvestmentRoundId("round-123");
    }

    @Test
    @DisplayName("Should use database count for investment numbers when adding to existing round")
    void shouldUseDatabaseCountWhenAddingInvestor() {
        // Given: Existing round with 5 investments in database
        InvestmentRound existingRound = InvestmentRound.builder()
            .id("round-existing")
            .roundNumber("ROUND-TES-2025-Q4-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.findById("round-existing")).thenReturn(Optional.of(existingRound));
        when(userRepository.findById("investor-1")).thenReturn(Optional.of(testInvestor));

        // Database has 5 investments, but in-memory collection is empty
        when(investmentRepository.countByInvestmentRoundId("round-existing")).thenReturn(5L);
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Add new investor to existing round
        InvestmentRoundCreateRequest.InvestorInput newInvestor = InvestmentRoundCreateRequest.InvestorInput.builder()
            .investorId("investor-1")
            .amount(BigDecimal.valueOf(15000))
            .build();

        InvestmentRoundResponse response = investmentRoundService.addInvestorToRound(
            "round-existing",
            newInvestor,
            "admin"
        );

        // Then: New investment should get sequence 006 (not 001)
        // Format: INV-{ROUND}-{SEQ}-{NANOTIME}
        assertThat(response.getInvestments()).hasSize(1);
        String investmentNumber = response.getInvestments().get(0).getInvestmentNumber();
        // Should contain sequence 006 followed by nanoTime suffix
        // Round number is ROUND-TES-2025-Q4-001 (line 209)
        assertThat(investmentNumber).matches("INV-ROUND-TES-2025-Q4-001-006-\\d+");

        // Verify database count was used
        verify(investmentRepository).countByInvestmentRoundId("round-existing");
    }

    @Test
    @DisplayName("Should generate round numbers scoped by shop")
    void shouldGenerateRoundNumbersScopedByShop() {
        // Given: Two different shops
        Shop shop1 = Shop.builder().id("shop-1").name("Shop One").tenant(testTenant).build();
        Shop shop2 = Shop.builder().id("shop-2").name("Shop Two").tenant(testTenant).build();

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(shop1));
        when(shopRepository.findById("shop-2")).thenReturn(Optional.of(shop2));
        when(investmentRoundRepository.countByShopId("shop-1")).thenReturn(0L);
        when(investmentRoundRepository.countByShopId("shop-2")).thenReturn(0L);
        when(userRepository.findById("investor-1")).thenReturn(Optional.of(testInvestor));
        when(validator.validate(any())).thenReturn(List.of());
        when(investmentRoundRepository.save(any(InvestmentRound.class)))
            .thenAnswer(invocation -> {
                InvestmentRound round = invocation.getArgument(0);
                round.setId("round-id-" + System.currentTimeMillis());
                return round;
            });
        when(investmentRepository.countByInvestmentRoundId(anyString())).thenReturn(0L);
        when(investmentRepository.save(any(Investment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Create rounds for both shops
        InvestmentRoundCreateRequest request1 = createTestRequestForShop("shop-1");
        InvestmentRoundCreateRequest request2 = createTestRequestForShop("shop-2");

        InvestmentRoundResponse response1 = investmentRoundService.createInvestmentRound(request1, "admin");
        InvestmentRoundResponse response2 = investmentRoundService.createInvestmentRound(request2, "admin");

        // Then: Both should get -001 because they're different shops
        assertThat(response1.getRoundNumber()).contains("-001");
        assertThat(response2.getRoundNumber()).contains("-001");

        // Verify each shop's count was queried separately
        verify(investmentRoundRepository).countByShopId("shop-1");
        verify(investmentRoundRepository).countByShopId("shop-2");
    }

    private InvestmentRoundCreateRequest createTestRequest() {
        return InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDate.now().plusMonths(12))
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(BigDecimal.valueOf(50000))
                    .notes("Test investment")
                    .build()
            ))
            .notes("Test round")
            .build();
    }

    private InvestmentRoundCreateRequest createTestRequestForShop(String shopId) {
        return InvestmentRoundCreateRequest.builder()
            .shopId(shopId)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .maturityDate(LocalDate.now().plusMonths(12))
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(BigDecimal.valueOf(50000))
                    .notes("Test investment")
                    .build()
            ))
            .notes("Test round")
            .build();
    }

    @Test
    @DisplayName("Should get investment round by ID")
    void shouldGetInvestmentRound() {
        // Given
        InvestmentRound round = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(round));

        // When
        InvestmentRoundResponse response = investmentRoundService.getInvestmentRound("round-1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("round-1");
        assertThat(response.getRoundNumber()).isEqualTo("ROUND-TES-2025-Q1-001");
        verify(investmentRoundRepository).findById("round-1");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent round")
    void shouldThrowExceptionWhenGettingNonExistentRound() {
        // Given
        when(investmentRoundRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> investmentRoundService.getInvestmentRound("non-existent"));

        verify(investmentRoundRepository).findById("non-existent");
    }

    @Test
    @DisplayName("Should update investment round successfully")
    void shouldUpdateInvestmentRound() {
        // Given
        InvestmentRound existingRound = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .notes("Old notes")
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(existingRound));
        when(investmentRoundRepository.save(any(InvestmentRound.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        InvestmentRoundCreateRequest updateRequest = InvestmentRoundCreateRequest.builder()
            .notes("Updated notes")
            .maturityDate(LocalDate.now().plusMonths(18))
            .build();

        // When
        InvestmentRoundResponse response = investmentRoundService.updateInvestmentRound("round-1", updateRequest, "admin");

        // Then
        assertThat(response.getNotes()).isEqualTo("Updated notes");
        verify(investmentRoundRepository).save(existingRound);
        verify(auditService).logFinancialTransaction(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when updating closed round")
    void shouldThrowExceptionWhenUpdatingClosedRound() {
        // Given
        InvestmentRound closedRound = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.CLOSED)
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(closedRound));

        InvestmentRoundCreateRequest updateRequest = InvestmentRoundCreateRequest.builder()
            .notes("Updated notes")
            .build();

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
            () -> investmentRoundService.updateInvestmentRound("round-1", updateRequest, "admin"));
    }

    @Test
    @DisplayName("Should delete investment round successfully")
    void shouldDeleteInvestmentRound() {
        // Given
        Investment investment = Investment.builder()
            .id("inv-1")
            .totalProfitEarned(BigDecimal.ZERO)
            .build();

        InvestmentRound round = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .investments(new HashSet<>(List.of(investment)))
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(round));

        // When
        investmentRoundService.deleteInvestmentRound("round-1", "admin");

        // Then
        verify(investmentRoundRepository).delete(round);
        verify(auditService).logFinancialTransaction(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when deleting round with distributions")
    void shouldThrowExceptionWhenDeletingRoundWithDistributions() {
        // Given
        Investment investment = Investment.builder()
            .id("inv-1")
            .totalProfitEarned(BigDecimal.valueOf(1000))
            .build();

        InvestmentRound round = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .investments(new HashSet<>(List.of(investment)))
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(round));

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
            () -> investmentRoundService.deleteInvestmentRound("round-1", "admin"));

        verify(investmentRoundRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should close investment round successfully")
    void shouldCloseRound() {
        // Given
        InvestmentRound openRound = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .status(InvestmentRound.RoundStatus.OPEN)
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(openRound));
        when(investmentRoundRepository.save(any(InvestmentRound.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        InvestmentRoundResponse response = investmentRoundService.closeRound("round-1", "admin");

        // Then
        assertThat(response.getStatus()).isEqualTo(InvestmentRound.RoundStatus.CLOSED);
        verify(investmentRoundRepository).save(openRound);
        verify(auditService).logFinancialTransaction(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw exception when closing non-open round")
    void shouldThrowExceptionWhenClosingNonOpenRound() {
        // Given
        InvestmentRound closedRound = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .status(InvestmentRound.RoundStatus.CLOSED)
            .investments(new HashSet<>())
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(closedRound));

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
            () -> investmentRoundService.closeRound("round-1", "admin"));
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate investor to round")
    void shouldThrowExceptionWhenAddingDuplicateInvestor() {
        // Given
        Investment existingInvestment = Investment.builder()
            .id("inv-1")
            .investor(testInvestor)
            .build();

        InvestmentRound round = InvestmentRound.builder()
            .id("round-1")
            .roundNumber("ROUND-TES-2025-Q1-001")
            .shop(testShop)
            .status(InvestmentRound.RoundStatus.OPEN)
            .investments(new HashSet<>(List.of(existingInvestment)))
            .build();

        when(investmentRoundRepository.findById("round-1")).thenReturn(Optional.of(round));
        when(userRepository.findById("investor-1")).thenReturn(Optional.of(testInvestor));

        InvestmentRoundCreateRequest.InvestorInput duplicateInvestor =
            InvestmentRoundCreateRequest.InvestorInput.builder()
                .investorId("investor-1")
                .amount(BigDecimal.valueOf(10000))
                .build();

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> investmentRoundService.addInvestorToRound("round-1", duplicateInvestor, "admin"));
    }

    @Test
    @DisplayName("Should throw exception when creating round with validation errors")
    void shouldThrowExceptionWhenCreatingRoundWithValidationErrors() {
        // Given
        when(validator.validate(any())).thenReturn(List.of("Total shares must equal 100%", "Invalid tier configuration"));

        InvestmentRoundCreateRequest request = createTestRequest();

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> investmentRoundService.createInvestmentRound(request, "admin"));

        verify(investmentRoundRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when shop not found during round creation")
    void shouldThrowExceptionWhenShopNotFound() {
        // Given
        when(shopRepository.findById("non-existent-shop")).thenReturn(Optional.empty());

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("non-existent-shop")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of())
            .build();

        when(validator.validate(any())).thenReturn(List.of());

        // When/Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> investmentRoundService.createInvestmentRound(request, "admin"));
    }
}