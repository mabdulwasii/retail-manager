package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.Receipt;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.ReceiptRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;

    @Mock
    private SalesTransactionService salesTransactionService;

    @InjectMocks
    private ReceiptService receiptService;

    private SalesTransaction testTransaction;
    private Receipt testReceipt;
    private Shop testShop;
    private User testCashier;
    private Product testProduct1;
    private Product testProduct2;
    private LineItem testLineItem1;
    private LineItem testLineItem2;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");
        testShop.setAddress("123 Test St");
        testShop.setPhoneNumber("555-0123");
        testShop.setEmail("shop@test.com");

        testCashier = new User();
        testCashier.setId("user-1");
        testCashier.setUsername("testcashier");

        testProduct1 = new Product();
        testProduct1.setId("product-1");
        testProduct1.setName("Test Product 1");

        testProduct2 = new Product();
        testProduct2.setId("product-2");
        testProduct2.setName("Very Long Product Name That Exceeds Twenty Characters");

        testLineItem1 = new LineItem();
        testLineItem1.setId("item-1");
        testLineItem1.setProduct(testProduct1);
        testLineItem1.setQuantity(2);
        testLineItem1.setUnitPrice(BigDecimal.valueOf(10.00));
        testLineItem1.setLineTotal(BigDecimal.valueOf(20.00));

        testLineItem2 = new LineItem();
        testLineItem2.setId("item-2");
        testLineItem2.setProduct(testProduct2);
        testLineItem2.setQuantity(1);
        testLineItem2.setUnitPrice(BigDecimal.valueOf(15.50));
        testLineItem2.setLineTotal(BigDecimal.valueOf(15.50));

        testTransaction = new SalesTransaction();
        testTransaction.setId("txn-1");
        testTransaction.setTransactionNumber("TXN001");
        testTransaction.setShop(testShop);
        testTransaction.setCashier(testCashier);
        testTransaction.setTransactionDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        testTransaction.setCustomerName("John Doe");
        testTransaction.setCustomerPhone("555-9876");
        testTransaction.setLineItems(Arrays.asList(testLineItem1, testLineItem2));
        testTransaction.setSubtotal(BigDecimal.valueOf(35.50));
        testTransaction.setTaxAmount(BigDecimal.valueOf(3.55));
        testTransaction.setDiscountAmount(BigDecimal.valueOf(1.00));
        testTransaction.setTotalAmount(BigDecimal.valueOf(38.05));
        testTransaction.setPaymentMethod(SalesTransaction.PaymentMethod.CASH);
        testTransaction.setPaymentReference("CASH-001");

        testReceipt = new Receipt();
        testReceipt.setId("receipt-1");
        testReceipt.setTransaction(testTransaction);
        testReceipt.setReceiptNumber("RCP-TXN001-20240115103000");
        testReceipt.setFormat(Receipt.ReceiptFormat.TEXT);
        testReceipt.setStatus(Receipt.ReceiptStatus.GENERATED);
        testReceipt.setGeneratedAt(LocalDateTime.now());
    }

    @Test
    void generateReceipt_WithNewTransaction_ShouldCreateNewReceipt() {
        // Arrange
        when(receiptRepository.findByTransaction(testTransaction)).thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);
        when(salesTransactionService.getTransactionById(any())).thenReturn(testTransaction);

        // Act
        Receipt result = receiptService.generateReceipt(testTransaction.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTransaction()).isEqualTo(testTransaction);

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(receiptCaptor.capture());
        Receipt savedReceipt = receiptCaptor.getValue();

        assertThat(savedReceipt.getReceiptNumber()).startsWith("RCP-TXN001-");
        assertThat(savedReceipt.getFormat()).isEqualTo(Receipt.ReceiptFormat.TEXT);
        assertThat(savedReceipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.GENERATED);
        assertThat(savedReceipt.getReceiptContent()).contains("Test Shop");
        assertThat(savedReceipt.getReceiptContent()).contains("Test Product 1");
        assertThat(savedReceipt.getReceiptContent()).contains("John Doe");
        assertThat(savedReceipt.getReceiptContent()).contains("38.05");
        assertThat(savedReceipt.getPrintableContent()).contains("SALES RECEIPT");
    }

    @Test
    void generateReceipt_WithExistingReceipt_ShouldReturnExisting() {
        // Arrange
        when(receiptRepository.findByTransaction(testTransaction)).thenReturn(Optional.of(testReceipt));
        when(salesTransactionService.getTransactionById(any())).thenReturn(testTransaction);

        // Act
        Receipt result = receiptService.generateReceipt(testTransaction.getId());

        // Assert
        assertThat(result).isEqualTo(testReceipt);
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void generateReceipt_WithMinimalTransaction_ShouldCreateReceipt() {
        // Arrange - transaction without customer details, tax, discount
        testTransaction.setCustomerName(null);
        testTransaction.setCustomerPhone(null);
        testTransaction.setTaxAmount(BigDecimal.ZERO);
        testTransaction.setDiscountAmount(null);
        testTransaction.setPaymentReference(null);

        when(receiptRepository.findByTransaction(testTransaction)).thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);
        when(salesTransactionService.getTransactionById(any())).thenReturn(testTransaction);

        // Act
        Receipt result = receiptService.generateReceipt(testTransaction.getId());

        // Assert
        assertThat(result).isNotNull();

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(receiptCaptor.capture());
        Receipt savedReceipt = receiptCaptor.getValue();

        assertThat(savedReceipt.getReceiptContent()).doesNotContain("Customer:");
        assertThat(savedReceipt.getReceiptContent()).doesNotContain("Tax:");
        assertThat(savedReceipt.getReceiptContent()).doesNotContain("Discount:");
        assertThat(savedReceipt.getReceiptContent()).doesNotContain("Payment Ref:");
    }

    @Test
    void generateReceipt_ShouldTruncateLongProductNames() {
        // Arrange
        when(receiptRepository.findByTransaction(testTransaction)).thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);
        when(salesTransactionService.getTransactionById(any())).thenReturn(testTransaction);

        // Act
        receiptService.generateReceipt(testTransaction.getId());

        // Assert
        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(receiptCaptor.capture());
        Receipt savedReceipt = receiptCaptor.getValue();

        // Long product name should be truncated with "..."
        assertThat(savedReceipt.getReceiptContent()).contains("Very Long Product...");
        assertThat(savedReceipt.getPrintableContent()).contains("Very Long Product...");
    }

    @Test
    void getReceipt_WithValidTransactionId_ShouldReturnReceipt() {
        // Arrange
        when(receiptRepository.findByTransactionId("txn-1")).thenReturn(Optional.of(testReceipt));

        // Act
        Optional<Receipt> result = receiptService.getReceipt("txn-1");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testReceipt);
    }

    @Test
    void getReceipt_WithInvalidTransactionId_ShouldReturnEmpty() {
        // Arrange
        when(receiptRepository.findByTransactionId("invalid-id")).thenReturn(Optional.empty());

        // Act
        Optional<Receipt> result = receiptService.getReceipt("invalid-id");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getReceiptByNumber_WithValidNumber_ShouldReturnReceipt() {
        // Arrange
        String receiptNumber = "RCP-TXN001-20240115103000";
        when(receiptRepository.findByReceiptNumber(receiptNumber)).thenReturn(Optional.of(testReceipt));

        // Act
        Optional<Receipt> result = receiptService.getReceiptByNumber(receiptNumber);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testReceipt);
    }

    @Test
    void getReceiptByNumber_WithInvalidNumber_ShouldReturnEmpty() {
        // Arrange
        when(receiptRepository.findByReceiptNumber("invalid-number")).thenReturn(Optional.empty());

        // Act
        Optional<Receipt> result = receiptService.getReceiptByNumber("invalid-number");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void markAsPrinted_WithValidReceipt_ShouldUpdateStatus() {
        // Arrange
        when(receiptRepository.findById("receipt-1")).thenReturn(Optional.of(testReceipt));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);

        // Act
        Receipt result = receiptService.markAsPrinted("receipt-1", "printer-operator");

        // Assert
        assertThat(result.getStatus()).isEqualTo(Receipt.ReceiptStatus.PRINTED);
        assertThat(result.getPrintedBy()).isEqualTo("printer-operator");
        assertThat(result.getPrintedAt()).isNotNull();

        verify(receiptRepository).save(testReceipt);
    }

    @Test
    void markAsPrinted_WithInvalidReceiptId_ShouldThrowException() {
        // Arrange
        when(receiptRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> receiptService.markAsPrinted("invalid-id", "operator"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Receipt not found: invalid-id");

        verify(receiptRepository, never()).save(any());
    }

    @Test
    void markAsEmailed_WithValidReceipt_ShouldUpdateStatus() {
        // Arrange
        when(receiptRepository.findById("receipt-1")).thenReturn(Optional.of(testReceipt));
        when(receiptRepository.save(any(Receipt.class))).thenReturn(testReceipt);

        // Act
        Receipt result = receiptService.markAsEmailed("receipt-1", "customer@test.com");

        // Assert
        assertThat(result.getStatus()).isEqualTo(Receipt.ReceiptStatus.EMAILED);
        assertThat(result.getEmailAddress()).isEqualTo("customer@test.com");
        assertThat(result.getEmailedAt()).isNotNull();

        verify(receiptRepository).save(testReceipt);
    }

    @Test
    void markAsEmailed_WithInvalidReceiptId_ShouldThrowException() {
        // Arrange
        when(receiptRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> receiptService.markAsEmailed("invalid-id", "customer@test.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Receipt not found: invalid-id");

        verify(receiptRepository, never()).save(any());
    }

    @Test
    void regenerateReceipt_WithExistingReceipt_ShouldDeleteExisting() {
        // Arrange
        when(receiptRepository.findByTransactionId("txn-1")).thenReturn(Optional.of(testReceipt));

        // Act
        receiptService.regenerateReceipt("txn-1");

        // Assert
        verify(receiptRepository).delete(testReceipt);
    }

    @Test
    void regenerateReceipt_WithoutExistingReceipt_ShouldNotDelete() {
        // Arrange
        when(receiptRepository.findByTransactionId("txn-1")).thenReturn(Optional.empty());

        // Act
        receiptService.regenerateReceipt("txn-1");

        // Assert
        verify(receiptRepository, never()).delete(any());
    }
}