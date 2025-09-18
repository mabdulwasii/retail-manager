package com.princely.shopmanager.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopConfiguration {

    @Column(name = "investment_enabled")
    private boolean investmentEnabled = true;

    @Column(name = "analytics_enabled")
    private boolean analyticsEnabled = true;

    @Column(name = "fraud_detection_enabled")
    private boolean fraudDetectionEnabled = false;

    @Column(name = "auto_backup_enabled")
    private boolean autoBackupEnabled = true;

    @Column(name = "currency")
    private String currency = "NGN";

    @Column(name = "tax_rate")
    private Double taxRate = 0.0;

    @Column(name = "max_discount_percentage")
    private Double maxDiscountPercentage = 20.0;

    @Column(name = "receipt_footer")
    private String receiptFooter;
}