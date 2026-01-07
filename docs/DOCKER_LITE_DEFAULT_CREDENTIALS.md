# Docker Lite - Default Credentials

**⚠️ SECURITY WARNING**: Change these passwords immediately after first login!

## Default User Accounts

The following accounts are automatically created when you first start Docker Lite:

### System Administrator
- **Username**: `superadmin`
- **Password**: `changeme`
- **Role**: SYSTEM_ADMIN
- **Permissions**: 108 (Full system access)
- **Description**: Complete control over all tenants, shops, and system features

### Tenant Administrator
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: TENANT_ADMIN
- **Permissions**: 86 (Tenant-level access)
- **Description**: Full access to tenant resources (shops, users, products, sales, inventory, investments)

## First Login Steps

1. Navigate to `http://localhost/login` (or your configured domain)
2. Login with either account above
3. **IMMEDIATELY** change your password:
   - Click your profile in the top-right corner
   - Select "Profile" or "Settings"
   - Change password in the Security section

## Customizing Default Credentials

You can customize the default credentials by setting environment variables in `.env.lite`:

```ini
# Super Admin Configuration
app.bootstrap.superadmin.enabled=true
app.bootstrap.superadmin.username=superadmin
app.bootstrap.superadmin.email=superadmin@shopmanager.local
app.bootstrap.superadmin.password=changeme

# Tenant Admin Configuration
app.bootstrap.tenantadmin.enabled=true
app.bootstrap.tenantadmin.username=admin
app.bootstrap.tenantadmin.email=admin@shopmanager.local
app.bootstrap.tenantadmin.password=admin123
```

## Production Deployment

For production deployments:

1. **Set strong passwords** in `.env.lite` BEFORE first startup
2. Generate secure random passwords:
   ```bash
   # Generate a strong password
   openssl rand -base64 32
   ```
3. Use password managers to store and share credentials securely
4. Enable password expiry policies (if available in system settings)
5. Implement MFA/2FA when available

## Disabling Bootstrap Users

If you want to disable automatic user creation (e.g., after initial setup):

```ini
# In .env.lite or application properties
app.bootstrap.superadmin.enabled=false
app.bootstrap.tenantadmin.enabled=false
```

**Note**: This only prevents creation of NEW users. Existing users remain active.

## Account Capabilities Comparison

| Feature | System Admin | Tenant Admin |
|---------|-------------|--------------|
| Manage all tenants | ✅ | ❌ |
| Manage tenant shops | ✅ | ✅ |
| Manage tenant users | ✅ | ✅ |
| Products & Inventory | ✅ | ✅ |
| Sales & POS | ✅ | ✅ |
| Investments | ✅ | ✅ |
| Analytics | ✅ | ✅ |
| System configuration | ✅ | ❌ |
| Cross-tenant access | ✅ | ❌ |

## Security Best Practices

1. **Change default passwords immediately** after first deployment
2. **Never expose these credentials** in public repositories or documentation
3. **Use strong, unique passwords** (minimum 12 characters, mixed case, numbers, symbols)
4. **Rotate passwords regularly** (every 90 days recommended)
5. **Limit superadmin access** to only necessary personnel
6. **Audit user activity** regularly through audit logs
7. **Backup user database** before making any user/role changes

## Troubleshooting

### Cannot login with default credentials

1. Check if users were created:
   ```bash
   docker exec retailhq-backend-lite grep "Bootstrap.*admin" /app/logs/spring.log
   ```

2. Verify users in database:
   ```bash
   docker exec retailhq-postgres-lite psql -U shopmanager -d shopmanager -c \
     "SELECT username, email FROM users WHERE username IN ('superadmin', 'admin');"
   ```

3. Check backend logs:
   ```bash
   docker logs retailhq-backend-lite | grep -i "bootstrap"
   ```

### Reset forgotten password

As the superadmin cannot be reset from UI, you'll need to reset via database:

```bash
# Generate new password hash
docker exec retailhq-backend-lite java -cp app.jar \
  org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder "new-password"

# Update user password (replace $2a$10$... with generated hash)
docker exec retailhq-postgres-lite psql -U shopmanager -d shopmanager -c \
  "UPDATE users SET password_hash='$2a$10$...' WHERE username='superadmin';"
```

## Related Documentation

- [Docker Lite Deployment Guide](./DOCKER_LITE_DEPLOYMENT.md)
- [User Management Guide](./USER_MANAGEMENT.md)
- [Security Configuration](./SECURITY_CONFIGURATION.md)
- [Role-Based Access Control](./SHOP_ACCESS_CONTROL.md)
