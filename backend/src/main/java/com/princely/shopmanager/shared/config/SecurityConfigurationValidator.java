package com.princely.shopmanager.shared.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Security configuration validator that checks for weak passwords and insecure configurations
 * during application startup.
 * 
 * This validator helps prevent deployment of applications with weak security configurations
 * by performing comprehensive security checks and logging warnings or errors for issues found.
 * 
 * Features:
 * - Password strength validation for all services
 * - Production environment security checks
 * - Comprehensive logging of security issues
 * - Configurable validation rules
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfigurationValidator {

    private final Environment environment;

    @Value("${spring.profiles.active:default}")
    private String activeProfiles;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${app.swagger.security.password:}")
    private String swaggerPassword;

    // Weak password patterns to check against
    private static final List<String> WEAK_PASSWORDS = Arrays.asList(
        "admin", "admin123", "password", "123456", "shop", "shop123",
        "minioadmin", "sonar", "postgres", "keycloak", "manager123",
        "employee123", "customer123", "investor123"
    );

    // Password strength requirements
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{"
        + MIN_PASSWORD_LENGTH + ",}$"
    );

    /**
     * Validates security configuration during application startup.
     * Performs comprehensive security checks and logs warnings for any issues found.
     */
    @PostConstruct
    public void validateSecurityConfiguration() {
        log.info("🔍 Starting security configuration validation...");
        
        List<String> securityIssues = new ArrayList<>();
        List<String> securityWarnings = new ArrayList<>();
        
        boolean isProduction = isProductionEnvironment();
        
        // Validate database password
        validateDatasourcePassword(securityIssues, securityWarnings, isProduction);
        
        // Validate Swagger password
        validateSwaggerPassword(securityIssues, securityWarnings, isProduction);
        
        // Validate environment variables
        validateEnvironmentVariables(securityIssues, securityWarnings, isProduction);
        
        // Report findings
        reportSecurityValidationResults(securityIssues, securityWarnings, isProduction);
    }

    /**
     * Validates the datasource password strength.
     */
    private void validateDatasourcePassword(List<String> issues, List<String> warnings, boolean isProduction) {
        if (datasourcePassword == null || datasourcePassword.trim().isEmpty()) {
            issues.add("DATABASE: No password configured for datasource");
            return;
        }

        if (isWeakPassword(datasourcePassword)) {
            String message = "DATABASE: Weak password detected - consider using a stronger password";
            if (isProduction) {
                issues.add(message + " (CRITICAL in production)");
            } else {
                warnings.add(message);
            }
        } else if (!isStrongPassword(datasourcePassword)) {
            String message = "DATABASE: Password doesn't meet strength requirements (12+ chars, mixed case, numbers, symbols)";
            if (isProduction) {
                issues.add(message);
            } else {
                warnings.add(message);
            }
        }
    }

    /**
     * Validates the Swagger security password strength.
     */
    private void validateSwaggerPassword(List<String> issues, List<String> warnings, boolean isProduction) {
        if (swaggerPassword == null || swaggerPassword.trim().isEmpty()) {
            warnings.add("SWAGGER: No password configured for API documentation access");
            return;
        }

        if (isWeakPassword(swaggerPassword)) {
            String message = "SWAGGER: Weak password detected for API documentation";
            if (isProduction) {
                issues.add(message + " (CRITICAL in production)");
            } else {
                warnings.add(message);
            }
        }
    }

    /**
     * Validates environment variables for common security issues.
     */
    private void validateEnvironmentVariables(List<String> issues, List<String> warnings, boolean isProduction) {
        // Check for common weak environment variables
        checkEnvironmentVariable("POSTGRES_PASSWORD", issues, warnings, isProduction);
        checkEnvironmentVariable("KEYCLOAK_ADMIN_PASSWORD", issues, warnings, isProduction);
        checkEnvironmentVariable("MINIO_ROOT_PASSWORD", issues, warnings, isProduction);
        checkEnvironmentVariable("SONAR_DB_PASSWORD", issues, warnings, isProduction);
        
        // Check if .env file might be in use (development indicator)
        if (isProduction && System.getProperty("user.dir") != null) {
            warnings.add("PRODUCTION: Ensure no .env files are deployed to production environment");
        }
    }

    /**
     * Checks a specific environment variable for weak passwords.
     */
    private void checkEnvironmentVariable(String varName, List<String> issues, List<String> warnings, boolean isProduction) {
        String value = environment.getProperty(varName);
        if (value != null && isWeakPassword(value)) {
            String message = varName + ": Weak password detected in environment variable";
            if (isProduction) {
                issues.add(message + " (CRITICAL in production)");
            } else {
                warnings.add(message);
            }
        }
    }

    /**
     * Reports the security validation results.
     */
    private void reportSecurityValidationResults(List<String> issues, List<String> warnings, boolean isProduction) {
        log.info("🔒 Security validation completed for {} environment", isProduction ? "PRODUCTION" : "DEVELOPMENT");
        
        if (!issues.isEmpty()) {
            log.error("🚨 SECURITY ISSUES FOUND ({} critical issues):", issues.size());
            issues.forEach(issue -> log.error("  ❌ {}", issue));
            
            if (isProduction) {
                log.error("🔴 PRODUCTION DEPLOYMENT WITH SECURITY ISSUES IS NOT RECOMMENDED!");
                log.error("🔴 Please fix all security issues before deploying to production.");
            }
        }
        
        if (!warnings.isEmpty()) {
            log.warn("⚠️ SECURITY WARNINGS ({} warnings):", warnings.size());
            warnings.forEach(warning -> log.warn("  ⚠️ {}", warning));
        }
        
        if (issues.isEmpty() && warnings.isEmpty()) {
            log.info("✅ No critical security issues detected");
        }
        
        // Log security recommendations
        logSecurityRecommendations(isProduction);
    }

    /**
     * Logs security recommendations based on environment.
     */
    private void logSecurityRecommendations(boolean isProduction) {
        if (isProduction) {
            log.info("🛡️ Production Security Recommendations:");
            log.info("   • Use strong, unique passwords (16+ characters)");
            log.info("   • Rotate passwords quarterly");
            log.info("   • Use environment variables for all credentials");
            log.info("   • Enable SSL/TLS for all database connections");
            log.info("   • Monitor authentication attempts and failures");
            log.info("   • Implement network isolation between services");
        } else {
            log.info("🔧 Development Security Tips:");
            log.info("   • Copy .env.example to .env and customize passwords");
            log.info("   • Never commit .env files to version control");
            log.info("   • Use different passwords for staging/production");
            log.info("   • Test with realistic password complexity");
        }
    }

    /**
     * Checks if the current environment is production.
     */
    private boolean isProductionEnvironment() {
        return activeProfiles != null && 
               (activeProfiles.contains("prod") || 
                activeProfiles.contains("production") ||
                "production".equalsIgnoreCase(activeProfiles));
    }

    /**
     * Checks if a password is considered weak (in the known weak passwords list).
     */
    private boolean isWeakPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return true;
        }
        
        String lowerPassword = password.toLowerCase();
        return WEAK_PASSWORDS.stream()
                .anyMatch(weak -> lowerPassword.contains(weak.toLowerCase()));
    }

    /**
     * Checks if a password meets strong password requirements.
     */
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }
}