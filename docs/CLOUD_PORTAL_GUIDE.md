# Cloud Portal User Guide

**Version:** 1.1.0
**Last Updated:** January 2026

---

## Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
- [Tenant Settings](#tenant-settings)
- [Shop Management](#shop-management)
- [Audit Logs](#audit-logs)
- [API Key Management](#api-key-management)
- [Subscriptions & Billing](#subscriptions--billing)
- [User Roles & Permissions](#user-roles--permissions)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Overview

The **Shop Manager Cloud Portal** is a comprehensive multi-tenant management platform that enables organizations to:

- **Manage Tenant Settings**: Configure company information, contact details, timezone, and locale
- **Manage Multiple Shops**: Create, edit, activate, and deactivate retail locations
- **Track Audit Logs**: View comprehensive activity logs with filtering and export capabilities
- **Manage API Keys**: Generate, view, and revoke API keys for programmatic access
- **Monitor Subscriptions**: View subscription tier, status, and billing information
- **View Analytics**: Access tenant-wide and shop-level analytics

### Key Features

✅ **Multi-Tenant Architecture**: Complete data isolation between tenants
✅ **Shop-Level Access Control**: Role-based permissions for shop access
✅ **Comprehensive Audit Trail**: All actions logged with user, timestamp, and IP address
✅ **RESTful API**: 17 cloud API endpoints for integration
✅ **Responsive UI**: React-based portal with modern UX
✅ **Secure Authentication**: Keycloak SSO integration with JWT tokens

---

## Getting Started

### Accessing the Cloud Portal

1. **Navigate to the Cloud Portal**:
   ```
   https://your-domain.com/cloud
   ```

2. **Login with your credentials**:
   - Enter your username and password
   - Authenticate via Keycloak SSO
   - You'll be redirected to the cloud portal dashboard

3. **Navigation**:
   - **Settings**: Tenant configuration and preferences
   - **Shops**: Manage all retail locations
   - **Audit Logs**: View activity history
   - **API Keys**: Manage programmatic access
   - **Subscriptions**: View billing and plan information

### User Roles

| Role | Access Level | Capabilities |
|------|-------------|--------------|
| **SYSTEM_ADMIN** | All tenants, all shops | Full system access |
| **TENANT_ADMIN** | All shops in tenant | Manage tenant settings, all shops |
| **OWNER** | All shops in tenant | Manage shops, view audit logs |
| **INVESTOR** | All shops in tenant | View-only access to analytics |
| **MANAGER** | Assigned shop only | Manage assigned shop |
| **EMPLOYEE** | Assigned shop only | Limited access to assigned shop |

---

## Tenant Settings

The **Tenant Settings** page allows you to configure your organization's information and preferences.

### Accessing Tenant Settings

1. Click **Settings** in the navigation menu
2. You'll see four main sections:
   - Company Information
   - Contact Details
   - Timezone & Locale Settings
   - Subscription Information

### Company Information

Configure your organization's core details:

| Field | Description | Required |
|-------|-------------|----------|
| **Company Name** | Official business name | Yes |
| **Company Email** | Primary contact email | Yes |
| **Company Registration Number** | Business registration ID | No |
| **Tax ID / VAT Number** | Tax identification number | No |

**Example**:
```
Company Name: Acme Retail Corp
Company Email: admin@acmeretail.com
Registration Number: BN-123456
Tax ID: VAT-GB-987654321
```

### Contact Details

Set up your organization's contact information:

| Field | Description | Required |
|-------|-------------|----------|
| **Street Address** | Physical address | No |
| **City** | City name | No |
| **State/Province** | State or province | No |
| **Postal Code** | ZIP/postal code | No |
| **Country** | Country name | No |
| **Phone Number** | Contact phone | No |

**Example**:
```
Street Address: 123 Main Street
City: London
State/Province: Greater London
Postal Code: SW1A 1AA
Country: United Kingdom
Phone: +44 20 7946 0958
```

### Timezone & Locale Settings

Configure regional preferences:

| Setting | Options | Default |
|---------|---------|---------|
| **Timezone** | All IANA timezones | UTC |
| **Locale** | en-US, en-GB, fr-FR, de-DE, es-ES | en-US |

**Supported Timezones** (examples):
- `America/New_York` - Eastern Time (US & Canada)
- `America/Los_Angeles` - Pacific Time (US & Canada)
- `Europe/London` - British Time
- `Europe/Paris` - Central European Time
- `Asia/Tokyo` - Japan Standard Time
- `Australia/Sydney` - Australian Eastern Time

**Impact of Timezone**:
- All timestamps displayed in selected timezone
- Affects report generation times
- Used for scheduled tasks and notifications

### Subscription Information

View your current subscription details (read-only):

- **Subscription Tier**: FREE, BASIC, PRO, ENTERPRISE
- **Status**: ACTIVE, TRIAL, SUSPENDED, CANCELLED
- **Total Shops**: Number of shops in your tenant
- **Billing Cycle**: Monthly/Annual
- **Next Billing Date**: When next payment is due

**Note**: To upgrade or manage your subscription, contact your account manager or visit the Subscriptions page.

### Saving Changes

1. Make your changes in any section
2. Click **Save Changes** button
3. Wait for confirmation message: "Settings saved successfully"
4. Changes take effect immediately

### Canceling Changes

- Click **Cancel** to discard unsaved changes
- Form will reset to last saved values

---

## Shop Management

The **Shop Management** page allows you to create, edit, and manage all retail locations within your tenant.

### Viewing Shops

The shops table displays:

| Column | Description |
|--------|-------------|
| **Shop Name** | Name of the retail location |
| **Contact** | Email and phone number |
| **Location** | City and country |
| **Status** | ACTIVE or INACTIVE badge |
| **Last Updated** | Last modification timestamp |
| **Actions** | Edit and Activate/Deactivate buttons |

### Filtering and Search

**Search by Name**:
```
Type in the search box to filter shops by name
Example: "Downtown" will show "Downtown Store", "Downtown Mall", etc.
```

**Filter by Status**:
- **All Shops**: Show all shops (default)
- **Active**: Show only active shops
- **Inactive**: Show only inactive shops

**Shop Count Display**:
```
Shops (5 of 12)
      ↑     ↑
   Filtered Total
```

### Creating a New Shop

1. **Click "Add Shop" button**
2. **Fill in required fields**:

   | Field | Description | Required | Validation |
   |-------|-------------|----------|------------|
   | **Shop Name** | Store name | Yes | 1-100 characters |
   | **Shop Email** | Contact email | Yes | Valid email format |
   | **Street Address** | Physical address | No | Max 200 characters |
   | **City** | City name | No | Max 50 characters |
   | **State/Province** | State/province | No | Max 50 characters |
   | **Postal Code** | ZIP/postal code | No | Max 20 characters |
   | **Country** | Country name | No | Max 50 characters |
   | **Phone Number** | Contact phone | No | Max 20 characters |

3. **Click "Create Shop"**
4. **Confirmation**: "Shop created successfully"

**Example**:
```
Shop Name: Downtown Store
Shop Email: downtown@acmeretail.com
Street Address: 456 High Street
City: Manchester
Country: United Kingdom
Phone: +44 161 123 4567
```

### Editing an Existing Shop

1. **Click "Edit" button** on the shop row
2. **Update fields** as needed
3. **Click "Save Changes"**
4. **Confirmation**: "Shop updated successfully"

**Note**: You can edit all fields except the shop ID (auto-generated).

### Activating / Deactivating Shops

**To Deactivate a Shop**:
1. Click **"Deactivate"** button on an active shop
2. Shop status changes to INACTIVE
3. Inactive shops cannot process sales or inventory changes

**To Activate a Shop**:
1. Click **"Activate"** button on an inactive shop
2. Shop status changes to ACTIVE
3. Shop can now process transactions

**Use Cases for Deactivation**:
- Temporary closure (renovations, holidays)
- Seasonal locations
- Shops pending closure
- Testing and development

### Empty State

If no shops match your filters:
```
No shops found
No shops match your search criteria.
Try adjusting your filters or create a new shop.
```

### Pagination

- **Default Page Size**: 20 shops per page
- **Navigation**: Previous / Next buttons
- **Page Indicator**: "Page 1 of 3"
- **Total Count**: "Showing 1 to 20 of 45 shops"

---

## Audit Logs

The **Audit Logs** page provides a comprehensive activity trail of all actions performed in your tenant.

### Viewing Audit Logs

The audit logs table displays:

| Column | Description |
|--------|-------------|
| **Timestamp** | When the action occurred |
| **Action** | Type of action (CREATE, UPDATE, DELETE, etc.) |
| **Entity** | What was affected (SHOP, USER, PRODUCT, etc.) |
| **User** | Who performed the action |
| **IP Address** | Source IP address |
| **Details** | Additional context and changes |

### Action Types

| Action | Badge Color | Description |
|--------|------------|-------------|
| **CREATE** | Green | New entity created |
| **UPDATE** | Blue | Existing entity modified |
| **DELETE** | Red | Entity deleted |
| **LOGIN** | Gray | User authentication |
| **LOGOUT** | Gray | User session ended |
| **SYNC** | Purple | Data synchronization |

### Entity Types

- **TENANT**: Tenant settings changes
- **SHOP**: Shop creation, updates, activation
- **USER**: User account changes
- **PRODUCT**: Product catalog changes
- **INVENTORY**: Stock adjustments
- **SALE**: Sales transactions
- **EXPENSE**: Expense records
- **API_KEY**: API key management
- **SUBSCRIPTION**: Subscription changes

### Filtering Audit Logs

**Search**:
```
Type to search across all fields
Example: "john.doe" to find all actions by that user
```

**Filter by Action**:
- All Actions
- CREATE
- UPDATE
- DELETE
- LOGIN
- SYNC

**Filter by Entity Type**:
- All Entities
- SHOP
- USER
- PRODUCT
- INVENTORY
- SALE

**Filter by Date Range**:
- Last 24 Hours
- Last 7 Days
- Last 30 Days (default)
- Last 90 Days
- Custom Range

**Combining Filters**:
```
Example: Show all CREATE actions on SHOP entities in last 7 days
- Action: CREATE
- Entity Type: SHOP
- Date Range: Last 7 Days
```

### Exporting Audit Logs

1. **Apply filters** (optional)
2. **Click "Export CSV" button**
3. **Download starts** with filename: `audit-logs-YYYY-MM-DD.csv`

**CSV Format**:
```csv
Timestamp,Action,Entity Type,Entity ID,User,IP Address,Details
2026-01-04T10:30:00Z,CREATE,SHOP,shop-123,john.doe,192.168.1.100,"Created shop: Downtown Store"
```

**Use Cases for Export**:
- Compliance audits
- Security investigations
- Performance analysis
- Historical reporting

### Audit Log Details

**Timestamp**:
- Displayed in tenant's configured timezone
- Format: `Jan 4, 2026 10:30 AM`
- ISO 8601 format in CSV export

**User Information**:
- Username or email
- User's role displayed
- System actions shown as "SYSTEM"

**IP Address**:
- Source IP of the request
- `127.0.0.1` for local actions
- `INTERNAL` for system-triggered actions

**Details**:
- Before/after values for updates
- Entity name for creates/deletes
- Error messages for failed actions

### Pagination

- **Default Page Size**: 50 logs per page
- **Activity Log Count**: Shows filtered/total count
- **Navigation**: Previous / Next buttons
- **Infinite Scroll**: Available in UI (optional)

---

## API Key Management

The **API Keys** page allows you to generate and manage programmatic access to the Shop Manager APIs.

### Viewing API Keys

The API keys table displays:

| Column | Description |
|--------|-------------|
| **Name** | Friendly name for the key |
| **Key (masked)** | First/last 8 chars (e.g., `sk_test_1234...5678`) |
| **Permissions** | Scope of access |
| **Status** | ACTIVE or REVOKED |
| **Created** | Creation date |
| **Last Used** | Last usage timestamp |
| **Actions** | Revoke button |

### Creating an API Key

1. **Click "Generate API Key" button**
2. **Fill in details**:
   - **Name**: Descriptive name (e.g., "Mobile App - Production")
   - **Permissions**: Select scope (READ_ONLY, READ_WRITE, ADMIN)
   - **Expiry**: Optional expiration date

3. **Click "Generate"**
4. **Copy API Key**: Display once, cannot retrieve later
   ```
   sk_live_1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcd
   ```

5. **Store Securely**: Save in password manager or secrets vault

**Permission Scopes**:

| Scope | Capabilities |
|-------|-------------|
| **READ_ONLY** | View shops, products, analytics |
| **READ_WRITE** | Read + create/update shops, products |
| **ADMIN** | Full access including user management |

### Using API Keys

**HTTP Header**:
```bash
X-API-Key: sk_live_1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcd
```

**Example Request**:
```bash
curl -H "X-API-Key: YOUR_API_KEY" \
     https://api.shopmanager.com/api/cloud/tenants/my-tenant/shops
```

### Revoking an API Key

1. **Click "Revoke" button** on the API key row
2. **Confirm revocation**
3. **Key immediately invalidated**
4. **All requests with this key will fail with 401 Unauthorized**

**When to Revoke**:
- Key compromised or leaked
- Integration no longer needed
- Regular key rotation (recommended every 90 days)
- Employee departure

### API Key Security

**Best Practices**:
- ✅ Never commit API keys to version control
- ✅ Use environment variables for keys
- ✅ Rotate keys regularly (every 90 days)
- ✅ Use separate keys for dev/staging/prod
- ✅ Revoke unused keys immediately
- ✅ Monitor key usage in audit logs

**Key Prefixes**:
- `sk_test_`: Test/development keys
- `sk_live_`: Production keys

---

## Subscriptions & Billing

The **Subscriptions** page displays your current plan and billing information.

### Subscription Tiers

| Tier | Shops | Users | Storage | API Calls | Price |
|------|-------|-------|---------|-----------|-------|
| **FREE** | 1 | 2 | 1 GB | 1,000/month | $0 |
| **BASIC** | 5 | 10 | 10 GB | 10,000/month | $29/month |
| **PRO** | 20 | 50 | 50 GB | 50,000/month | $99/month |
| **ENTERPRISE** | Unlimited | Unlimited | Unlimited | Unlimited | Custom |

### Subscription Status

| Status | Description |
|--------|-------------|
| **ACTIVE** | Subscription active, all features available |
| **TRIAL** | Free trial period (14 days) |
| **SUSPENDED** | Payment failed, limited access |
| **CANCELLED** | Subscription cancelled, access ends on billing date |

### Viewing Subscription Details

**Current Plan**:
- Tier name and features
- Number of shops used / limit
- Number of users used / limit
- Storage used / limit

**Billing Information**:
- Billing cycle (Monthly/Annual)
- Next billing date
- Payment method (last 4 digits)
- Billing history

### Upgrading Your Subscription

**To Upgrade**:
1. Click **"Upgrade Plan"** button
2. Select new tier
3. Enter payment information
4. Confirm upgrade
5. Changes take effect immediately

**Pro-rated Billing**:
- Upgrade: Credit applied for unused time
- Downgrade: Refund issued for remaining time

### Billing History

View past invoices:
- Invoice date
- Amount paid
- Payment method
- Download PDF invoice

**Export Invoices**:
- Click "Export CSV" to download all invoices
- Useful for accounting and tax purposes

---

## User Roles & Permissions

### Cloud Portal Access by Role

| Feature | SYSTEM_ADMIN | TENANT_ADMIN | OWNER | INVESTOR | MANAGER | EMPLOYEE |
|---------|--------------|--------------|-------|----------|---------|----------|
| **Tenant Settings** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Shop Management** | ✅ | ✅ | ✅ | ❌ | Own shop only | ❌ |
| **Audit Logs** | ✅ | ✅ | ✅ | ✅ | Own shop only | ❌ |
| **API Keys** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Subscriptions** | ✅ | ✅ | View only | ❌ | ❌ | ❌ |
| **Analytics** | ✅ | ✅ | ✅ | ✅ | Own shop only | Own shop only |

### Shop-Level Access

**MANAGER and EMPLOYEE roles** only see:
- Their assigned shop in the shop list
- Audit logs for their shop only
- Analytics for their shop only

**Example**:
```
User: john.doe
Role: MANAGER
Assigned Shop: Downtown Store

Can see:
- Downtown Store in shop list
- Audit logs for Downtown Store actions
- Analytics for Downtown Store only

Cannot see:
- Other shops in the tenant
- Tenant-wide settings
- Other shops' audit logs
```

---

## Best Practices

### Tenant Settings

1. **Keep Contact Info Updated**
   - Review quarterly
   - Update after relocations
   - Ensure emergency contacts are current

2. **Choose Appropriate Timezone**
   - Match primary business location
   - Consider customer timezone for reports
   - Update for daylight saving changes

3. **Locale Settings**
   - Match primary market language
   - Affects date/number formatting
   - Important for multi-regional tenants

### Shop Management

1. **Naming Conventions**
   - Use clear, descriptive names
   - Include location: "Downtown Store", "Airport Branch"
   - Avoid generic names: "Store 1", "Branch A"

2. **Keep Contact Info Current**
   - Update when staff changes
   - Use shop-specific emails (not personal)
   - Test phone numbers periodically

3. **Use Deactivation Wisely**
   - Deactivate instead of delete for history retention
   - Reactivate seasonal locations as needed
   - Keep audit trail intact

### Audit Logs

1. **Regular Reviews**
   - Weekly review of critical actions
   - Monthly compliance audits
   - Investigate anomalies promptly

2. **Export for Compliance**
   - Monthly export for archives
   - Store in secure, immutable storage
   - Retain per regulatory requirements (typically 7 years)

3. **Use Filters Effectively**
   - Narrow down to specific investigations
   - Combine multiple filters for precision
   - Save common filter sets (feature coming soon)

### API Key Management

1. **Key Hygiene**
   - Rotate keys every 90 days
   - Use descriptive names: "Mobile App - Prod (2026-Q1)"
   - Delete unused keys immediately

2. **Least Privilege**
   - Grant minimum required permissions
   - Use READ_ONLY when possible
   - Separate keys for different services

3. **Monitoring**
   - Review "Last Used" regularly
   - Check audit logs for API key usage
   - Alert on suspicious patterns

---

## Troubleshooting

### Cannot Save Tenant Settings

**Symptom**: "Save Changes" button disabled or error on save

**Solutions**:
1. Check required fields (Company Name, Email) are filled
2. Verify email format is valid
3. Check for unsaved changes indicator
4. Refresh page and try again
5. Check browser console for errors

### Shop Not Appearing in List

**Symptom**: Created shop doesn't show in table

**Solutions**:
1. Check filter settings (All Shops selected?)
2. Clear search box
3. Refresh the page
4. Verify you have permission to see all shops
5. Check if shop was created in different tenant

### Audit Logs Not Loading

**Symptom**: Spinning loader or empty table

**Solutions**:
1. Check date range filter (too narrow?)
2. Clear all filters and retry
3. Check internet connection
4. Verify authentication (session may have expired)
5. Contact support if persistent

### Cannot Generate API Key

**Symptom**: "Generate API Key" button disabled or fails

**Solutions**:
1. Verify you have TENANT_ADMIN or SYSTEM_ADMIN role
2. Check subscription limits (max keys reached?)
3. Ensure unique key name
4. Refresh page and retry
5. Check browser console for errors

### API Key Not Working

**Symptom**: 401 Unauthorized when using API key

**Solutions**:
1. Verify key status is ACTIVE (not REVOKED)
2. Check key has required permissions for endpoint
3. Verify header format: `X-API-Key: YOUR_KEY`
4. Ensure no extra spaces or line breaks in key
5. Try generating a new key

### Performance Issues

**Symptom**: Slow page loads or timeouts

**Solutions**:
1. **Audit Logs**: Narrow date range (30 days max)
2. **Shop List**: Use pagination (20 per page)
3. **API Keys**: Limit to active keys only
4. Clear browser cache
5. Check network connection
6. Try different browser

---

## Support

For additional help:

- **Documentation**: [https://docs.shopmanager.com](https://docs.shopmanager.com)
- **API Reference**: [CLOUD_API_REFERENCE.md](./CLOUD_API_REFERENCE.md)
- **Developer Guide**: [CLOUD_DEVELOPMENT_GUIDE.md](./CLOUD_DEVELOPMENT_GUIDE.md)
- **Support Email**: support@shopmanager.com
- **Community Forum**: [https://community.shopmanager.com](https://community.shopmanager.com)

---

**Last Updated**: January 2026
**Version**: 1.1.0
**Maintained by**: Shop Manager Team
