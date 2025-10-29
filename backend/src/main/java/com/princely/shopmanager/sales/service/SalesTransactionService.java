package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.core.service.ProductService;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.domain.InventoryHistory;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.inventory.service.InventoryService;
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
import java.util.ArrayList;
import java.util.Comparator;
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
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final ProductService productService;
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

        // Validate inventory availability for all products BEFORE creating transaction
        List<InventoryAllocation> allocations = new ArrayList<>();
        for (SalesTransactionCreateRequest.LineItemRequest lineItemRequest : request.getLineItems()) {
            Product product = productRepository.findById(lineItemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineItemRequest.getProductId()));

            // Check if sufficient stock available
            if (!productService.hasAvailableStock(product.getId(), lineItemRequest.getQuantity())) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName() +
                    ". Required: " + lineItemRequest.getQuantity());
            }

            // Allocate inventory using FEFO (First Expiry, First Out) strategy
            List<Inventory> allocated = allocateInventory(shop.getId(), product.getId(), lineItemRequest.getQuantity());
            allocations.add(new InventoryAllocation(product, lineItemRequest, allocated));
        }

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
        for (InventoryAllocation allocation : allocations) {
            LineItem lineItem = LineItem.builder()
                .product(allocation.product)
                .quantity(allocation.request.getQuantity())
                .unitPrice(allocation.request.getUnitPrice())
                .discountAmount(allocation.request.getDiscount() != null ? allocation.request.getDiscount() : BigDecimal.ZERO)
                .build();
            lineItem.calculateLineTotal();

            transaction.addLineItem(lineItem);
        }

        transaction.recalculateTotals();
        transaction = salesTransactionRepository.save(transaction);

        // Deduct stock from allocated inventories
        for (InventoryAllocation allocation : allocations) {
            for (Inventory inventory : allocation.inventories) {
                int quantityToDeduct = Math.min(allocation.remainingQuantity, inventory.getAvailableStock());
                inventoryService.sellStock(inventory.getId(), quantityToDeduct, transaction.getId());
                allocation.remainingQuantity -= quantityToDeduct;

                log.debug("Deducted {} units from inventory {} for product {}",
                    quantityToDeduct, inventory.getId(), allocation.product.getName());

                if (allocation.remainingQuantity <= 0) {
                    break;
                }
            }
        }

        // Audit the creation
        auditService.logEntityCreation("SalesTransaction", transaction.getId(),
            "Sales transaction created: " + transactionNumber + " - Total: " + transaction.getTotalAmount());

        log.info("Successfully created sales transaction: {} with inventory deduction", transactionNumber);
        return SalesTransactionResponse.fromEntity(transaction);
    }

    /**
     * Allocates inventory for a product using FEFO (First Expiry, First Out) strategy.
     * Prioritizes batches that expire sooner to minimize waste.
     *
     * @param shopId Shop ID
     * @param productId Product ID
     * @param quantity Required quantity
     * @return List of inventory records to use, sorted by priority
     * @throws IllegalStateException if insufficient stock
     */
    private List<Inventory> allocateInventory(String shopId, String productId, int quantity) {
        List<Inventory> availableInventories = inventoryRepository.findByProductId(productId).stream()
            .filter(inv -> inv.getShop().getId().equals(shopId))
            .filter(inv -> inv.getStatus() == Inventory.InventoryStatus.ACTIVE)
            .filter(inv -> !inv.isExpired())
            .filter(inv -> inv.getAvailableStock() > 0)
            .sorted(Comparator
                // First: prioritize expiring batches (FEFO)
                .comparing((Inventory inv) -> inv.getExpiryDate() != null ? inv.getExpiryDate() : java.time.LocalDate.MAX)
                // Second: older batches first (FIFO for same expiry)
                .thenComparing(Inventory::getCreatedAt))
            .toList();

        int totalAvailable = availableInventories.stream()
            .mapToInt(Inventory::getAvailableStock)
            .sum();

        if (totalAvailable < quantity) {
            throw new IllegalStateException("Insufficient available stock. Required: " + quantity +
                ", Available: " + totalAvailable);
        }

        // Return inventories that will be used (may be multiple batches)
        List<Inventory> allocated = new ArrayList<>();
        int remaining = quantity;
        for (Inventory inv : availableInventories) {
            allocated.add(inv);
            remaining -= inv.getAvailableStock();
            if (remaining <= 0) {
                break;
            }
        }

        return allocated;
    }

    /**
     * Helper class to track inventory allocation for a line item
     */
    private static class InventoryAllocation {
        final Product product;
        final SalesTransactionCreateRequest.LineItemRequest request;
        final List<Inventory> inventories;
        int remainingQuantity;

        InventoryAllocation(Product product, SalesTransactionCreateRequest.LineItemRequest request,
                           List<Inventory> inventories) {
            this.product = product;
            this.request = request;
            this.inventories = inventories;
            this.remainingQuantity = request.getQuantity();
        }
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
