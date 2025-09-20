package com.princely.shopmanager.sales.controller;

import com.princely.shopmanager.sales.domain.Receipt;
import com.princely.shopmanager.sales.service.ReceiptService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Slf4j
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/generate/{transactionId}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<Receipt> generateReceipt(@PathVariable String transactionId) {

        Receipt receipt = receiptService.generateReceipt(transactionId);
        return ResponseEntity.ok(receipt);
    }

    @GetMapping("/{receiptId}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<Receipt> getReceipt(@PathVariable String receiptId) {
        Optional<Receipt> receipt = receiptService.getReceipt(receiptId);
        return receipt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-number/{receiptNumber}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<Receipt> getReceiptByNumber(@PathVariable String receiptNumber) {
        Optional<Receipt> receipt = receiptService.getReceiptByNumber(receiptNumber);
        return receipt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<Receipt> getReceiptByTransaction(@PathVariable String transactionId) {
        Optional<Receipt> receipt = receiptService.getReceipt(transactionId);
        return receipt.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{receiptId}/content")
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<String> getReceiptContent(@PathVariable String receiptId) {
        Optional<Receipt> receipt = receiptService.getReceipt(receiptId);

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
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<String> getPrintableContent(@PathVariable String receiptId) {
        Optional<Receipt> receipt = receiptService.getReceipt(receiptId);

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
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<Receipt> markAsPrinted(@PathVariable String receiptId,
                                               @RequestParam String printedBy) {
        Receipt receipt = receiptService.markAsPrinted(receiptId, printedBy);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/{receiptId}/mark-emailed")
    @PreAuthorize("hasRole('CASHIER') or hasRole('MANAGER')")
    public ResponseEntity<Receipt> markAsEmailed(@PathVariable String receiptId,
                                               @RequestParam String emailAddress) {
        Receipt receipt = receiptService.markAsEmailed(receiptId, emailAddress);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/regenerate/{transactionId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> regenerateReceipt(@PathVariable String transactionId) {
        receiptService.regenerateReceipt(transactionId);
        return ResponseEntity.ok().build();
    }
}