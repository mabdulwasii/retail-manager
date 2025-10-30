package com.princely.shopmanager.shared.constants;

/**
 * Constants for permission actions following CRUD pattern.
 * Used in combination with resources to define granular permissions.
 */
public final class ActionConstants {

    private ActionConstants() {
        // Prevent instantiation
    }

    /**
     * Create action - ability to create new resources
     */
    public static final String CREATE = "CREATE";

    /**
     * Read action - ability to view a single resource by ID
     */
    public static final String READ = "READ";

    /**
     * List action - ability to list/search multiple resources
     */
    public static final String LIST = "LIST";

    /**
     * Update action - ability to modify existing resources
     */
    public static final String UPDATE = "UPDATE";

    /**
     * Delete action - ability to remove resources
     */
    public static final String DELETE = "DELETE";

    /**
     * View action - special read-only access (used for scoped viewing like audit logs)
     */
    public static final String VIEW = "VIEW";

    /**
     * Manage action - combined permission for full resource management
     * (typically includes CREATE, READ, LIST, UPDATE, DELETE)
     */
    public static final String MANAGE = "MANAGE";

    /**
     * Admin action - full administrative access to a resource
     */
    public static final String ADMIN = "ADMIN";

    /**
     * Send action - ability to send/transmit resources (e.g., receipts, emails)
     */
    public static final String SEND = "SEND";

    /**
     * View Shop action - scoped viewing at shop level
     */
    public static final String VIEW_SHOP = "VIEW_SHOP";

    /**
     * View Tenant action - scoped viewing at tenant level
     */
    public static final String VIEW_TENANT = "VIEW_TENANT";
}
