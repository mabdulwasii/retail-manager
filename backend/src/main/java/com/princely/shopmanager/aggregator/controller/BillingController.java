package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.aggregator.dto.InvoiceDto;
import com.princely.shopmanager.aggregator.service.CloudSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Billing Controller.
 * Manages invoices and billing history for cloud subscriptions.
 */
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/billing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing", description = "Manage invoices and billing history")
public class BillingController {

    private final CloudSubscriptionService subscriptionService;

    /**
     * Get all invoices for a tenant.
     *
     * GET /api/cloud/tenants/{tenantId}/billing/invoices
     *
     * @param tenantId Tenant ID
     * @return List of invoices
     */
    @GetMapping("/invoices")
    @Operation(summary = "List invoices",
            description = "Get all invoices for a tenant, ordered by issue date (newest first)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<List<InvoiceDto>> listInvoices(@PathVariable String tenantId) {
        log.info("Listing invoices for tenant: {}", tenantId);
        List<InvoiceDto> invoices = subscriptionService.getInvoices(tenantId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Get a specific invoice by ID.
     *
     * GET /api/cloud/tenants/{tenantId}/billing/invoices/{invoiceId}
     *
     * @param tenantId Tenant ID
     * @param invoiceId Invoice ID
     * @return Invoice details
     */
    @GetMapping("/invoices/{invoiceId}")
    @Operation(summary = "Get invoice",
            description = "Get details of a specific invoice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invoice"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<InvoiceDto> getInvoice(
            @PathVariable String tenantId,
            @PathVariable String invoiceId) {

        log.info("Getting invoice: {} for tenant: {}", invoiceId, tenantId);

        // TODO: Implement getInvoiceById in service
        // For now, return invoice from list
        List<InvoiceDto> invoices = subscriptionService.getInvoices(tenantId);
        InvoiceDto invoice = invoices.stream()
                .filter(inv -> inv.getId().equals(invoiceId))
                .findFirst()
                .orElse(null);

        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(invoice);
    }

    /**
     * Download invoice PDF.
     *
     * GET /api/cloud/tenants/{tenantId}/billing/invoices/{invoiceId}/pdf
     *
     * @param tenantId Tenant ID
     * @param invoiceId Invoice ID
     * @return PDF download URL or redirect
     */
    @GetMapping("/invoices/{invoiceId}/pdf")
    @Operation(summary = "Download invoice PDF",
            description = "Get download URL for invoice PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF URL retrieved"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    public ResponseEntity<PdfDownloadResponse> downloadInvoicePdf(
            @PathVariable String tenantId,
            @PathVariable String invoiceId) {

        log.info("Downloading PDF for invoice: {} (tenant: {})", invoiceId, tenantId);

        // TODO: Implement PDF generation
        // For now, return placeholder
        return ResponseEntity.ok(new PdfDownloadResponse(
                "https://cloud.shopmanager.com/invoices/" + invoiceId + ".pdf"
        ));
    }

    /**
     * Response DTO for PDF download.
     */
    public record PdfDownloadResponse(String downloadUrl) {
    }
}
