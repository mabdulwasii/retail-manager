#!/bin/bash
set -e

echo "Configuring Keycloak realm for Shop Manager..."

# Keycloak admin credentials
ADMIN_USER="admin"
ADMIN_PASSWORD="KeycloakAdm1n@2024!SecureAuth"
KEYCLOAK_URL="https://localhost"
HOST_HEADER="auth.shop-manager.local"

echo "Step 1: Getting admin access token..."

# Get admin token
TOKEN_RESPONSE=$(curl -k -H "Host: $HOST_HEADER" -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    --data-urlencode "username=$ADMIN_USER" \
    --data-urlencode "password=$ADMIN_PASSWORD" \
    --data-urlencode "grant_type=password" \
    --data-urlencode "client_id=admin-cli")

if [ $? -ne 0 ]; then
    echo "Failed to connect to Keycloak"
    exit 1
fi

# Extract token using grep and sed (avoiding jq dependency)
TOKEN=$(echo "$TOKEN_RESPONSE" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ -z "$TOKEN" ]; then
    echo "Failed to get admin token"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "✓ Got admin token successfully"

echo "Step 2: Checking if shop-manager realm exists..."

# Check if realm exists
REALM_CHECK=$(curl -k -H "Host: $HOST_HEADER" -s -o /dev/null -w "%{http_code}" \
    -H "Authorization: Bearer $TOKEN" \
    "$KEYCLOAK_URL/admin/realms/shop-manager")

if [ "$REALM_CHECK" = "200" ]; then
    echo "✓ shop-manager realm already exists"
else
    echo "Step 3: Creating shop-manager realm..."

    # Create realm
    REALM_RESPONSE=$(curl -k -H "Host: $HOST_HEADER" -s -X POST "$KEYCLOAK_URL/admin/realms" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "realm": "shop-manager",
            "enabled": true,
            "displayName": "Shop Manager",
            "registrationAllowed": false,
            "rememberMe": true,
            "verifyEmail": false,
            "loginWithEmailAllowed": true,
            "resetPasswordAllowed": true,
            "bruteForceProtected": true,
            "sslRequired": "external"
        }')

    echo "✓ shop-manager realm created"
fi

echo "Step 4: Verifying realm accessibility..."

# Verify realm
REALM_VERIFY=$(curl -k -H "Host: $HOST_HEADER" -s "$KEYCLOAK_URL/realms/shop-manager/.well-known/openid-configuration" | grep -o '"issuer":"[^"]*"' | cut -d'"' -f4)

if [[ "$REALM_VERIFY" == *"shop-manager"* ]]; then
    echo "✓ Realm verification successful"
    echo "✓ Keycloak shop-manager realm is ready!"
    echo ""
    echo "Next steps:"
    echo "1. Access Keycloak admin: https://auth.shop-manager.local"
    echo "2. Login with: admin / KeycloakAdm1n@2024!SecureAuth"
    echo "3. Configure shop-manager realm clients and users"
    echo ""
else
    echo "✗ Realm verification failed"
    exit 1
fi

echo "Keycloak configuration completed successfully!"