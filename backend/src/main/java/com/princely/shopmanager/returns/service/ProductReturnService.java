package com.princely.shopmanager.returns.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.inventory.service.InventoryService;
import com.princely.shopmanager.investment.service.FraudDetectionService;
import com.princely.shopmanager.returns.domain.ProductReturn;
import com.princely.shopmanager.returns.dto.ProductReturnCreateRequest;
import com.princely.shopmanager.returns.dto.ProductReturnResponse;
import com.princely.shopmanager.returns.repository.ProductReturnRepository;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductReturnService {

    private final ProductReturnRepository returnRepository;
    private final FraudDetectionService fraudDetectionService;
    private final InventoryService inventoryService;
    private final AuditService auditService;

    public ProductReturnResponse createReturn(ProductReturnCreateRequest request) {
        String shopId = TenantContext.getCurrentTenant();

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

    public ProductReturnResponse processReturn(String returnId) {
        ProductReturn productReturn = returnRepository.findById(returnId)
            .orElseThrow(() -> new EntityNotFoundException("Product return not found"));

        if (!productReturn.canProcess()) {
            throw new IllegalStateException("Return cannot be processed in current state");
        }

        if (productReturn.isRestockable()) {
            inventoryService.returnStock(
                productReturn.getProduct().getId(),
                productReturn.getQuantityReturned(),
                returnId
            );
        }

        productReturn.complete();
        productReturn = returnRepository.save(productReturn);

        auditService.logEntityModification("ProductReturn", productReturn.getId(),
            "Product return processed successfully");

        return mapToResponse(productReturn);
    }

    @Transactional(readOnly = true)
    public Page<ProductReturnResponse> getReturns(String shopId, Pageable pageable) {
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