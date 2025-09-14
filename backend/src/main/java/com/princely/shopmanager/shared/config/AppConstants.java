package com.princely.shopmanager.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Application-wide constants configured via environment variables.
 * All values can be overridden through environment variables or ConfigMaps.
 */
@Configuration
@Getter
public class AppConstants {

    // Pagination defaults
    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

    @Value("${app.pagination.max-page-size:100}")
    private int maxPageSize;

    // Cache settings
    @Value("${app.cache.default-ttl-minutes:60}")
    private int defaultCacheTtlMinutes;

    @Value("${app.cache.analytics-ttl-hours:1}")
    private int analyticsCacheTtlHours;

    // Security settings
    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.account-lock-duration-minutes:30}")
    private int accountLockDurationMinutes;

    @Value("${app.security.password-min-length:8}")
    private int passwordMinLength;

    @Value("${app.security.password-max-length:128}")
    private int passwordMaxLength;

    @Value("${app.security.session-timeout-minutes:30}")
    private int sessionTimeoutMinutes;

    @Value("${app.security.jwt-expiration-hours:24}")
    private int jwtExpirationHours;

    // Business rules
    @Value("${app.business.min-reorder-point:10}")
    private int minReorderPoint;

    @Value("${app.business.default-tax-rate:0.075}")
    private double defaultTaxRate;

    @Value("${app.business.max-return-days:30}")
    private int maxReturnDays;

    @Value("${app.business.min-stock-level:0}")
    private int minStockLevel;

    @Value("${app.business.max-stock-level:999999}")
    private int maxStockLevel;

    @Value("${app.business.default-currency:USD}")
    private String defaultCurrency;

    // Inventory settings
    @Value("${app.inventory.low-stock-threshold:10}")
    private int lowStockThreshold;

    @Value("${app.inventory.expiry-warning-days:30}")
    private int expiryWarningDays;

    @Value("${app.inventory.auto-reorder-enabled:false}")
    private boolean autoReorderEnabled;

    @Value("${app.inventory.stock-check-interval-hours:6}")
    private int stockCheckIntervalHours;

    // Backup settings
    @Value("${app.backup.retention-days:30}")
    private int backupRetentionDays;

    @Value("${app.backup.max-file-size-mb:500}")
    private int maxBackupFileSizeMb;

    @Value("${app.backup.compression-enabled:true}")
    private boolean backupCompressionEnabled;

    @Value("${app.backup.encryption-algorithm:AES256}")
    private String backupEncryptionAlgorithm;

    @Value("${app.backup.schedule-cron:0 0 2 * * SUN}")
    private String backupScheduleCron;

    // Notification settings
    @Value("${app.notification.email-enabled:true}")
    private boolean emailNotificationEnabled;

    @Value("${app.notification.sms-enabled:false}")
    private boolean smsNotificationEnabled;

    @Value("${app.notification.low-stock-alert-enabled:true}")
    private boolean lowStockAlertEnabled;

    // File upload settings
    @Value("${app.upload.max-file-size-mb:10}")
    private int maxUploadFileSizeMb;

    @Value("${app.upload.allowed-extensions:csv,xlsx,xls,pdf,jpg,jpeg,png}")
    private String allowedFileExtensions;

    @Value("${app.upload.temp-directory:/tmp/shop-manager/uploads}")
    private String uploadTempDirectory;

    // Audit settings
    @Value("${app.audit.retention-days:90}")
    private int auditRetentionDays;

    @Value("${app.audit.log-entity-changes:true}")
    private boolean logEntityChanges;

    @Value("${app.audit.log-security-events:true}")
    private boolean logSecurityEvents;

    @Value("${app.audit.log-api-requests:false}")
    private boolean logApiRequests;

    // Performance settings
    @Value("${app.performance.connection-pool-size:10}")
    private int connectionPoolSize;

    @Value("${app.performance.query-timeout-seconds:30}")
    private int queryTimeoutSeconds;

    @Value("${app.performance.bulk-batch-size:1000}")
    private int bulkBatchSize;

    // Report settings
    @Value("${app.reports.default-format:PDF}")
    private String defaultReportFormat;

    @Value("${app.reports.logo-path:classpath:static/logo.png}")
    private String reportLogoPath;

    @Value("${app.reports.generation-timeout-minutes:5}")
    private int reportGenerationTimeoutMinutes;

    // Integration settings
    @Value("${app.integration.retry-attempts:3}")
    private int integrationRetryAttempts;

    @Value("${app.integration.retry-delay-ms:1000}")
    private int integrationRetryDelayMs;

    @Value("${app.integration.timeout-seconds:30}")
    private int integrationTimeoutSeconds;

    // Feature flags defaults
    @Value("${app.features.multi-currency:false}")
    private boolean multiCurrencyEnabled;

    @Value("${app.features.barcode-scanning:true}")
    private boolean barcodeScanningEnabled;

    @Value("${app.features.loyalty-program:false}")
    private boolean loyaltyProgramEnabled;

    @Value("${app.features.promotional-pricing:true}")
    private boolean promotionalPricingEnabled;

    // Validation patterns
    @Value("${app.validation.email-pattern:^[A-Za-z0-9+_.-]+@(.+)$}")
    private String emailValidationPattern;

    @Value("${app.validation.phone-pattern:^\\+?[1-9]\\d{1,14}$}")
    private String phoneValidationPattern;

    @Value("${app.validation.tax-id-pattern:^[A-Z0-9-]+$}")
    private String taxIdValidationPattern;

    // System defaults
    @Value("${app.system.default-timezone:UTC}")
    private String defaultTimezone;

    @Value("${app.system.default-locale:en_US}")
    private String defaultLocale;

    @Value("${app.system.maintenance-mode:false}")
    private boolean maintenanceMode;

    @Value("${app.system.debug-mode:false}")
    private boolean debugMode;
}