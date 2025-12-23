package com.princely.shopmanager.returns.domain;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductReturn Domain Tests")
class ProductReturnTest {

    private ProductReturn productReturn;
    private Shop testShop;
    private User testUser;
    private SalesTransaction testTransaction;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        Tenant testTenant = Tenant.builder()
            .id("tenant-1")
            .name("Test Tenant")
            .build();

        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .tenant(testTenant)
            .build();

        testUser = User.builder()
            .id("user-1")
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();

        testTransaction = SalesTransaction.builder()
            .id("txn-1")
            .transactionNumber("TXN-001")
            .shop(testShop)
            .build();

        testProduct = Product.builder()
            .id("product-1")
            .name("Test Product")
            .shop(testShop)
            .build();

        productReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .returnType(ProductReturn.ReturnType.FULL)
            .build();
    }

    @Test
    @DisplayName("Should be processable when status is PENDING")
    void shouldBeProcessableWhenPending() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PENDING);

        // When
        boolean result = productReturn.isProcessable();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should be processable when status is APPROVED")
    void shouldBeProcessableWhenApproved() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.APPROVED);

        // When
        boolean result = productReturn.isProcessable();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not be processable when status is REJECTED")
    void shouldNotBeProcessableWhenRejected() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.REJECTED);

        // When
        boolean result = productReturn.isProcessable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not be processable when status is COMPLETED")
    void shouldNotBeProcessableWhenCompleted() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.COMPLETED);

        // When
        boolean result = productReturn.isProcessable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not be processable when status is CANCELLED")
    void shouldNotBeProcessableWhenCancelled() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.CANCELLED);

        // When
        boolean result = productReturn.isProcessable();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should require fraud check when status is PENDING")
    void shouldRequireFraudCheckWhenPending() {
        // Given
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.PENDING);

        // When
        boolean result = productReturn.requiresFraudCheck();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should require fraud check when status is REVIEW_REQUIRED")
    void shouldRequireFraudCheckWhenReviewRequired() {
        // Given
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.REVIEW_REQUIRED);

        // When
        boolean result = productReturn.requiresFraudCheck();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not require fraud check when status is PASSED")
    void shouldNotRequireFraudCheckWhenPassed() {
        // Given
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.PASSED);

        // When
        boolean result = productReturn.requiresFraudCheck();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not require fraud check when status is BYPASSED")
    void shouldNotRequireFraudCheckWhenBypassed() {
        // Given
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.BYPASSED);

        // When
        boolean result = productReturn.requiresFraudCheck();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should be able to process when status is PENDING and fraud check PASSED")
    void shouldBeAbleToProcessWhenPendingAndFraudPassed() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PENDING);
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.PASSED);

        // When
        boolean result = productReturn.canProcess();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should be able to process when status is APPROVED and fraud check BYPASSED")
    void shouldBeAbleToProcessWhenApprovedAndFraudBypassed() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.APPROVED);
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.BYPASSED);

        // When
        boolean result = productReturn.canProcess();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not be able to process when status is PENDING but fraud check PENDING")
    void shouldNotBeAbleToProcessWhenFraudCheckPending() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PENDING);
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.PENDING);

        // When
        boolean result = productReturn.canProcess();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not be able to process when status is REJECTED even if fraud check PASSED")
    void shouldNotBeAbleToProcessWhenRejected() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.REJECTED);
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.PASSED);

        // When
        boolean result = productReturn.canProcess();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should not be able to process when fraud check FAILED")
    void shouldNotBeAbleToProcessWhenFraudCheckFailed() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PENDING);
        productReturn.setFraudCheckStatus(ProductReturn.FraudCheckStatus.FAILED);

        // When
        boolean result = productReturn.canProcess();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should approve return and set processor details")
    void shouldApproveReturnAndSetProcessorDetails() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PENDING);
        LocalDateTime beforeApproval = LocalDateTime.now().minusSeconds(1);

        // When
        productReturn.approve(testUser);

        // Then
        assertThat(productReturn.getStatus()).isEqualTo(ProductReturn.ReturnStatus.APPROVED);
        assertThat(productReturn.getProcessedBy()).isEqualTo(testUser);
        assertThat(productReturn.getProcessedDate()).isNotNull();
        assertThat(productReturn.getProcessedDate()).isAfter(beforeApproval);
    }

    @Test
    @DisplayName("Should reject return and set rejection details")
    void shouldRejectReturnAndSetRejectionDetails() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PENDING);
        String rejectionReason = "Item shows signs of use";
        LocalDateTime beforeRejection = LocalDateTime.now().minusSeconds(1);

        // When
        productReturn.reject(testUser, rejectionReason);

        // Then
        assertThat(productReturn.getStatus()).isEqualTo(ProductReturn.ReturnStatus.REJECTED);
        assertThat(productReturn.getProcessedBy()).isEqualTo(testUser);
        assertThat(productReturn.getProcessedDate()).isNotNull();
        assertThat(productReturn.getProcessedDate()).isAfter(beforeRejection);
        assertThat(productReturn.getInternalNotes()).isEqualTo(rejectionReason);
    }

    @Test
    @DisplayName("Should complete return and set completion date")
    void shouldCompleteReturnAndSetCompletionDate() {
        // Given
        productReturn.setStatus(ProductReturn.ReturnStatus.PROCESSING);
        LocalDateTime beforeCompletion = LocalDateTime.now().minusSeconds(1);

        // When
        productReturn.complete();

        // Then
        assertThat(productReturn.getStatus()).isEqualTo(ProductReturn.ReturnStatus.COMPLETED);
        assertThat(productReturn.getProcessedDate()).isNotNull();
        assertThat(productReturn.getProcessedDate()).isAfter(beforeCompletion);
    }

    @Test
    @DisplayName("Should calculate FULL refund amount correctly")
    void shouldCalculateFullRefundAmount() {
        // Given
        productReturn.setReturnType(ProductReturn.ReturnType.FULL);
        productReturn.setQuantityReturned(3);
        productReturn.setRefundAmount(null);
        BigDecimal unitPrice = BigDecimal.valueOf(100);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(300)); // 3 * 100 = 300
    }

    @Test
    @DisplayName("Should calculate PARTIAL refund amount with 80% rate")
    void shouldCalculatePartialRefundAmount() {
        // Given
        productReturn.setReturnType(ProductReturn.ReturnType.PARTIAL);
        productReturn.setQuantityReturned(5);
        productReturn.setRefundAmount(null);
        BigDecimal unitPrice = BigDecimal.valueOf(50);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        // 5 * 50 * 0.8 = 200
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    @DisplayName("Should calculate DAMAGED refund amount with 50% rate")
    void shouldCalculateDamagedRefundAmount() {
        // Given
        productReturn.setReturnType(ProductReturn.ReturnType.DAMAGED);
        productReturn.setQuantityReturned(4);
        productReturn.setRefundAmount(null);
        BigDecimal unitPrice = BigDecimal.valueOf(80);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        // 4 * 80 * 0.5 = 160
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(160));
    }

    @Test
    @DisplayName("Should calculate EXPIRED refund amount with 30% rate")
    void shouldCalculateExpiredRefundAmount() {
        // Given
        productReturn.setReturnType(ProductReturn.ReturnType.EXPIRED);
        productReturn.setQuantityReturned(10);
        productReturn.setRefundAmount(null);
        BigDecimal unitPrice = BigDecimal.valueOf(20);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        // 10 * 20 * 0.3 = 60
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    @DisplayName("Should return pre-set refund amount when already set")
    void shouldReturnPreSetRefundAmount() {
        // Given
        BigDecimal presetAmount = BigDecimal.valueOf(250);
        productReturn.setRefundAmount(presetAmount);
        productReturn.setReturnType(ProductReturn.ReturnType.FULL);
        productReturn.setQuantityReturned(3);
        BigDecimal unitPrice = BigDecimal.valueOf(100);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        assertThat(refund).isEqualByComparingTo(presetAmount);
    }

    @Test
    @DisplayName("Should return shop ID from shop entity")
    void shouldReturnShopIdFromShopEntity() {
        // Given
        productReturn.setShop(testShop);

        // When
        String shopId = productReturn.getShopId();

        // Then
        assertThat(shopId).isEqualTo("shop-1");
    }

    @Test
    @DisplayName("Should return null when shop is null")
    void shouldReturnNullWhenShopIsNull() {
        // Given
        productReturn.setShop(null);

        // When
        String shopId = productReturn.getShopId();

        // Then
        assertThat(shopId).isNull();
    }

    @Test
    @DisplayName("Should have default status as PENDING")
    void shouldHaveDefaultStatusAsPending() {
        // Given
        ProductReturn newReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .build();

        // Then
        assertThat(newReturn.getStatus()).isEqualTo(ProductReturn.ReturnStatus.PENDING);
    }

    @Test
    @DisplayName("Should have default fraud check status as PENDING")
    void shouldHaveDefaultFraudCheckStatusAsPending() {
        // Given
        ProductReturn newReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .build();

        // Then
        assertThat(newReturn.getFraudCheckStatus()).isEqualTo(ProductReturn.FraudCheckStatus.PENDING);
    }

    @Test
    @DisplayName("Should have default return type as FULL")
    void shouldHaveDefaultReturnTypeAsFull() {
        // Given
        ProductReturn newReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .build();

        // Then
        assertThat(newReturn.getReturnType()).isEqualTo(ProductReturn.ReturnType.FULL);
    }

    @Test
    @DisplayName("Should have default refund type as CASH")
    void shouldHaveDefaultRefundTypeAsCash() {
        // Given
        ProductReturn newReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .build();

        // Then
        assertThat(newReturn.getRefundType()).isEqualTo(ProductReturn.RefundType.CASH);
    }

    @Test
    @DisplayName("Should have default isRestockable as true")
    void shouldHaveDefaultIsRestockableAsTrue() {
        // Given
        ProductReturn newReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .build();

        // Then
        assertThat(newReturn.isRestockable()).isTrue();
    }

    @Test
    @DisplayName("Should have return date set by default")
    void shouldHaveReturnDateSetByDefault() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        ProductReturn newReturn = ProductReturn.builder()
            .shop(testShop)
            .salesTransaction(testTransaction)
            .product(testProduct)
            .quantityReturned(1)
            .returnReason(ProductReturn.ReturnReason.DEFECTIVE)
            .build();

        // Then
        assertThat(newReturn.getReturnDate()).isNotNull();
        assertThat(newReturn.getReturnDate()).isAfter(beforeCreation);
    }

    @Test
    @DisplayName("Should calculate refund for multiple quantities correctly")
    void shouldCalculateRefundForMultipleQuantities() {
        // Given
        productReturn.setReturnType(ProductReturn.ReturnType.DAMAGED);
        productReturn.setQuantityReturned(7);
        productReturn.setRefundAmount(null);
        BigDecimal unitPrice = BigDecimal.valueOf(45.50);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        // 7 * 45.50 * 0.5 = 159.25
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(159.25));
    }

    @Test
    @DisplayName("Should handle decimal unit prices in refund calculation")
    void shouldHandleDecimalUnitPricesInRefundCalculation() {
        // Given
        productReturn.setReturnType(ProductReturn.ReturnType.PARTIAL);
        productReturn.setQuantityReturned(3);
        productReturn.setRefundAmount(null);
        BigDecimal unitPrice = BigDecimal.valueOf(19.99);

        // When
        BigDecimal refund = productReturn.calculateRefundAmount(unitPrice);

        // Then
        // 3 * 19.99 * 0.8 = 47.976
        assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(47.976));
    }
}
