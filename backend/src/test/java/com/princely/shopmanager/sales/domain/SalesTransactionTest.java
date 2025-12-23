package com.princely.shopmanager.sales.domain;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SalesTransaction Domain Tests")
class SalesTransactionTest {

    private SalesTransaction transaction;
    private Shop testShop;
    private User testCashier;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        testCashier = User.builder()
            .id("user-1")
            .username("cashier")
            .build();

        testProduct = Product.builder()
            .id("product-1")
            .name("Test Product")
            .sku("SKU-001")
            .build();

        transaction = SalesTransaction.builder()
            .transactionNumber("TXN-001")
            .shop(testShop)
            .cashier(testCashier)
            .transactionDate(LocalDateTime.now())
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();
    }

    @Test
    @DisplayName("Should have default status as PENDING")
    void shouldHaveDefaultStatusAsPending() {
        // Given
        SalesTransaction newTransaction = SalesTransaction.builder()
            .transactionNumber("TXN-002")
            .shop(testShop)
            .cashier(testCashier)
            .transactionDate(LocalDateTime.now())
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // Then
        assertThat(newTransaction.getStatus()).isEqualTo(SalesTransaction.TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("Should have default isVoided as false")
    void shouldHaveDefaultIsVoidedAsFalse() {
        // Given
        SalesTransaction newTransaction = SalesTransaction.builder()
            .transactionNumber("TXN-002")
            .shop(testShop)
            .cashier(testCashier)
            .transactionDate(LocalDateTime.now())
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // Then
        assertThat(newTransaction.isVoided()).isFalse();
    }

    @Test
    @DisplayName("Should have default requiresReview as false")
    void shouldHaveDefaultRequiresReviewAsFalse() {
        // Given
        SalesTransaction newTransaction = SalesTransaction.builder()
            .transactionNumber("TXN-002")
            .shop(testShop)
            .cashier(testCashier)
            .transactionDate(LocalDateTime.now())
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // Then
        assertThat(newTransaction.isRequiresReview()).isFalse();
    }

    @Test
    @DisplayName("Should initialize lineItems as empty list")
    void shouldInitializeLineItemsAsEmptyList() {
        // Then
        assertThat(transaction.getLineItems()).isNotNull();
        assertThat(transaction.getLineItems()).isEmpty();
    }

    // addLineItem tests
    @Test
    @DisplayName("addLineItem - Should add line item and set transaction reference")
    void addLineItem_shouldAddLineItemAndSetTransactionReference() {
        // Given
        LineItem lineItem = LineItem.builder()
            .product(testProduct)
            .productName("Test Product")
            .productSku("SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(50))
            .lineTotal(BigDecimal.valueOf(100))
            .build();

        // When
        transaction.addLineItem(lineItem);

        // Then
        assertThat(transaction.getLineItems()).hasSize(1);
        assertThat(transaction.getLineItems()).contains(lineItem);
        assertThat(lineItem.getTransaction()).isEqualTo(transaction);
    }

    @Test
    @DisplayName("addLineItem - Should recalculate totals after adding line item")
    void addLineItem_shouldRecalculateTotalsAfterAdding() {
        // Given
        LineItem lineItem1 = LineItem.builder()
            .product(testProduct)
            .productName("Product 1")
            .productSku("SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(50))
            .lineTotal(BigDecimal.valueOf(100))
            .build();

        LineItem lineItem2 = LineItem.builder()
            .product(testProduct)
            .productName("Product 2")
            .productSku("SKU-002")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(75))
            .lineTotal(BigDecimal.valueOf(75))
            .build();

        // When
        transaction.addLineItem(lineItem1);
        transaction.addLineItem(lineItem2);

        // Then
        assertThat(transaction.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(175));
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(175));
    }

    // removeLineItem tests
    @Test
    @DisplayName("removeLineItem - Should remove line item and clear transaction reference")
    void removeLineItem_shouldRemoveLineItemAndClearTransactionReference() {
        // Given
        LineItem lineItem = LineItem.builder()
            .product(testProduct)
            .productName("Test Product")
            .productSku("SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(50))
            .lineTotal(BigDecimal.valueOf(100))
            .build();

        transaction.addLineItem(lineItem);

        // When
        transaction.removeLineItem(lineItem);

        // Then
        assertThat(transaction.getLineItems()).isEmpty();
        assertThat(lineItem.getTransaction()).isNull();
    }

    @Test
    @DisplayName("removeLineItem - Should recalculate totals after removing line item")
    void removeLineItem_shouldRecalculateTotalsAfterRemoving() {
        // Given
        LineItem lineItem1 = LineItem.builder()
            .product(testProduct)
            .productName("Product 1")
            .productSku("SKU-001")
            .quantity(2)
            .unitPrice(BigDecimal.valueOf(50))
            .lineTotal(BigDecimal.valueOf(100))
            .build();

        LineItem lineItem2 = LineItem.builder()
            .product(testProduct)
            .productName("Product 2")
            .productSku("SKU-002")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(75))
            .lineTotal(BigDecimal.valueOf(75))
            .build();

        transaction.addLineItem(lineItem1);
        transaction.addLineItem(lineItem2);

        // When
        transaction.removeLineItem(lineItem1);

        // Then
        assertThat(transaction.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(75));
    }

    // recalculateTotals tests
    @Test
    @DisplayName("recalculateTotals - Should calculate subtotal from line items")
    void recalculateTotals_shouldCalculateSubtotalFromLineItems() {
        // Given
        LineItem lineItem1 = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();

        LineItem lineItem2 = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(50.50))
            .build();

        transaction.getLineItems().add(lineItem1);
        transaction.getLineItems().add(lineItem2);

        // When
        transaction.recalculateTotals();

        // Then
        assertThat(transaction.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(150.50));
    }

    @Test
    @DisplayName("recalculateTotals - Should set zero subtotal when no line items")
    void recalculateTotals_shouldSetZeroSubtotalWhenNoLineItems() {
        // When
        transaction.recalculateTotals();

        // Then
        assertThat(transaction.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("recalculateTotals - Should initialize taxAmount to zero if null")
    void recalculateTotals_shouldInitializeTaxAmountToZeroIfNull() {
        // Given
        transaction.setTaxAmount(null);
        LineItem lineItem = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();
        transaction.getLineItems().add(lineItem);

        // When
        transaction.recalculateTotals();

        // Then
        assertThat(transaction.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("recalculateTotals - Should initialize discountAmount to zero if null")
    void recalculateTotals_shouldInitializeDiscountAmountToZeroIfNull() {
        // Given
        transaction.setDiscountAmount(null);
        LineItem lineItem = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();
        transaction.getLineItems().add(lineItem);

        // When
        transaction.recalculateTotals();

        // Then
        assertThat(transaction.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("recalculateTotals - Should calculate total with tax")
    void recalculateTotals_shouldCalculateTotalWithTax() {
        // Given
        LineItem lineItem = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();
        transaction.getLineItems().add(lineItem);
        transaction.setTaxAmount(BigDecimal.valueOf(10));
        transaction.setDiscountAmount(BigDecimal.ZERO);

        // When
        transaction.recalculateTotals();

        // Then
        // subtotal (100) + tax (10) - discount (0) = 110
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(110));
    }

    @Test
    @DisplayName("recalculateTotals - Should calculate total with discount")
    void recalculateTotals_shouldCalculateTotalWithDiscount() {
        // Given
        LineItem lineItem = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();
        transaction.getLineItems().add(lineItem);
        transaction.setTaxAmount(BigDecimal.ZERO);
        transaction.setDiscountAmount(BigDecimal.valueOf(15));

        // When
        transaction.recalculateTotals();

        // Then
        // subtotal (100) + tax (0) - discount (15) = 85
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(85));
    }

    @Test
    @DisplayName("recalculateTotals - Should calculate total with both tax and discount")
    void recalculateTotals_shouldCalculateTotalWithTaxAndDiscount() {
        // Given
        LineItem lineItem1 = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();
        LineItem lineItem2 = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(50))
            .build();
        transaction.getLineItems().add(lineItem1);
        transaction.getLineItems().add(lineItem2);
        transaction.setTaxAmount(BigDecimal.valueOf(12));
        transaction.setDiscountAmount(BigDecimal.valueOf(20));

        // When
        transaction.recalculateTotals();

        // Then
        // subtotal (150) + tax (12) - discount (20) = 142
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(142));
    }

    @Test
    @DisplayName("recalculateTotals - Should handle decimal amounts correctly")
    void recalculateTotals_shouldHandleDecimalAmountsCorrectly() {
        // Given
        LineItem lineItem = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(99.99))
            .build();
        transaction.getLineItems().add(lineItem);
        transaction.setTaxAmount(BigDecimal.valueOf(7.50));
        transaction.setDiscountAmount(BigDecimal.valueOf(5.25));

        // When
        transaction.recalculateTotals();

        // Then
        // subtotal (99.99) + tax (7.50) - discount (5.25) = 102.24
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(102.24));
    }

    // ShopAware implementation tests
    @Test
    @DisplayName("getShopId - Should return shop ID when shop is set")
    void getShopId_shouldReturnShopIdWhenShopIsSet() {
        // Given
        transaction.setShop(testShop);

        // When
        String shopId = transaction.getShopId();

        // Then
        assertThat(shopId).isEqualTo("shop-1");
    }

    @Test
    @DisplayName("getShopId - Should return null when shop is null")
    void getShopId_shouldReturnNullWhenShopIsNull() {
        // Given
        transaction.setShop(null);

        // When
        String shopId = transaction.getShopId();

        // Then
        assertThat(shopId).isNull();
    }

    // PaymentMethod enum tests
    @Test
    @DisplayName("PaymentMethod - All enum values should exist")
    void paymentMethod_allEnumValuesShouldExist() {
        SalesTransaction.PaymentMethod[] methods = SalesTransaction.PaymentMethod.values();

        assertThat(methods).hasSize(6);
        assertThat(methods).contains(
            SalesTransaction.PaymentMethod.CASH,
            SalesTransaction.PaymentMethod.CARD,
            SalesTransaction.PaymentMethod.BANK_TRANSFER,
            SalesTransaction.PaymentMethod.MOBILE_MONEY,
            SalesTransaction.PaymentMethod.CREDIT,
            SalesTransaction.PaymentMethod.MIXED
        );
    }

    // TransactionStatus enum tests
    @Test
    @DisplayName("TransactionStatus - All enum values should exist")
    void transactionStatus_allEnumValuesShouldExist() {
        SalesTransaction.TransactionStatus[] statuses = SalesTransaction.TransactionStatus.values();

        assertThat(statuses).hasSize(5);
        assertThat(statuses).contains(
            SalesTransaction.TransactionStatus.PENDING,
            SalesTransaction.TransactionStatus.COMPLETED,
            SalesTransaction.TransactionStatus.CANCELLED,
            SalesTransaction.TransactionStatus.REFUNDED,
            SalesTransaction.TransactionStatus.PARTIALLY_REFUNDED
        );
    }

    // Edge cases
    @Test
    @DisplayName("Should handle multiple addLineItem calls correctly")
    void shouldHandleMultipleAddLineItemCallsCorrectly() {
        // Given
        LineItem item1 = LineItem.builder().lineTotal(BigDecimal.valueOf(10)).build();
        LineItem item2 = LineItem.builder().lineTotal(BigDecimal.valueOf(20)).build();
        LineItem item3 = LineItem.builder().lineTotal(BigDecimal.valueOf(30)).build();

        // When
        transaction.addLineItem(item1);
        transaction.addLineItem(item2);
        transaction.addLineItem(item3);

        // Then
        assertThat(transaction.getLineItems()).hasSize(3);
        assertThat(transaction.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    @DisplayName("Should handle add and remove operations in sequence")
    void shouldHandleAddAndRemoveOperationsInSequence() {
        // Given
        LineItem item1 = LineItem.builder().lineTotal(BigDecimal.valueOf(10)).build();
        LineItem item2 = LineItem.builder().lineTotal(BigDecimal.valueOf(20)).build();
        LineItem item3 = LineItem.builder().lineTotal(BigDecimal.valueOf(30)).build();

        // When
        transaction.addLineItem(item1);
        transaction.addLineItem(item2);
        transaction.addLineItem(item3);
        transaction.removeLineItem(item2);

        // Then
        assertThat(transaction.getLineItems()).hasSize(2);
        assertThat(transaction.getLineItems()).containsExactly(item1, item3);
        assertThat(transaction.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(40));
    }

    @Test
    @DisplayName("Should handle negative discount amounts")
    void shouldHandleNegativeDiscountAmounts() {
        // Given
        LineItem lineItem = LineItem.builder()
            .lineTotal(BigDecimal.valueOf(100))
            .build();
        transaction.getLineItems().add(lineItem);
        transaction.setTaxAmount(BigDecimal.ZERO);
        transaction.setDiscountAmount(BigDecimal.valueOf(-10)); // Negative discount (adds to total)

        // When
        transaction.recalculateTotals();

        // Then
        // subtotal (100) + tax (0) - discount (-10) = 110
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(110));
    }

    @Test
    @DisplayName("Should handle large amounts correctly")
    void shouldHandleLargeAmountsCorrectly() {
        // Given
        LineItem lineItem = LineItem.builder()
            .lineTotal(new BigDecimal("99999999.99"))
            .build();
        transaction.getLineItems().add(lineItem);
        transaction.setTaxAmount(new BigDecimal("10000.00"));
        transaction.setDiscountAmount(new BigDecimal("9999.99"));

        // When
        transaction.recalculateTotals();

        // Then
        // subtotal (99999999.99) + tax (10000.00) - discount (9999.99) = 100000000.00
        assertThat(transaction.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100000000.00"));
    }

    @Test
    @DisplayName("Should preserve fraud detection fields")
    void shouldPreserveFraudDetectionFields() {
        // Given
        transaction.setFraudScore(BigDecimal.valueOf(75.50));
        transaction.setRiskLevel("HIGH");
        transaction.setRequiresReview(true);
        transaction.setFraudFlags("HIGH_AMOUNT,SUSPICIOUS_PATTERN");

        // Then
        assertThat(transaction.getFraudScore()).isEqualByComparingTo(BigDecimal.valueOf(75.50));
        assertThat(transaction.getRiskLevel()).isEqualTo("HIGH");
        assertThat(transaction.isRequiresReview()).isTrue();
        assertThat(transaction.getFraudFlags()).isEqualTo("HIGH_AMOUNT,SUSPICIOUS_PATTERN");
    }

    @Test
    @DisplayName("Should preserve void information")
    void shouldPreserveVoidInformation() {
        // Given
        LocalDateTime voidTime = LocalDateTime.now();
        transaction.setVoided(true);
        transaction.setVoidReason("Customer requested refund");
        transaction.setVoidedBy("admin-user");
        transaction.setVoidedAt(voidTime);

        // Then
        assertThat(transaction.isVoided()).isTrue();
        assertThat(transaction.getVoidReason()).isEqualTo("Customer requested refund");
        assertThat(transaction.getVoidedBy()).isEqualTo("admin-user");
        assertThat(transaction.getVoidedAt()).isEqualTo(voidTime);
    }
}
