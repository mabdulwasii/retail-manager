package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.service.InventoryService;
import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.dto.SalesTransactionCreateRequest;
import com.princely.shopmanager.sales.dto.SalesTransactionResponse;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.service.ShopAwareService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SalesTransactionService extends ShopAwareService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReceiptService receiptService;
    private final InventoryAllocationService inventoryAllocationService;
    private final SalesLineItemBuilder lineItemBuilder;

    public SalesTransactionService(
            ShopAccessValidator shopAccessValidator,
            ShopRepository shopRepository,
            SalesTransactionRepository salesTransactionRepository,
            UserRepository userRepository,
            InventoryService inventoryService,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher,
            ReceiptService receiptService,
            InventoryAllocationService inventoryAllocationService,
            SalesLineItemBuilder lineItemBuilder
    ) {
        super(shopAccessValidator, shopRepository);
        this.salesTransactionRepository = salesTransactionRepository;
        this.userRepository = userRepository;
        this.inventoryService = inventoryService;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.receiptService = receiptService;
        this.inventoryAllocationService = inventoryAllocationService;
        this.lineItemBuilder = lineItemBuilder;
    }

    /**
     * Helper method to find a sales transaction and validate shop access.
     */
    private SalesTransaction findTransactionForUser(String transactionId, JwtPrincipal principal) {
        SalesTransaction transaction = salesTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new EntityNotFoundException("Sales transaction not found: " + transactionId));

        if (shopAccessValidator.hasNoAccessToShop(transaction.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this sales transaction");
        }

        return transaction;
    }

    @Transactional
    public SalesTransactionResponse createTransaction(SalesTransactionCreateRequest request, JwtPrincipal principal) {
        log.info("Creating sales transaction for shop: {}", request.getShopId());

        // Validate shop access and get shop
        validateShopAccess(request.getShopId(), principal);
        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + request.getShopId()));

        // Get current user as cashier
        String currentUserId = TenantContext.getCurrentUserId();
        User cashier = userRepository.findByKeycloakId(currentUserId)
            .orElseThrow(() -> new IllegalStateException("Current user not found: " + currentUserId));

        // Generate transaction number
        String transactionNumber = generateTransactionNumber(shop.getId());

        // Validate and allocate inventory (delegated to service)
        List<InventoryAllocationService.InventoryAllocation> allocations =
            inventoryAllocationService.validateAndAllocate(request.getLineItems(), shop.getId(), principal);

        // Build transaction
        SalesTransaction transaction = buildTransaction(request, shop, cashier, transactionNumber);

        // Build and add line items (delegated to builder)
        List<LineItem> lineItems = lineItemBuilder.buildFromAllocations(allocations);
        lineItems.forEach(transaction::addLineItem);

        transaction.recalculateTotals();
        transaction = salesTransactionRepository.save(transaction);

        // Deduct stock from allocated inventories
        deductInventoryStock(allocations, transaction.getId(), principal);

        // Audit the creation
        auditService.logEntityCreation("SalesTransaction", transaction.getId(),
            "Sales transaction created: " + transactionNumber + " - Total: " + transaction.getTotalAmount());

        // Automatically generate receipt for the transaction
        generateReceiptSafely(transactionNumber, transaction);

        log.info("Successfully created sales transaction: {} with inventory deduction", transactionNumber);
        return SalesTransactionResponse.fromEntity(transaction);
    }

    /**
     * Builds a sales transaction entity from the request.
     */
    private SalesTransaction buildTransaction(SalesTransactionCreateRequest request, Shop shop,
                                             User cashier, String transactionNumber) {
        return SalesTransaction.builder()
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
    }

    /**
     * Deducts stock from allocated inventories.
     */
    private void deductInventoryStock(List<InventoryAllocationService.InventoryAllocation> allocations,
                                     String transactionId, JwtPrincipal principal) {
        for (InventoryAllocationService.InventoryAllocation allocation : allocations) {
            for (Inventory inventory : allocation.inventories) {
                int quantityToDeduct = Math.min(allocation.remainingQuantity, inventory.getAvailableStock());
                inventoryService.sellStock(inventory.getId(), quantityToDeduct, transactionId, principal);
                allocation.remainingQuantity -= quantityToDeduct;

                log.debug("Deducted {} units from inventory {} for product {}",
                    quantityToDeduct, inventory.getId(), allocation.product.getName());

                if (allocation.remainingQuantity <= 0) {
                    break;
                }
            }
        }
    }

    /**
     * Generates receipt safely, logging errors without failing the transaction.
     */
    private void generateReceiptSafely(String transactionNumber, SalesTransaction transaction) {
        try {
            receiptService.generateReceipt(transaction);
            log.info("Receipt automatically generated for transaction: {}", transactionNumber);
        } catch (Exception e) {
            log.error("Failed to auto-generate receipt for transaction: {}", transactionNumber, e);
            // Don't fail the transaction creation if receipt generation fails
        }
    }


    @Transactional(readOnly = true)
    public SalesTransactionResponse getTransaction(String id, JwtPrincipal principal) {
        SalesTransaction transaction = findTransactionForUser(id, principal);
        return SalesTransactionResponse.fromEntity(transaction);
    }

    @Transactional(readOnly = true)
    public SalesTransaction getTransactionById(String id, JwtPrincipal principal) {
        return findTransactionForUser(id, principal);
    }

    @Transactional(readOnly = true)
    public Page<SalesTransactionResponse> getTransactions(String shopId, Pageable pageable, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        Page<SalesTransaction> transactions = salesTransactionRepository.findAll(pageable);
        return transactions.map(SalesTransactionResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<SalesTransactionResponse> getTransactionsByDateRange(
            String shopId, LocalDateTime startDate, LocalDateTime endDate, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        List<SalesTransaction> transactions = salesTransactionRepository
            .findByShopAndDateRange(shopId, startDate, endDate);

        return transactions.stream()
            .map(SalesTransactionResponse::fromEntity)
            .toList();
    }

    @Transactional
    public void voidTransaction(String id, String reason, JwtPrincipal principal) {
        SalesTransaction transaction = findTransactionForUser(id, principal);

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
