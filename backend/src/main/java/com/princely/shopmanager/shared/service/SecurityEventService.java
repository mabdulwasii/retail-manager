package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEventService {

    // Default value constant
    private static final String DEFAULT_UNKNOWN = "UNKNOWN";

    private final AuditService auditService;
    private final ShopRepository shopRepository;

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String userId = extractUserId(auth);
        String userName = extractUserName(auth);
        String ipAddress = getClientIpAddress();

        log.info("Successful authentication for user: {} from IP: {}", userName, ipAddress);

        auditService.logSecurityEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.ActionType.LOGIN,
            "Successful authentication",
            ipAddress,
            true
        );
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String userName = extractUserName(event.getAuthentication());
        String ipAddress = getClientIpAddress();

        log.warn("Failed authentication attempt for user: {} from IP: {}", userName, ipAddress);

        auditService.logSecurityEvent(
            getCurrentShop(),
            DEFAULT_UNKNOWN,
            userName,
            AuditLog.ActionType.LOGIN_FAILED,
            "Authentication failed: " + event.getException().getMessage(),
            ipAddress,
            false
        );
    }

    @EventListener
    public void handleAuthorizationDenied(AuthorizationDeniedEvent<?> event) {
        Authentication auth = event.getAuthentication().get();
        String userId = extractUserId(auth);
        String userName = extractUserName(auth);
        String ipAddress = getClientIpAddress();

        log.warn("Authorization denied for user: {} accessing: {} from IP: {}",
            userName, event.getAuthorizationResult(), ipAddress);

        auditService.logSecurityEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.ActionType.PERMISSION_DENIED,
            "Access denied to resource: " + event.getAuthorizationResult(),
            ipAddress,
            false
        );
    }

    public void logLogout(String userId, String userName) {
        String ipAddress = getClientIpAddress();

        log.info("User logout: {} from IP: {}", userName, ipAddress);

        auditService.logSecurityEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.ActionType.LOGOUT,
            "User logout",
            ipAddress,
            true
        );
    }

    public void logPasswordChange(String userId, String userName, boolean successful) {
        String ipAddress = getClientIpAddress();

        auditService.logSecurityEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.ActionType.PASSWORD_CHANGE,
            successful ? "Password changed successfully" : "Password change failed",
            ipAddress,
            successful
        );
    }

    public void logAccountLocked(String userId, String userName, String reason) {
        String ipAddress = getClientIpAddress();

        auditService.logSecurityEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.ActionType.ACCOUNT_LOCKED,
            "Account locked: " + reason,
            ipAddress,
            true
        );
    }

    public void logAccountUnlocked(String userId, String userName, String unlockedBy) {
        String ipAddress = getClientIpAddress();

        auditService.logSecurityEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.ActionType.ACCOUNT_UNLOCKED,
            "Account unlocked by: " + unlockedBy,
            ipAddress,
            true
        );
    }

    public void logSuspiciousActivity(String userId, String userName, String activity, String details) {
        String ipAddress = getClientIpAddress();

        auditService.logCustomEvent(
            getCurrentShop(),
            userId,
            userName,
            AuditLog.AuditCategory.SECURITY_EVENT,
            AuditLog.ActionType.CREATE, // Generic action for suspicious activity
            "SUSPICIOUS_ACTIVITY",
            null,
            "Suspicious activity detected: " + activity + ". Details: " + details,
            null,
            AuditLog.Severity.WARNING
        );
    }

    private String extractUserId(Authentication auth) {
        if (auth == null) return DEFAULT_UNKNOWN;

        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("sub");
        }

        return auth.getName();
    }

    private String extractUserName(Authentication auth) {
        if (auth == null) return DEFAULT_UNKNOWN;

        if (auth.getPrincipal() instanceof Jwt jwt) {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            return preferredUsername != null ? preferredUsername : auth.getName();
        }

        return auth.getName();
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            log.debug("Failed to extract client IP address: {}", e.getMessage());
            return DEFAULT_UNKNOWN;
        }
    }

    private Shop getCurrentShop() {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            if (tenantId != null) {
                return shopRepository.findById(tenantId).orElse(null);
            }
        } catch (Exception e) {
            log.debug("Failed to get current shop: {}", e.getMessage());
        }
        return null;
    }
}