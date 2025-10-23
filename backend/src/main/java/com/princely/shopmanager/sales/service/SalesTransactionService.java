package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.dto.SalesTransactionCreateRequest;
import com.princely.shopmanager.sales.dto.SalesTransactionResponse;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.security.TenantSecurityValidator;
import com.princely.shopmanager.shared.service.AuditService;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesTransactionService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TenantSecurityValidator tenantSecurityValidator;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SalesTransactionResponse createTransaction(SalesTransactionCreateRequest request) {
        log.info("Creating sales transaction for shop: {}", request.getShopId());

        // Get shop and validate tenant access
        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + request.getShopId()));
        tenantSecurityValidator.validateShopAccess(shop);

        // Get current user as cashier
        String currentUserId = TenantContext.getCurrentUserId();
        User cashier = userRepository.findByKeycloakId(currentUserId)
            .orElseThrow(() -> new IllegalStateException("Current user not found: " + currentUserId));

        // Generate transaction number
        String transactionNumber = generateTransactionNumber(shop.getId());

        // Build transaction
        SalesTransaction transaction = SalesTransaction.builder()
            .transactionNumber(transactionNumber)
            .shop(shop)
            .cashier(cashier)
            .customerName(request.getCustomerName())
            .customerPhone(request.getCustomerPhone())
            .customerEmail(request.getCustomerEmail())
            .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
            .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
            .paymentMethod(request.getPaymentMethod())
            .paymentReference(request.getPaymentReference())
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .transactionDate(LocalDateTime.now())
            .notes(request.getNotes())
            .build();

        // Add line items
        for (SalesTransactionCreateRequest.LineItemRequest lineItemRequest : request.getLineItems()) {
            Product product = productRepository.findById(lineItemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineItemRequest.getProductId()));

            LineItem lineItem = LineItem.builder()
                .product(product)
                .quantity(lineItemRequest.getQuantity())
                .unitPrice(lineItemRequest.getUnitPrice())
                .discountAmount(lineItemRequest.getDiscount() != null ? lineItemRequest.getDiscount() : BigDecimal.ZERO)
                .build();
            lineItem.calculateLineTotal();

            transaction.addLineItem(lineItem);
        }

        transaction.recalculateTotals();
        transaction = salesTransactionRepository.save(transaction);

        // Audit the creation
        auditService.logEntityCreation("SalesTransaction", transaction.getId(),
            "Sales transaction created: " + transactionNumber + " - Total: " + transaction.getTotalAmount());

        log.info("Successfully created sales transaction: {}", transactionNumber);
        return SalesTransactionResponse.fromEntity(transaction);
    }

    @Transactional(readOnly = true)
    public SalesTransactionResponse getTransaction(String id) {
        SalesTransaction transaction = salesTransactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        tenantSecurityValidator.validateShopAccess(transaction.getShop());

        return SalesTransactionResponse.fromEntity(transaction);
    }

    @Transactional(readOnly = true)
    public SalesTransaction getTransactionById(String id) {
        return salesTransactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<SalesTransactionResponse> getTransactions(String shopId, Pageable pageable) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));
        tenantSecurityValidator.validateShopAccess(shop);

        Page<SalesTransaction> transactions = salesTransactionRepository.findAll(pageable);
        return transactions.map(SalesTransactionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<SalesTransactionResponse> getTransactionsByDateRange(
            String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));
        tenantSecurityValidator.validateShopAccess(shop);

        List<SalesTransaction> transactions = salesTransactionRepository
            .findByShopAndDateRange(shopId, startDate, endDate);

        return transactions.stream()
            .map(SalesTransactionResponse::fromEntity)
            .toList();
    }

    @Transactional
    public void voidTransaction(String id, String reason) {
        SalesTransaction transaction = salesTransactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        tenantSecurityValidator.validateShopAccess(transaction.getShop());

        if (transaction.isVoided()) {
            throw new IllegalStateException("Transaction is already voided");
        }

        String currentUserId = TenantContext.getCurrentUserId();
        transaction.setVoided(true);
        transaction.setVoidReason(reason);
        transaction.setVoidedBy(currentUserId);
        transaction.setVoidedAt(LocalDateTime.now());
        transaction.setStatus(SalesTransaction.TransactionStatus.CANCELLED);

        salesTransactionRepository.save(transaction);

        auditService.logEntityModification("SalesTransaction", id,
            "Transaction voided by " + currentUserId + " - Reason: " + reason);

        log.info("Voided transaction: {} by user: {}", id, currentUserId);
    }

    private String generateTransactionNumber(String shopId) {
        String prefix = "TXN-" + shopId.substring(0, Math.min(8, shopId.length())).toUpperCase();
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String transactionNumber = prefix + "-" + uniquePart;

        // Ensure uniqueness
        while (salesTransactionRepository.existsByTransactionNumber(transactionNumber)) {
            uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            transactionNumber = prefix + "-" + uniquePart;
        }

        return transactionNumber;
    }
}
