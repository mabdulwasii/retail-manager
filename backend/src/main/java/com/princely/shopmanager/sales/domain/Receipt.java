package com.princely.shopmanager.sales.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import com.princely.shopmanager.shared.domain.ShopAware;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "transaction")
@EqualsAndHashCode(callSuper = true, exclude = "transaction")
public class Receipt extends BaseEntity implements ShopAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "receipt_number", unique = true, nullable = false)
    private String receiptNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private SalesTransaction transaction;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Builder.Default
    @Column(name = "printed_count")
    private Integer printedCount = 0;

    @Column(name = "last_printed_at")
    private LocalDateTime lastPrintedAt;

    @Builder.Default
    @Column(name = "email_sent")
    private boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Builder.Default
    @Column(name = "sms_sent")
    private boolean smsSent = false;

    @Column(name = "sms_sent_at")
    private LocalDateTime smsSentAt;

    @Column(name = "receipt_content", columnDefinition = "TEXT")
    private String receiptContent;

    @Column(name = "printable_content", columnDefinition = "TEXT")
    private String printableContent;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private ReceiptFormat format = ReceiptFormat.TEXT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReceiptStatus status = ReceiptStatus.GENERATED;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "printed_at")
    private LocalDateTime printedAt;

    @Column(name = "printed_by")
    private String printedBy;

    @Column(name = "emailed_at")
    private LocalDateTime emailedAt;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "signature_url")
    private String signatureUrl;

    public enum ReceiptFormat {
        TEXT,
        PDF,
        HTML,
        JSON
    }

    public enum ReceiptStatus {
        GENERATED,
        PRINTED,
        EMAILED,
        CANCELLED,
        VOIDED
    }

    public void incrementPrintCount() {
        this.printedCount++;
        this.lastPrintedAt = LocalDateTime.now();
    }

    /**
     * Returns the shop ID this receipt belongs to (via its sales transaction).
     * Required by {@link ShopAware} interface for shop-level access control.
     *
     * @return shop ID, or null if transaction is not loaded
     */
    @Override
    public String getShopId() {
        return transaction != null ? transaction.getShopId() : null;
    }
}