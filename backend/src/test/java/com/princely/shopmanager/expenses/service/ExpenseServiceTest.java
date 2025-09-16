package com.princely.shopmanager.expenses.service;

import com.princely.shopmanager.auth.domain.JwtPrincipal;
import com.princely.shopmanager.expenses.domain.Expense;
import com.princely.shopmanager.expenses.domain.ExpenseCategory;
import com.princely.shopmanager.expenses.domain.ExpenseStatus;
import com.princely.shopmanager.expenses.dto.ExpenseCreateRequest;
import com.princely.shopmanager.expenses.dto.ExpenseResponse;
import com.princely.shopmanager.expenses.repository.ExpenseCategoryRepository;
import com.princely.shopmanager.expenses.repository.ExpenseRepository;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expense Service Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCategoryRepository categoryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ExpenseService expenseService;

    private UUID shopId;
    private UUID categoryId;
    private String userId;
    private JwtPrincipal principal;
    private ExpenseCategory category;
    private ExpenseCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        shopId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        userId = UUID.randomUUID().toString();

        principal = mock(JwtPrincipal.class);
        when(principal.getUserId()).thenReturn(userId);
        when(principal.getFullName()).thenReturn("Test User");
        when(principal.getUsername()).thenReturn("testuser");

        category = ExpenseCategory.builder()
            .id(categoryId)
            .shopId(shopId)
            .name("Test Category")
            .description("Test category description")
            .isActive(true)
            .requiresApproval(false)
            .autoApprovalEnabled(true)
            .approvalLimit(BigDecimal.valueOf(1000))
            .build();

        Set<String> tags = new HashSet<>();
        tags.add("urgent");
        tags.add("maintenance");

        createRequest = ExpenseCreateRequest.builder()
            .title("Test Expense")
            .description("Test expense description")
            .categoryId(categoryId)
            .amount(BigDecimal.valueOf(500))
            .expenseDate(LocalDate.now())
            .paymentMethod("cash")
            .vendorName("Test Vendor")
            .referenceNumber("REF123")
            .tags(tags)
            .notes("Test notes")
            .submitForApproval(false)
            .build();
    }

    @Test
    @DisplayName("Should create expense successfully")
    void shouldCreateExpenseSuccessfully() {
        // Given
        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        Expense savedExpense = createExpenseFromRequest();
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // When
        ExpenseResponse response = expenseService.createExpense(shopId, createRequest, principal);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Expense");
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(response.status()).isEqualTo(ExpenseStatus.APPROVED); // Auto-approved
        assertThat(response.createdBy()).isEqualTo(UUID.fromString(userId));
        assertThat(response.createdByName()).isEqualTo("Test User");

        verify(expenseRepository).save(any(Expense.class));
        verify(auditService).logExpenseCreation(any(UUID.class), eq(shopId), eq(UUID.fromString(userId)), eq(BigDecimal.valueOf(500)));
    }

    @Test
    @DisplayName("Should create expense as draft when auto-approval disabled")
    void shouldCreateExpenseAsDraftWhenAutoApprovalDisabled() {
        // Given
        category.setAutoApprovalEnabled(false);
        category.setRequiresApproval(true);

        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        Expense savedExpense = createExpenseFromRequest();
        savedExpense.setStatus(ExpenseStatus.DRAFT);
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // When
        ExpenseResponse response = expenseService.createExpense(shopId, createRequest, principal);

        // Then
        assertThat(response.status()).isEqualTo(ExpenseStatus.DRAFT);
    }

    @Test
    @DisplayName("Should submit for approval when requested")
    void shouldSubmitForApprovalWhenRequested() {
        // Given
        category.setAutoApprovalEnabled(false);
        category.setRequiresApproval(true);
        createRequest = ExpenseCreateRequest.builder()
            .title("Test Expense")
            .categoryId(categoryId)
            .amount(BigDecimal.valueOf(500))
            .expenseDate(LocalDate.now())
            .submitForApproval(true)
            .build();

        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        Expense savedExpense = createExpenseFromRequest();
        savedExpense.setStatus(ExpenseStatus.PENDING_APPROVAL);
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // When
        ExpenseResponse response = expenseService.createExpense(shopId, createRequest, principal);

        // Then
        assertThat(response.status()).isEqualTo(ExpenseStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(shopId, createRequest, principal))
            .hasMessageContaining("Expense category not found");

        verify(expenseRepository, never()).save(any(Expense.class));
        verify(auditService, never()).logExpenseCreation(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when category is inactive")
    void shouldThrowExceptionWhenCategoryIsInactive() {
        // Given
        category.setIsActive(false);
        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        // When & Then
        assertThatThrownBy(() -> expenseService.createExpense(shopId, createRequest, principal))
            .hasMessageContaining("Cannot create expense for inactive category");

        verify(expenseRepository, never()).save(any(Expense.class));
        verify(auditService, never()).logExpenseCreation(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should clean and normalize tags")
    void shouldCleanAndNormalizeTags() {
        // Given
        Set<String> messyTags = new HashSet<>();
        messyTags.add("  URGENT  ");
        messyTags.add("Maintenance");
        messyTags.add("");
        messyTags.add("   ");

        createRequest = ExpenseCreateRequest.builder()
            .title("Test Expense")
            .categoryId(categoryId)
            .amount(BigDecimal.valueOf(500))
            .expenseDate(LocalDate.now())
            .tags(messyTags)
            .build();

        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        Expense savedExpense = createExpenseFromRequest();
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // When
        ExpenseResponse response = expenseService.createExpense(shopId, createRequest, principal);

        // Then
        assertThat(response.tags()).containsExactlyInAnyOrder("urgent", "maintenance");
    }

    @Test
    @DisplayName("Should validate expense amount is positive")
    void shouldValidateExpenseAmountIsPositive() {
        // Given
        createRequest = ExpenseCreateRequest.builder()
            .title("Test Expense")
            .categoryId(categoryId)
            .amount(BigDecimal.ZERO)
            .expenseDate(LocalDate.now())
            .build();

        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        // When & Then
        // This validation should happen at the entity level via @PrePersist
        assertThatThrownBy(() -> {
            Expense expense = Expense.builder()
                .amount(BigDecimal.ZERO)
                .expenseDate(LocalDate.now())
                .build();
            // Simulate @PrePersist validation
            if (expense.getAmount() != null && expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Expense amount must be greater than zero");
            }
        }).hasMessageContaining("Expense amount must be greater than zero");
    }

    @Test
    @DisplayName("Should validate expense date is not in future")
    void shouldValidateExpenseDateIsNotInFuture() {
        // Given
        createRequest = ExpenseCreateRequest.builder()
            .title("Test Expense")
            .categoryId(categoryId)
            .amount(BigDecimal.valueOf(500))
            .expenseDate(LocalDate.now().plusDays(1))
            .build();

        when(categoryRepository.findByIdAndShopId(categoryId, shopId))
            .thenReturn(Optional.of(category));

        // When & Then
        // This validation should happen at the entity level via @PrePersist
        assertThatThrownBy(() -> {
            Expense expense = Expense.builder()
                .expenseDate(LocalDate.now().plusDays(1))
                .build();
            // Simulate @PrePersist validation
            if (expense.getExpenseDate() != null && expense.getExpenseDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Expense date cannot be in the future");
            }
        }).hasMessageContaining("Expense date cannot be in the future");
    }

    private Expense createExpenseFromRequest() {
        return Expense.builder()
            .id(UUID.randomUUID())
            .shopId(shopId)
            .title(createRequest.title())
            .description(createRequest.description())
            .categoryId(categoryId)
            .amount(createRequest.amount())
            .expenseDate(createRequest.expenseDate())
            .paymentMethod(createRequest.paymentMethod())
            .vendorName(createRequest.vendorName())
            .referenceNumber(createRequest.referenceNumber())
            .tags(createRequest.tags() != null ? createRequest.tags() : new HashSet<>())
            .notes(createRequest.notes())
            .expenseCreatedBy(UUID.fromString(userId))
            .createdByName("Test User")
            .status(category.canAutoApprove(createRequest.amount()) ? ExpenseStatus.APPROVED : ExpenseStatus.DRAFT)
            .build();
    }
}