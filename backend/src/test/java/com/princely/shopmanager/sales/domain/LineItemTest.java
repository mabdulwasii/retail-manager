package com.princely.shopmanager.sales.domain;

import com.princely.shopmanager.core.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LineItem Domain Tests")
class LineItemTest {

    private LineItem lineItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
            .id("product-1")
            .name("Test Product")
            .sku("SKU-001")
            .build();

        lineItem = LineItem.builder()
            .product(testProduct)
            .productName("Test Product")
            .productSku("SKU-001")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(100))
            .build();
    }

    @Test
    @DisplayName("Should have default discountPercentage as zero")
    void shouldHaveDefaultDiscountPercentageAsZero() {
        // Given
        LineItem newLineItem = LineItem.builder()
            .product(testProduct)
            .productName("Test Product")
            .productSku("SKU-001")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(100))
            .build();

        // Then
        assertThat(newLineItem.getDiscountPercentage()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should have default discountAmount as zero")
    void shouldHaveDefaultDiscountAmountAsZero() {
        // Given
        LineItem newLineItem = LineItem.builder()
            .product(testProduct)
            .productName("Test Product")
            .productSku("SKU-001")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(100))
            .build();

        // Then
        assertThat(newLineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should have default taxAmount as zero")
    void shouldHaveDefaultTaxAmountAsZero() {
        // Given
        LineItem newLineItem = LineItem.builder()
            .product(testProduct)
            .productName("Test Product")
            .productSku("SKU-001")
            .quantity(1)
            .unitPrice(BigDecimal.valueOf(100))
            .build();

        // Then
        assertThat(newLineItem.getTaxAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // calculateLineTotal tests - basic calculation
    @Test
    @DisplayName("calculateLineTotal - Should calculate line total without discount or tax")
    void calculateLineTotal_shouldCalculateLineTotalWithoutDiscountOrTax() {
        // Given
        lineItem.setQuantity(2);
        lineItem.setUnitPrice(BigDecimal.valueOf(50));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // 2 * 50 = 100
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("calculateLineTotal - Should calculate line total for quantity 1")
    void calculateLineTotal_shouldCalculateLineTotalForQuantityOne() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(75.50));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(75.50));
    }

    @Test
    @DisplayName("calculateLineTotal - Should calculate line total for large quantity")
    void calculateLineTotal_shouldCalculateLineTotalForLargeQuantity() {
        // Given
        lineItem.setQuantity(100);
        lineItem.setUnitPrice(BigDecimal.valueOf(10));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // 100 * 10 = 1000
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    // calculateLineTotal with discount percentage
    @Test
    @DisplayName("calculateLineTotal - Should apply discount percentage")
    void calculateLineTotal_shouldApplyDiscountPercentage() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(10)); // 10% discount
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 1 * 100 = 100
        // discount = 100 * 10 / 100 = 10
        // lineTotal = 100 - 10 + 0 = 90
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(90));
    }

    @Test
    @DisplayName("calculateLineTotal - Should apply 50% discount percentage")
    void calculateLineTotal_shouldApply50PercentDiscount() {
        // Given
        lineItem.setQuantity(2);
        lineItem.setUnitPrice(BigDecimal.valueOf(80));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(50)); // 50% discount
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 2 * 80 = 160
        // discount = 160 * 50 / 100 = 80
        // lineTotal = 160 - 80 + 0 = 80
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    @DisplayName("calculateLineTotal - Should apply 100% discount percentage")
    void calculateLineTotal_shouldApply100PercentDiscount() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(100)); // 100% discount (free)
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 1 * 100 = 100
        // discount = 100 * 100 / 100 = 100
        // lineTotal = 100 - 100 + 0 = 0
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateLineTotal - Should not apply discount when percentage is zero")
    void calculateLineTotal_shouldNotApplyDiscountWhenPercentageIsZero() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("calculateLineTotal - Should not apply discount when percentage is null")
    void calculateLineTotal_shouldNotApplyDiscountWhenPercentageIsNull() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(null);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    // calculateLineTotal with tax
    @Test
    @DisplayName("calculateLineTotal - Should add tax amount")
    void calculateLineTotal_shouldAddTaxAmount() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.valueOf(15));

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 1 * 100 = 100
        // lineTotal = 100 - 0 + 15 = 115
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(115));
    }

    @Test
    @DisplayName("calculateLineTotal - Should handle zero tax")
    void calculateLineTotal_shouldHandleZeroTax() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    // calculateLineTotal with both discount and tax
    @Test
    @DisplayName("calculateLineTotal - Should apply both discount percentage and tax")
    void calculateLineTotal_shouldApplyBothDiscountAndTax() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(20)); // 20% discount
        lineItem.setTaxAmount(BigDecimal.valueOf(12));

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 1 * 100 = 100
        // discount = 100 * 20 / 100 = 20
        // lineTotal = 100 - 20 + 12 = 92
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(92));
    }

    @Test
    @DisplayName("calculateLineTotal - Should apply discount and tax with multiple quantity")
    void calculateLineTotal_shouldApplyDiscountAndTaxWithMultipleQuantity() {
        // Given
        lineItem.setQuantity(5);
        lineItem.setUnitPrice(BigDecimal.valueOf(40));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(10)); // 10% discount
        lineItem.setTaxAmount(BigDecimal.valueOf(20));

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 5 * 40 = 200
        // discount = 200 * 10 / 100 = 20
        // lineTotal = 200 - 20 + 20 = 200
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    // Decimal and edge cases
    @Test
    @DisplayName("calculateLineTotal - Should handle decimal unit prices")
    void calculateLineTotal_shouldHandleDecimalUnitPrices() {
        // Given
        lineItem.setQuantity(3);
        lineItem.setUnitPrice(BigDecimal.valueOf(19.99));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // 3 * 19.99 = 59.97
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(59.97));
    }

    @Test
    @DisplayName("calculateLineTotal - Should handle decimal discount percentage")
    void calculateLineTotal_shouldHandleDecimalDiscountPercentage() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(7.5)); // 7.5% discount
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 1 * 100 = 100
        // discount = 100 * 7.5 / 100 = 7.5
        // lineTotal = 100 - 7.5 + 0 = 92.5
        assertThat(lineItem.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(7.5));
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(92.5));
    }

    @Test
    @DisplayName("calculateLineTotal - Should handle decimal tax amount")
    void calculateLineTotal_shouldHandleDecimalTaxAmount() {
        // Given
        lineItem.setQuantity(1);
        lineItem.setUnitPrice(BigDecimal.valueOf(100));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.valueOf(8.75));

        // When
        lineItem.calculateLineTotal();

        // Then
        // lineTotal = 100 - 0 + 8.75 = 108.75
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(108.75));
    }

    @Test
    @DisplayName("calculateLineTotal - Should handle complex decimal calculation")
    void calculateLineTotal_shouldHandleComplexDecimalCalculation() {
        // Given
        lineItem.setQuantity(7);
        lineItem.setUnitPrice(BigDecimal.valueOf(12.99));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(15.5)); // 15.5% discount
        lineItem.setTaxAmount(BigDecimal.valueOf(6.87));

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 7 * 12.99 = 90.93
        // discount = 90.93 * 15.5 / 100 = 14.09415 (BigDecimal precision)
        // lineTotal = 90.93 - 14.09415 + 6.87 = 83.70585
        BigDecimal expectedLineTotal = new BigDecimal("83.70585");
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(expectedLineTotal);
    }

    @Test
    @DisplayName("calculateLineTotal - Should handle large quantities")
    void calculateLineTotal_shouldHandleLargeQuantities() {
        // Given
        lineItem.setQuantity(1000);
        lineItem.setUnitPrice(BigDecimal.valueOf(5.50));
        lineItem.setDiscountPercentage(BigDecimal.valueOf(5));
        lineItem.setTaxAmount(BigDecimal.valueOf(300));

        // When
        lineItem.calculateLineTotal();

        // Then
        // subtotal = 1000 * 5.50 = 5500
        // discount = 5500 * 5 / 100 = 275
        // lineTotal = 5500 - 275 + 300 = 5525
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(5525));
    }

    @Test
    @DisplayName("calculateLineTotal - Should handle very small unit price")
    void calculateLineTotal_shouldHandleVerySmallUnitPrice() {
        // Given
        lineItem.setQuantity(10);
        lineItem.setUnitPrice(BigDecimal.valueOf(0.01));
        lineItem.setDiscountPercentage(BigDecimal.ZERO);
        lineItem.setDiscountAmount(BigDecimal.ZERO);
        lineItem.setTaxAmount(BigDecimal.ZERO);

        // When
        lineItem.calculateLineTotal();

        // Then
        // 10 * 0.01 = 0.10
        assertThat(lineItem.getLineTotal()).isEqualByComparingTo(BigDecimal.valueOf(0.10));
    }

    @Test
    @DisplayName("Should preserve product information")
    void shouldPreserveProductInformation() {
        // Given
        lineItem.setProductName("Custom Product Name");
        lineItem.setProductSku("CUSTOM-SKU-123");
        lineItem.setProductCategory("Electronics");

        // Then
        assertThat(lineItem.getProductName()).isEqualTo("Custom Product Name");
        assertThat(lineItem.getProductSku()).isEqualTo("CUSTOM-SKU-123");
        assertThat(lineItem.getProductCategory()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should preserve notes")
    void shouldPreserveNotes() {
        // Given
        lineItem.setNotes("Gift wrapped, special handling required");

        // Then
        assertThat(lineItem.getNotes()).isEqualTo("Gift wrapped, special handling required");
    }
}
