# Keycloak Configuration for Shop Manager

## Overview

Shop Manager requires specific JWT claims from Keycloak for proper user synchronization and authorization. This document explains how to configure Keycloak to provide the required claims.

---

## Required JWT Claims

### 1. `sub` (Subject) - **CRITICAL**

- **Purpose:** Unique Keycloak user ID
- **Used for:** Database user lookup by `keycloak_id`
- **Requirement:** MANDATORY
- **Type:** String (UUID)
- **Example:** `"sub": "b70258a4-eb17-489f-b804-cd209531ac9b"`

**Why it's critical:**
- Primary key for linking Keycloak users to database records
- Used by `UserController.getCurrentUserProfile()` to retrieve user data
- Immutable - never changes even if user details are updated

**How to configure:**

1. Navigate to **Keycloak Admin Console**
2. Go to **Realm Settings → Clients → retail-frontend**
3. Click **Client Scopes** tab
4. Click **retail-frontend-dedicated** (or your dedicated scope)
5. Go to **Mappers** tab
6. Click **Add builtin**
7. Find and select **`sub`** mapper
8. Click **Add**
9. Verify mapper settings:
   - **Token Claim Name:** sub
   - **Add to ID token:** ON
   - **Add to access token:** ON
   - **Add to userinfo:** ON

**Troubleshooting:**
- If `sub` claim is missing, check that the mapper is enabled
- Ensure the mapper is assigned to the client scope
- Verify client scope is assigned to the client

---

### 2. `tenant_id` - **REQUIRED**

- **Purpose:** Identifies which tenant the user belongs to
- **Used for:** Multi-tenant data isolation
- **Requirement:** REQUIRED
- **Type:** String
- **Example:** `"tenant_id": "default-tenant-id"`

**How to configure:**

#### Step 1: Set User Attribute
1. Navigate to **Users** → Select user
2. Go to **Attributes** tab
3. Click **Add attribute**
4. **Key:** `tenant_id`
5. **Value:** `default-tenant-id` (or your tenant UUID)
6. Click **Save**

#### Step 2: Create Mapper
1. Navigate to **Clients → retail-frontend → Client Scopes**
2. Click **retail-frontend-dedicated**
3. Go to **Mappers** tab
4. Click **Create**
5. Configure mapper:
   - **Name:** tenant_id
   - **Mapper Type:** User Attribute
   - **User Attribute:** tenant_id
   - **Token Claim Name:** tenant_id
   - **Claim JSON Type:** String
   - **Add to ID token:** ON
   - **Add to access token:** ON
   - **Add to userinfo:** ON
6. Click **Save**

---

### 3. `shop_id` - **RECOMMENDED**

- **Purpose:** Assigns users to specific shops on first login
- **Used for:** Auto-populating `shop_id` in database
- **Requirement:** RECOMMENDED (optional for tenant-level users)
- **Type:** String
- **Example:** `"shop_id": "default-shop-id"`

**How to configure:**

#### Step 1: Set User Attribute
1. Navigate to **Users** → Select user
2. Go to **Attributes** tab
3. Click **Add attribute**
4. **Key:** `shop_id`
5. **Value:** `default-shop-id` (or your shop UUID)
6. Click **Save**

#### Step 2: Create Mapper
1. Navigate to **Clients → retail-frontend → Client Scopes**
2. Click **retail-frontend-dedicated**
3. Go to **Mappers** tab
4. Click **Create**
5. Configure mapper:
   - **Name:** shop_id
   - **Mapper Type:** User Attribute
   - **User Attribute:** shop_id
   - **Token Claim Name:** shop_id
   - **Claim JSON Type:** String
   - **Add to ID token:** ON
   - **Add to access token:** ON
   - **Add to userinfo:** ON
6. Click **Save**

---

### 4. Standard Claims - **REQUIRED**

These claims are typically included by default in Keycloak:

| Claim | Purpose | Example |
|-------|---------|---------|
| `email` | User email address | `"email": "user@example.com"` |
| `email_verified` | Email verification status | `"email_verified": true` |
| `preferred_username` | Username | `"preferred_username": "johndoe"` |
| `given_name` | First name | `"given_name": "John"` |
| `family_name` | Last name | `"family_name": "Doe"` |
| `name` | Full name | `"name": "John Doe"` |
| `realm_access.roles` | User roles | `"realm_access": {"roles": ["TENANT_ADMIN"]}` |

**Verify these claims exist** in your JWT by decoding it at [jwt.io](https://jwt.io).

---

## Testing JWT Configuration

### Step 1: Login and Extract JWT

1. Login to Shop Manager frontend
2. Open Browser DevTools (F12)
3. Go to **Application** → **Storage** → **Local Storage** or **Session Storage**
4. Find the access token (usually stored as `auth_token` or similar)
5. Copy the JWT token

### Step 2: Decode and Verify JWT

1. Go to [https://jwt.io](https://jwt.io)
2. Paste the JWT in the **Encoded** section
3. Review the **Decoded** payload

**Expected payload structure:**
```json
{
  "exp": 1762828001,
  "iat": 1762827702,
  "jti": "b685725b-05c5-44e9-bd41-f241f8ece818",
  "iss": "https://auth.retail.gomco.com/realms/retail",
  "aud": "account",
  "sub": "b70258a4-eb17-489f-b804-cd209531ac9b",  // ✅ MUST BE PRESENT
  "typ": "Bearer",
  "azp": "retail-frontend",
  "sid": "bbcc5661-4945-43d6-b74b-ab81a2034032",
  "tenant_id": "default-tenant-id",               // ✅ REQUIRED
  "shop_id": "default-shop-id",                    // ✅ RECOMMENDED
  "email_verified": false,
  "name": "John Doe",
  "preferred_username": "johndoe",
  "given_name": "John",
  "family_name": "Doe",
  "email": "john.doe@example.com",                // ✅ REQUIRED
  "realm_access": {
    "roles": [
      "TENANT_ADMIN",
      "MANAGER",
      "OWNER"
    ]
  }
}
```

### Step 3: Verify User Synchronization

1. After configuring Keycloak, logout all users
2. Clear browser cache and cookies
3. Login with test user
4. Call `GET /api/users/profile`
5. Verify response contains correct user data

---

## User Synchronization Flow

Understanding how Shop Manager syncs users from Keycloak:

```
┌─────────────────────────────────────────────────────────┐
│ 1. User Login → Keycloak Authentication                │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 2. Keycloak Issues JWT with Claims:                    │
│    - sub: Keycloak User ID                             │
│    - email: user@example.com                           │
│    - tenant_id, shop_id, roles                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 3. AuthenticationSuccessListener Detects Login         │
│    → Calls UserSyncService.syncUserFromKeycloak()      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 4. UserSyncService Checks Database:                    │
│    a) User exists by keycloak_id? → Update user        │
│    b) User not found? → Create new user                │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 5. Database User Record:                               │
│    - keycloak_id ← JWT "sub"                           │
│    - email ← JWT "email"                               │
│    - tenant_id ← JWT "tenant_id"                       │
│    - shop_id ← JWT "shop_id"                           │
│    - roles ← JWT "realm_access.roles"                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 6. Subsequent API Calls:                               │
│    → Use database for authorization                    │
│    → Permissions from database roles                   │
└─────────────────────────────────────────────────────────┘
```

**Key Points:**
- First login creates database record
- UserSyncService runs automatically (no manual action needed)
- Database is the source of truth for permissions
- JWT only used for initial sync and authentication

---

## Common Issues and Solutions

### Issue 1: Profile Returns Wrong User

**Symptoms:**
- Login with user A, but /profile returns user B's data
- Multiple users showing same profile

**Cause:**
- Missing `sub` claim in JWT
- Multiple users have NULL `keycloak_id` in database

**Solution:**
1. Add `sub` mapper to Keycloak (see above)
2. Logout all users
3. Login again → UserSyncService will fix keycloak_id
4. Verify: `SELECT email, keycloak_id FROM users;`

---

### Issue 2: Users Created Without Shop

**Symptoms:**
- User appears in database but `shop_id` is NULL
- User cannot access shop-specific resources

**Cause:**
- Missing `shop_id` in JWT or user attributes

**Solution:**
1. Set `shop_id` attribute in Keycloak user profile
2. Add `shop_id` mapper (see above)
3. User must logout and login again
4. UserSyncService will NOT update shop on existing users
5. Manual fix: `UPDATE users SET shop_id = 'shop-uuid' WHERE email = 'user@example.com';`

---

### Issue 3: "User not found in database"

**Symptoms:**
- Login succeeds but API calls fail with 500 error
- Logs show "User not found in database"

**Cause:**
- UserSyncService failed to create user
- Tenant doesn't exist in database

**Solution:**
1. Check logs for UserSyncService errors
2. Verify tenant exists: `SELECT * FROM tenants WHERE id = 'tenant-id';`
3. Create tenant if missing via migration or API
4. User must logout and login again

---

### Issue 4: Roles Not Syncing

**Symptoms:**
- User has roles in Keycloak but not in database
- Permission denied errors despite having roles in JWT

**Cause:**
- Role names in Keycloak don't match database role names
- Roles don't exist in database

**Solution:**
1. Check database: `SELECT name FROM roles WHERE is_system = true;`
2. Verify Keycloak role names match exactly (case-sensitive)
3. System roles in database:
   - SYSTEM_ADMIN
   - TENANT_ADMIN
   - OWNER
   - MANAGER
   - EMPLOYEE
   - INVESTOR
   - CASHIER
   - ACCOUNTANT
   - AUDITOR
   - CUSTOMER
4. Login again to re-sync roles

---

## Best Practices

### 1. User Attribute Management
- Set `tenant_id` and `shop_id` immediately when creating users in Keycloak
- Use consistent UUID formats for IDs
- Don't change user attributes manually after first login

### 2. Role Management
- Create system roles in database FIRST (via migrations)
- Then assign matching roles in Keycloak
- Keep role names consistent across Keycloak and database

### 3. Testing New Users
- Always decode JWT after first login to verify claims
- Check database to confirm user was synced correctly
- Test /profile endpoint before granting production access

### 4. Troubleshooting
- Enable DEBUG logging for `com.princely.shopmanager.auth.service.UserSyncService`
- Check Keycloak server logs for authentication errors
- Use browser DevTools Network tab to inspect API responses

---

## Configuration Checklist

Use this checklist when setting up a new Keycloak realm or client:

- [ ] Client `retail-frontend` created
- [ ] Client scope `retail-frontend-dedicated` created
- [ ] Mapper: `sub` → added to client scope
- [ ] Mapper: `tenant_id` → created and configured
- [ ] Mapper: `shop_id` → created and configured
- [ ] Standard profile mappers enabled (email, name, username)
- [ ] Realm roles created matching database roles
- [ ] Test user created with attributes: tenant_id, shop_id
- [ ] Test login and JWT decode verification
- [ ] Test /profile API call
- [ ] Verify database user record created with correct keycloak_id

---

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth2 / OIDC Standard Claims](https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims)
- [JWT.io - JWT Decoder](https://jwt.io)
- Shop Manager Developer Guide: `DEVELOPER_GUIDE.md`
- Permission Matrix: `PERMISSION_MATRIX.md`
