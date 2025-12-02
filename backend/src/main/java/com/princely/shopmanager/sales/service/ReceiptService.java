package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.sales.domain.Receipt;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.ReceiptRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.ShopAwareService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReceiptService extends ShopAwareService {

    private final ReceiptRepository receiptRepository;
    private final SalesTransactionRepository transactionRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReceiptService(
            ShopAccessValidator shopAccessValidator,
            ShopRepository shopRepository,
            ReceiptRepository receiptRepository,
            SalesTransactionRepository transactionRepository
    ) {
        super(shopAccessValidator, shopRepository);
        this.receiptRepository = receiptRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Helper method to find a receipt and validate shop access.
     */
    private Receipt findReceiptForUser(String receiptId, JwtPrincipal principal) {
        Receipt receipt = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new EntityNotFoundException("Receipt not found: " + receiptId));

        if (shopAccessValidator.hasNoAccessToShop(receipt.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this receipt");
        }

        return receipt;
    }

    @Transactional
    public Receipt generateReceipt(SalesTransaction transaction) {
        log.debug("Generating receipt for transaction {}", transaction.getTransactionNumber());

        // Check if receipt already exists
        Optional<Receipt> existingReceipt = receiptRepository.findByTransaction(transaction);
        if (existingReceipt.isPresent()) {
            log.debug("Receipt already exists for transaction {}", transaction.getTransactionNumber());
            return existingReceipt.get();
        }

        // Generate receipt content
        String receiptContent = generateReceiptContent(transaction);
        String printableContent = generatePrintableContent(transaction);

        Receipt receipt = Receipt.builder()
            .transaction(transaction)
            .receiptNumber(generateReceiptNumber(transaction))
            .receiptContent(receiptContent)
            .printableContent(printableContent)
            .format(Receipt.ReceiptFormat.TEXT)
            .status(Receipt.ReceiptStatus.GENERATED)
            .generatedAt(LocalDateTime.now())
            .build();

        receipt = receiptRepository.save(receipt);

        log.info("Generated receipt {} for transaction {}",
            receipt.getReceiptNumber(), transaction.getTransactionNumber());

        return receipt;
    }

    private String generateReceiptNumber(SalesTransaction transaction) {
        return "RCP-" + transaction.getTransactionNumber() + "-" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generateReceiptContent(SalesTransaction transaction) {
        StringBuilder content = new StringBuilder();

        // Header
        content.append("=== RECEIPT ===\n");
        content.append("Shop: ").append(transaction.getShop().getName()).append("\n");
        content.append("Address: ").append(transaction.getShop().getAddress()).append("\n");
        content.append("Phone: ").append(transaction.getShop().getPhone()).append("\n");
        content.append("Email: ").append(transaction.getShop().getEmail()).append("\n\n");

        // Transaction details
        content.append("Transaction #: ").append(transaction.getTransactionNumber()).append("\n");
        content.append("Date: ").append(transaction.getTransactionDate().format(DATE_FORMATTER)).append("\n");
        content.append("Cashier: ").append(transaction.getCashier().getUsername()).append("\n\n");

        // Customer details (if available)
        if (transaction.getCustomerName() != null) {
            content.append("Customer: ").append(transaction.getCustomerName()).append("\n");
            if (transaction.getCustomerPhone() != null) {
                content.append("Phone: ").append(transaction.getCustomerPhone()).append("\n");
            }
            content.append("\n");
        }

        // Line items
        content.append("ITEMS:\n");
        content.append("-".repeat(50)).append("\n");
        content.append(String.format("%-20s %6s %8s %10s\n", "Item", "Qty", "Price", "Total"));
        content.append("-".repeat(50)).append("\n");

        transaction.getLineItems().forEach(lineItem -> {
            content.append(String.format("%-20s %6d %8.2f %10.2f\n",
                lineItem.getProduct().getName().length() > 20 ?
                    lineItem.getProduct().getName().substring(0, 17) + "..." :
                    lineItem.getProduct().getName(),
                lineItem.getQuantity(),
                lineItem.getUnitPrice(),
                lineItem.getLineTotal()
            ));
        });

        content.append("-".repeat(50)).append("\n");

        // Totals
        content.append(String.format("%-35s %10.2f\n", "Subtotal:", transaction.getSubtotal()));

        if (transaction.getTaxAmount() != null && transaction.getTaxAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            content.append(String.format("%-35s %10.2f\n", "Tax:", transaction.getTaxAmount()));
        }

        if (transaction.getDiscountAmount() != null && transaction.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            content.append(String.format("%-35s %10.2f\n", "Discount:", transaction.getDiscountAmount()));
        }

        content.append(String.format("%-35s %10.2f\n", "TOTAL:", transaction.getTotalAmount()));
        content.append("-".repeat(50)).append("\n");

        // Payment details
        content.append("Payment Method: ").append(transaction.getPaymentMethod()).append("\n");
        if (transaction.getPaymentReference() != null) {
            content.append("Payment Ref: ").append(transaction.getPaymentReference()).append("\n");
        }

        // Footer
        content.append("\nThank you for shopping with us!\n");
        content.append("Please keep this receipt for your records.\n");

        return content.toString();
    }

    private String generatePrintableContent(SalesTransaction transaction) {
        // Generate printer-friendly content with proper formatting
        StringBuilder printable = new StringBuilder();

        // Center-aligned header
        String shopName = transaction.getShop().getName().toUpperCase();
        printable.append(centerText(shopName, 48)).append("\n");
        printable.append(centerText("SALES RECEIPT", 48)).append("\n");
        printable.append("=".repeat(48)).append("\n\n");

        // Transaction info
        printable.append(String.format("Receipt #: %s\n", generateReceiptNumber(transaction)));
        printable.append(String.format("Date: %s\n", transaction.getTransactionDate().format(DATE_FORMATTER)));
        printable.append(String.format("Cashier: %s\n\n", transaction.getCashier().getUsername()));

        // Items header
        printable.append(String.format("%-20s %4s %8s %10s\n", "ITEM", "QTY", "PRICE", "AMOUNT"));
        printable.append("-".repeat(48)).append("\n");

        // Items
        transaction.getLineItems().forEach(item -> {
            String itemName = item.getProduct().getName();
            if (itemName.length() > 20) {
                itemName = itemName.substring(0, 17) + "...";
            }
            printable.append(String.format("%-20s %4d %8.2f %10.2f\n",
                itemName,
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()));
        });

        printable.append("-".repeat(48)).append("\n");

        // Totals
        printable.append(String.format("%32s: %10.2f\n", "SUBTOTAL", transaction.getSubtotal()));

        if (transaction.getTaxAmount() != null && transaction.getTaxAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            printable.append(String.format("%32s: %10.2f\n", "TAX", transaction.getTaxAmount()));
        }

        if (transaction.getDiscountAmount() != null && transaction.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            printable.append(String.format("%32s: %10.2f\n", "DISCOUNT", transaction.getDiscountAmount()));
        }

        printable.append("=".repeat(48)).append("\n");
        printable.append(String.format("%32s: %10.2f\n", "TOTAL", transaction.getTotalAmount()));
        printable.append("=".repeat(48)).append("\n\n");

        // Payment info
        printable.append(String.format("Payment: %s\n", transaction.getPaymentMethod()));
        if (transaction.getPaymentReference() != null) {
            printable.append(String.format("Ref: %s\n", transaction.getPaymentReference()));
        }

        // Footer
        printable.append("\n");
        printable.append(centerText("THANK YOU!", 48)).append("\n");
        printable.append(centerText("Please come again", 48)).append("\n");

        return printable.toString();
    }

    private String centerText(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int spaces = (width - text.length()) / 2;
        return " ".repeat(spaces) + text + " ".repeat(width - text.length() - spaces);
    }

    @Transactional(readOnly = true)
    public Optional<Receipt> getReceipt(String transactionId, JwtPrincipal principal) {
        // Validate transaction access first
        SalesTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + transactionId));

        if (shopAccessValidator.hasNoAccessToShop(transaction.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this transaction");
        }

        return receiptRepository.findByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public Optional<Receipt> getReceiptByNumber(String receiptNumber, JwtPrincipal principal) {
        Optional<Receipt> receipt = receiptRepository.findByReceiptNumber(receiptNumber);

        if (receipt.isPresent()) {
            if (shopAccessValidator.hasNoAccessToShop(receipt.get().getShopId(), principal)) {
                throw new AccessDeniedException("You don't have permission to access this receipt");
            }
        }

        return receipt;
    }

    @Transactional(readOnly = true)
    public Page<Receipt> findAllReceipts(String shopId, Pageable pageable, JwtPrincipal principal) {
        // Apply scope-based filtering based on user's role
        FilterScope filterScope = getFilterScope(principal);

        // If shopId is provided in the request, validate access and use it
        if (shopId != null && !shopId.trim().isEmpty()) {
            validateShopAccess(shopId, principal);
            Specification<Receipt> spec = (root, query, criteriaBuilder) -> {
                Join<Receipt, SalesTransaction> transactionJoin = root.join("transaction");
                return criteriaBuilder.equal(transactionJoin.get("shop").get("id"), shopId);
            };
            return receiptRepository.findAll(spec, pageable);
        }

        // Otherwise, apply filtering based on user's role
        if (filterScope.isSystemWide()) {
            // SYSTEM_ADMIN: Return all receipts
            log.debug("SYSTEM_ADMIN: Returning all receipts");
            return receiptRepository.findAll(pageable);
        } else if (filterScope.isTenantWide()) {
            // TENANT_ADMIN/OWNER/INVESTOR: Filter by tenantId
            log.debug("Tenant-wide access: Filtering receipts by tenantId: {}", filterScope.getTenantId());
            Specification<Receipt> spec = (root, query, criteriaBuilder) -> {
                Join<Receipt, SalesTransaction> transactionJoin = root.join("transaction");
                Join<SalesTransaction, com.princely.shopmanager.core.domain.Shop> shopJoin = transactionJoin.join("shop");
                return criteriaBuilder.equal(shopJoin.get("tenant").get("id"), filterScope.getTenantId());
            };
            return receiptRepository.findAll(spec, pageable);
        } else {
            // MANAGER/EMPLOYEE/etc.: Filter by shopId
            log.debug("Shop-scoped access: Filtering receipts by shopId: {}", filterScope.getShopId());
            Specification<Receipt> spec = (root, query, criteriaBuilder) -> {
                Join<Receipt, SalesTransaction> transactionJoin = root.join("transaction");
                return criteriaBuilder.equal(transactionJoin.get("shop").get("id"), filterScope.getShopId());
            };
            return receiptRepository.findAll(spec, pageable);
        }
    }

    @Transactional
    public Receipt markAsPrinted(String receiptId, String printedBy, JwtPrincipal principal) {
        Receipt receipt = findReceiptForUser(receiptId, principal);

        receipt.setStatus(Receipt.ReceiptStatus.PRINTED);
        receipt.setPrintedAt(LocalDateTime.now());
        receipt.setPrintedBy(printedBy);

        return receiptRepository.save(receipt);
    }

    @Transactional
    public Receipt markAsEmailed(String receiptId, String emailAddress, JwtPrincipal principal) {
        Receipt receipt = findReceiptForUser(receiptId, principal);

        receipt.setStatus(Receipt.ReceiptStatus.EMAILED);
        receipt.setEmailedAt(LocalDateTime.now());
        receipt.setEmailAddress(emailAddress);

        return receiptRepository.save(receipt);
    }

    @Transactional
    public void regenerateReceipt(String transactionId, JwtPrincipal principal) {
        // Validate transaction access first
        SalesTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found: " + transactionId));

        if (shopAccessValidator.hasNoAccessToShop(transaction.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this transaction");
        }

        Optional<Receipt> existingReceipt = receiptRepository.findByTransactionId(transactionId);
        if (existingReceipt.isPresent()) {
            receiptRepository.delete(existingReceipt.get());
            log.info("Deleted existing receipt for regeneration: {}", existingReceipt.get().getReceiptNumber());
        }

        // The receipt will be regenerated when requested next time
        log.info("Marked receipt for regeneration for transaction: {}", transactionId);
    }
}