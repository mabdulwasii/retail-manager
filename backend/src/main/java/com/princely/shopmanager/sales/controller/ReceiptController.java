package com.princely.shopmanager.sales.controller;

import com.princely.shopmanager.sales.domain.Receipt;
import com.princely.shopmanager.sales.service.ReceiptService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for receipt management operations.
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final ReceiptService receiptService;

    @GetMapping
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_LIST)")
    public ResponseEntity<Page<Receipt>> listReceipts(
            @RequestParam(required = false) String shopId,
            @PageableDefault(size = 20, sort = "generatedAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Page<Receipt> receipts = receiptService.findAllReceipts(shopId, pageable, principal);
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/{receiptId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_READ)")
    public ResponseEntity<Receipt> getReceipt(
            @PathVariable String receiptId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Optional<Receipt> receipt = receiptService.getReceipt(receiptId, principal);
        return receipt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-number/{receiptNumber}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_READ)")
    public ResponseEntity<Receipt> getReceiptByNumber(
            @PathVariable String receiptNumber,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Optional<Receipt> receipt = receiptService.getReceiptByNumber(receiptNumber, principal);
        return receipt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_READ)")
    public ResponseEntity<Receipt> getReceiptByTransaction(
            @PathVariable String transactionId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Optional<Receipt> receipt = receiptService.getReceipt(transactionId, principal);
        return receipt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{receiptId}/content")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_READ)")
    public ResponseEntity<String> getReceiptContent(
            @PathVariable String receiptId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Optional<Receipt> receipt = receiptService.getReceipt(receiptId, principal);

        if (receipt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"receipt-" + receipt.get().getReceiptNumber() + ".txt\"");

        return ResponseEntity.ok()
            .headers(headers)
            .body(receipt.get().getReceiptContent());
    }

    @GetMapping("/{receiptId}/printable")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_READ)")
    public ResponseEntity<String> getPrintableContent(
            @PathVariable String receiptId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Optional<Receipt> receipt = receiptService.getReceipt(receiptId, principal);

        if (receipt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.add("X-Print-Format", "text/plain");

        return ResponseEntity.ok()
            .headers(headers)
            .body(receipt.get().getPrintableContent());
    }

    @PostMapping("/{receiptId}/mark-printed")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_CREATE)")
    public ResponseEntity<Receipt> markAsPrinted(
            @PathVariable String receiptId,
            @RequestParam String printedBy,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Receipt receipt = receiptService.markAsPrinted(receiptId, printedBy, principal);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/{receiptId}/mark-emailed")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_EMAIL)")
    public ResponseEntity<Receipt> markAsEmailed(
            @PathVariable String receiptId,
            @RequestParam String emailAddress,
            @AuthenticationPrincipal JwtPrincipal principal) {
        Receipt receipt = receiptService.markAsEmailed(receiptId, emailAddress, principal);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/regenerate/{transactionId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).RECEIPT_CREATE)")
    public ResponseEntity<Void> regenerateReceipt(
            @PathVariable String transactionId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        receiptService.regenerateReceipt(transactionId, principal);
        return ResponseEntity.ok().build();
    }
}