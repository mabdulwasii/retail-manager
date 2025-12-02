package com.princely.shopmanager.returns.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.inventory.service.InventoryService;
import com.princely.shopmanager.returns.domain.ProductReturn;
import com.princely.shopmanager.returns.dto.ProductReturnCreateRequest;
import com.princely.shopmanager.returns.dto.ProductReturnResponse;
import com.princely.shopmanager.returns.repository.ProductReturnRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.service.ShopAwareService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@Transactional
public class ProductReturnService extends ShopAwareService {

    private final ProductReturnRepository returnRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    public ProductReturnService(
            ShopAccessValidator shopAccessValidator,
            ShopRepository shopRepository,
            ProductReturnRepository returnRepository,
            InventoryService inventoryService,
            AuditService auditService
    ) {
        super(shopAccessValidator, shopRepository);
        this.returnRepository = returnRepository;
        this.inventoryService = inventoryService;
        this.auditService = auditService;
    }

    /**
     * Helper method to find a product return and validate shop access.
     */
    private ProductReturn findReturnForUser(String returnId, JwtPrincipal principal) {
        ProductReturn productReturn = returnRepository.findById(returnId)
            .orElseThrow(() -> new EntityNotFoundException("Product return not found: " + returnId));

        if (shopAccessValidator.hasNoAccessToShop(productReturn.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this product return");
        }

        return productReturn;
    }

    public ProductReturnResponse createReturn(ProductReturnCreateRequest request, JwtPrincipal principal) {
        // TODO: Validate shop access via salesTransactionId when request structure is complete
        // Shop access is currently enforced at controller level via path parameter

        ProductReturn productReturn = ProductReturn.builder()
            .quantityReturned(request.getQuantityReturned())
            .returnReason(request.getReturnReason())
            .returnType(request.getReturnType())
            .customerNotes(request.getCustomerNotes())
            .build();

        productReturn = returnRepository.save(productReturn);

        auditService.logEntityCreation("ProductReturn", productReturn.getId(),
            "Product return created for quantity: " + request.getQuantityReturned());

        return mapToResponse(productReturn);
    }

    public ProductReturnResponse processReturn(String returnId, JwtPrincipal principal) {
        ProductReturn productReturn = findReturnForUser(returnId, principal);

        if (!productReturn.canProcess()) {
            throw new IllegalStateException("Return cannot be processed in current state");
        }

        if (productReturn.isRestockable()) {
            inventoryService.returnStock(
                productReturn.getProduct().getId(),
                productReturn.getQuantityReturned(),
                returnId,
                principal
            );
        }

        productReturn.complete();
        productReturn = returnRepository.save(productReturn);

        auditService.logEntityModification("ProductReturn", productReturn.getId(),
            "Product return processed successfully");

        return mapToResponse(productReturn);
    }

    @Transactional(readOnly = true)
    public Page<ProductReturnResponse> getReturns(String shopId, Pageable pageable, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        return returnRepository.findByShopId(shopId, pageable)
            .map(this::mapToResponse);
    }

    private ProductReturnResponse mapToResponse(ProductReturn productReturn) {
        return ProductReturnResponse.builder()
            .id(productReturn.getId())
            .shopId(productReturn.getShop().getId())
            .quantityReturned(productReturn.getQuantityReturned())
            .returnReason(productReturn.getReturnReason())
            .returnType(productReturn.getReturnType())
            .status(productReturn.getStatus())
            .returnDate(productReturn.getReturnDate())
            .processedDate(productReturn.getProcessedDate())
            .refundAmount(productReturn.getRefundAmount())
            .build();
    }
}