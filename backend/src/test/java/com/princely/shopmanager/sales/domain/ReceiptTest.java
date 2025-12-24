package com.princely.shopmanager.sales.domain;

import com.princely.shopmanager.core.domain.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Receipt Domain Tests")
class ReceiptTest {

    private Receipt receipt;
    private SalesTransaction testTransaction;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        testTransaction = SalesTransaction.builder()
            .id("txn-1")
            .transactionNumber("TXN-001")
            .shop(testShop)
            .build();

        receipt = Receipt.builder()
            .receiptNumber("RCT-001")
            .transaction(testTransaction)
            .issuedDate(LocalDateTime.now())
            .generatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should have default printedCount as zero")
    void shouldHaveDefaultPrintedCountAsZero() {
        // Given
        Receipt newReceipt = Receipt.builder()
            .receiptNumber("RCT-002")
            .transaction(testTransaction)
            .issuedDate(LocalDateTime.now())
            .generatedAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(newReceipt.getPrintedCount()).isZero();
    }

    @Test
    @DisplayName("Should have default emailSent as false")
    void shouldHaveDefaultEmailSentAsFalse() {
        // Given
        Receipt newReceipt = Receipt.builder()
            .receiptNumber("RCT-002")
            .transaction(testTransaction)
            .issuedDate(LocalDateTime.now())
            .generatedAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(newReceipt.isEmailSent()).isFalse();
    }

    @Test
    @DisplayName("Should have default smsSent as false")
    void shouldHaveDefaultSmsSentAsFalse() {
        // Given
        Receipt newReceipt = Receipt.builder()
            .receiptNumber("RCT-002")
            .transaction(testTransaction)
            .issuedDate(LocalDateTime.now())
            .generatedAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(newReceipt.isSmsSent()).isFalse();
    }

    @Test
    @DisplayName("Should have default format as TEXT")
    void shouldHaveDefaultFormatAsText() {
        // Given
        Receipt newReceipt = Receipt.builder()
            .receiptNumber("RCT-002")
            .transaction(testTransaction)
            .issuedDate(LocalDateTime.now())
            .generatedAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(newReceipt.getFormat()).isEqualTo(Receipt.ReceiptFormat.TEXT);
    }

    @Test
    @DisplayName("Should have default status as GENERATED")
    void shouldHaveDefaultStatusAsGenerated() {
        // Given
        Receipt newReceipt = Receipt.builder()
            .receiptNumber("RCT-002")
            .transaction(testTransaction)
            .issuedDate(LocalDateTime.now())
            .generatedAt(LocalDateTime.now())
            .build();

        // Then
        assertThat(newReceipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.GENERATED);
    }

    // incrementPrintCount tests
    @Test
    @DisplayName("incrementPrintCount - Should increment count from zero to one")
    void incrementPrintCount_shouldIncrementCountFromZeroToOne() {
        // Given
        receipt.setPrintedCount(0);
        LocalDateTime beforeIncrement = LocalDateTime.now().minusSeconds(1);

        // When
        receipt.incrementPrintCount();

        // Then
        assertThat(receipt.getPrintedCount()).isEqualTo(1);
        assertThat(receipt.getLastPrintedAt()).isNotNull();
        assertThat(receipt.getLastPrintedAt()).isAfter(beforeIncrement);
    }

    @Test
    @DisplayName("incrementPrintCount - Should increment count from one to two")
    void incrementPrintCount_shouldIncrementCountFromOneToTwo() {
        // Given
        receipt.setPrintedCount(1);

        // When
        receipt.incrementPrintCount();

        // Then
        assertThat(receipt.getPrintedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("incrementPrintCount - Should increment count multiple times")
    void incrementPrintCount_shouldIncrementCountMultipleTimes() {
        // Given
        receipt.setPrintedCount(0);

        // When
        receipt.incrementPrintCount();
        receipt.incrementPrintCount();
        receipt.incrementPrintCount();

        // Then
        assertThat(receipt.getPrintedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("incrementPrintCount - Should update lastPrintedAt each time")
    void incrementPrintCount_shouldUpdateLastPrintedAtEachTime() {
        // Given
        receipt.setPrintedCount(0);

        // When
        receipt.incrementPrintCount();
        LocalDateTime firstPrintTime = receipt.getLastPrintedAt();

        receipt.incrementPrintCount();
        LocalDateTime secondPrintTime = receipt.getLastPrintedAt();

        // Then
        // Both timestamps should be set (note: they may be equal due to fast execution)
        assertThat(firstPrintTime).isNotNull();
        assertThat(secondPrintTime)
            .isNotNull()
            .isAfterOrEqualTo(firstPrintTime);
        assertThat(receipt.getPrintedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("incrementPrintCount - Should increment from large count")
    void incrementPrintCount_shouldIncrementFromLargeCount() {
        // Given
        receipt.setPrintedCount(99);

        // When
        receipt.incrementPrintCount();

        // Then
        assertThat(receipt.getPrintedCount()).isEqualTo(100);
    }

    // ShopAware implementation tests
    @Test
    @DisplayName("getShopId - Should return shop ID from transaction")
    void getShopId_shouldReturnShopIdFromTransaction() {
        // Given
        receipt.setTransaction(testTransaction);

        // When
        String shopId = receipt.getShopId();

        // Then
        assertThat(shopId).isEqualTo("shop-1");
    }

    @Test
    @DisplayName("getShopId - Should return null when transaction is null")
    void getShopId_shouldReturnNullWhenTransactionIsNull() {
        // Given
        receipt.setTransaction(null);

        // When
        String shopId = receipt.getShopId();

        // Then
        assertThat(shopId).isNull();
    }

    @Test
    @DisplayName("getShopId - Should return null when transaction has no shop")
    void getShopId_shouldReturnNullWhenTransactionHasNoShop() {
        // Given
        SalesTransaction transactionWithoutShop = SalesTransaction.builder()
            .id("txn-2")
            .transactionNumber("TXN-002")
            .shop(null)
            .build();
        receipt.setTransaction(transactionWithoutShop);

        // When
        String shopId = receipt.getShopId();

        // Then
        assertThat(shopId).isNull();
    }

    // ReceiptFormat enum tests
    @Test
    @DisplayName("ReceiptFormat - All enum values should exist")
    void receiptFormat_allEnumValuesShouldExist() {
        Receipt.ReceiptFormat[] formats = Receipt.ReceiptFormat.values();

        assertThat(formats)
            .hasSize(4)
            .contains(
                Receipt.ReceiptFormat.TEXT,
                Receipt.ReceiptFormat.PDF,
                Receipt.ReceiptFormat.HTML,
                Receipt.ReceiptFormat.JSON
            );
    }

    // ReceiptStatus enum tests
    @Test
    @DisplayName("ReceiptStatus - All enum values should exist")
    void receiptStatus_allEnumValuesShouldExist() {
        Receipt.ReceiptStatus[] statuses = Receipt.ReceiptStatus.values();

        assertThat(statuses)
            .hasSize(5)
            .contains(
                Receipt.ReceiptStatus.GENERATED,
                Receipt.ReceiptStatus.PRINTED,
                Receipt.ReceiptStatus.EMAILED,
                Receipt.ReceiptStatus.CANCELLED,
                Receipt.ReceiptStatus.VOIDED
            );
    }

    // Email tracking tests
    @Test
    @DisplayName("Should track email sent status")
    void shouldTrackEmailSentStatus() {
        // Given
        LocalDateTime emailTime = LocalDateTime.now();
        receipt.setEmailSent(true);
        receipt.setEmailSentAt(emailTime);
        receipt.setEmailAddress("customer@example.com");

        // Then
        assertThat(receipt.isEmailSent()).isTrue();
        assertThat(receipt.getEmailSentAt()).isEqualTo(emailTime);
        assertThat(receipt.getEmailAddress()).isEqualTo("customer@example.com");
    }

    @Test
    @DisplayName("Should track emailed status separately")
    void shouldTrackEmailedStatusSeparately() {
        // Given
        LocalDateTime emailedTime = LocalDateTime.now();
        receipt.setEmailedAt(emailedTime);
        receipt.setEmailAddress("customer@example.com");

        // Then
        assertThat(receipt.getEmailedAt()).isEqualTo(emailedTime);
        assertThat(receipt.getEmailAddress()).isEqualTo("customer@example.com");
    }

    // SMS tracking tests
    @Test
    @DisplayName("Should track SMS sent status")
    void shouldTrackSmsSentStatus() {
        // Given
        LocalDateTime smsTime = LocalDateTime.now();
        receipt.setSmsSent(true);
        receipt.setSmsSentAt(smsTime);

        // Then
        assertThat(receipt.isSmsSent()).isTrue();
        assertThat(receipt.getSmsSentAt()).isEqualTo(smsTime);
    }

    // Printing tracking tests
    @Test
    @DisplayName("Should track printed status")
    void shouldTrackPrintedStatus() {
        // Given
        LocalDateTime printedTime = LocalDateTime.now();
        receipt.setPrintedAt(printedTime);
        receipt.setPrintedBy("cashier-1");

        // Then
        assertThat(receipt.getPrintedAt()).isEqualTo(printedTime);
        assertThat(receipt.getPrintedBy()).isEqualTo("cashier-1");
    }

    // Content tests
    @Test
    @DisplayName("Should store receipt content")
    void shouldStoreReceiptContent() {
        // Given
        String content = "Receipt content with transaction details";
        receipt.setReceiptContent(content);

        // Then
        assertThat(receipt.getReceiptContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should store printable content")
    void shouldStorePrintableContent() {
        // Given
        String printableContent = "Formatted content for printing";
        receipt.setPrintableContent(printableContent);

        // Then
        assertThat(receipt.getPrintableContent()).isEqualTo(printableContent);
    }

    // Format tests
    @Test
    @DisplayName("Should allow setting format to PDF")
    void shouldAllowSettingFormatToPdf() {
        // Given
        receipt.setFormat(Receipt.ReceiptFormat.PDF);

        // Then
        assertThat(receipt.getFormat()).isEqualTo(Receipt.ReceiptFormat.PDF);
    }

    @Test
    @DisplayName("Should allow setting format to HTML")
    void shouldAllowSettingFormatToHtml() {
        // Given
        receipt.setFormat(Receipt.ReceiptFormat.HTML);

        // Then
        assertThat(receipt.getFormat()).isEqualTo(Receipt.ReceiptFormat.HTML);
    }

    @Test
    @DisplayName("Should allow setting format to JSON")
    void shouldAllowSettingFormatToJson() {
        // Given
        receipt.setFormat(Receipt.ReceiptFormat.JSON);

        // Then
        assertThat(receipt.getFormat()).isEqualTo(Receipt.ReceiptFormat.JSON);
    }

    // Status tests
    @Test
    @DisplayName("Should allow setting status to PRINTED")
    void shouldAllowSettingStatusToPrinted() {
        // Given
        receipt.setStatus(Receipt.ReceiptStatus.PRINTED);

        // Then
        assertThat(receipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.PRINTED);
    }

    @Test
    @DisplayName("Should allow setting status to EMAILED")
    void shouldAllowSettingStatusToEmailed() {
        // Given
        receipt.setStatus(Receipt.ReceiptStatus.EMAILED);

        // Then
        assertThat(receipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.EMAILED);
    }

    @Test
    @DisplayName("Should allow setting status to CANCELLED")
    void shouldAllowSettingStatusToCancelled() {
        // Given
        receipt.setStatus(Receipt.ReceiptStatus.CANCELLED);

        // Then
        assertThat(receipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should allow setting status to VOIDED")
    void shouldAllowSettingStatusToVoided() {
        // Given
        receipt.setStatus(Receipt.ReceiptStatus.VOIDED);

        // Then
        assertThat(receipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.VOIDED);
    }

    // Additional field tests
    @Test
    @DisplayName("Should store QR code")
    void shouldStoreQrCode() {
        // Given
        String qrCode = "QR_CODE_DATA_BASE64_ENCODED";
        receipt.setQrCode(qrCode);

        // Then
        assertThat(receipt.getQrCode()).isEqualTo(qrCode);
    }

    @Test
    @DisplayName("Should store signature URL")
    void shouldStoreSignatureUrl() {
        // Given
        String signatureUrl = "https://example.com/signatures/receipt-001.png";
        receipt.setSignatureUrl(signatureUrl);

        // Then
        assertThat(receipt.getSignatureUrl()).isEqualTo(signatureUrl);
    }

    // Complex scenarios
    @Test
    @DisplayName("Should track complete printing workflow")
    void shouldTrackCompletePrintingWorkflow() {
        // Given
        receipt.setPrintedCount(0);
        receipt.setStatus(Receipt.ReceiptStatus.GENERATED);

        // When - first print
        receipt.incrementPrintCount();
        receipt.setPrintedAt(LocalDateTime.now());
        receipt.setPrintedBy("cashier-1");
        receipt.setStatus(Receipt.ReceiptStatus.PRINTED);

        // When - second print (reprint)
        receipt.incrementPrintCount();

        // Then
        assertThat(receipt.getPrintedCount()).isEqualTo(2);
        assertThat(receipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.PRINTED);
        assertThat(receipt.getPrintedBy()).isEqualTo("cashier-1");
        assertThat(receipt.getLastPrintedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should track complete email workflow")
    void shouldTrackCompleteEmailWorkflow() {
        // Given
        receipt.setEmailSent(false);
        receipt.setStatus(Receipt.ReceiptStatus.GENERATED);

        // When
        LocalDateTime emailTime = LocalDateTime.now();
        receipt.setEmailSent(true);
        receipt.setEmailSentAt(emailTime);
        receipt.setEmailAddress("customer@example.com");
        receipt.setEmailedAt(emailTime);
        receipt.setStatus(Receipt.ReceiptStatus.EMAILED);

        // Then
        assertThat(receipt.isEmailSent()).isTrue();
        assertThat(receipt.getEmailSentAt()).isEqualTo(emailTime);
        assertThat(receipt.getEmailedAt()).isEqualTo(emailTime);
        assertThat(receipt.getEmailAddress()).isEqualTo("customer@example.com");
        assertThat(receipt.getStatus()).isEqualTo(Receipt.ReceiptStatus.EMAILED);
    }

    @Test
    @DisplayName("Should support both printing and emailing")
    void shouldSupportBothPrintingAndEmailing() {
        // When
        receipt.incrementPrintCount();
        receipt.setPrintedAt(LocalDateTime.now());
        receipt.setPrintedBy("cashier-1");

        receipt.setEmailSent(true);
        receipt.setEmailSentAt(LocalDateTime.now());
        receipt.setEmailAddress("customer@example.com");

        // Then
        assertThat(receipt.getPrintedCount()).isEqualTo(1);
        assertThat(receipt.isEmailSent()).isTrue();
    }
}
