package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for updating product fields.
 * Handles validation and field assignment logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFieldUpdater {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Updates product basic fields (name, description, status).
     *
     * @param product Product to update
     * @param request Update request
     * @param changes StringBuilder to track changes for audit
     */
    public void updateBasicFields(Product product, ProductUpdateRequest request, StringBuilder changes) {
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            changes.append("Name: ").append(product.getName()).append(" → ").append(request.getName()).append("; ");
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getStatus() != null && request.getStatus() != product.getStatus()) {
            changes.append("Status: ").append(product.getStatus()).append(" → ").append(request.getStatus()).append("; ");
            product.setStatus(request.getStatus());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
    }

    /**
     * Updates product catalog fields (barcode, category, unit, dimensions).
     *
     * @param product Product to update
     * @param request Update request
     * @throws IllegalArgumentException if barcode already exists
     * @throws EntityNotFoundException if category not found
     */
    public void updateCatalogFields(Product product, ProductUpdateRequest request) {
        if (request.getBarcode() != null && !request.getBarcode().equals(product.getBarcode())) {
            // Check barcode uniqueness
            if (productRepository.existsByBarcodeAndShopId(request.getBarcode(), product.getShop().getId())) {
                throw new IllegalArgumentException("Barcode already exists: " + request.getBarcode());
            }
            product.setBarcode(request.getBarcode());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }

        if (request.getWeightInGrams() != null) {
            product.setWeightInGrams(request.getWeightInGrams());
        }

        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }
    }

    /**
     * Updates product supplier and pricing fields.
     *
     * @param product Product to update
     * @param request Update request
     */
    public void updateSupplierAndPricingFields(Product product, ProductUpdateRequest request) {
        if (request.getSupplierName() != null) {
            product.setSupplierName(request.getSupplierName());
        }

        if (request.getSupplierContact() != null) {
            product.setSupplierContact(request.getSupplierContact());
        }

        if (request.getIsTaxable() != null) {
            product.setTaxable(request.getIsTaxable());
        }

        if (request.getIsDiscountable() != null) {
            product.setDiscountable(request.getIsDiscountable());
        }

        if (request.getMetadata() != null) {
            product.setMetadata(request.getMetadata());
        }
    }
}
