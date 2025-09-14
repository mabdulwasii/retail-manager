package com.princely.shopmanager.auth.context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserName = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
        log.debug("Setting current tenant to: {}", tenant);
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static String getCurrentTenantId() {
        return getCurrentTenant();
    }

    public static void setCurrentTenantId(String tenantId) {
        setCurrentTenant(tenantId);
    }

    public static void setCurrentUser(String userId, String userName) {
        log.debug("Setting current user to: {} ({})", userName, userId);
        currentUserId.set(userId);
        currentUserName.set(userName);
    }

    public static String getCurrentUserId() {
        return currentUserId.get();
    }

    public static String getCurrentUserName() {
        return currentUserName.get();
    }

    public static void clear() {
        log.debug("Clearing tenant context");
        currentTenant.remove();
        currentUserId.remove();
        currentUserName.remove();
    }

    public static boolean hasTenant() {
        return getCurrentTenant() != null;
    }

    public static void requireTenant() {
        if (!hasTenant()) {
            throw new IllegalStateException("No tenant context available");
        }
    }

    public static String requireCurrentTenant() {
        String tenant = getCurrentTenant();
        if (tenant == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return tenant;
    }
}