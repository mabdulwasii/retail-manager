#!/bin/bash

# Keycloak realm initialization script for Docker Compose
set -e

echo "Waiting for Keycloak to be ready..."

# Wait for Keycloak to be available
until curl -f http://keycloak:8080/health/ready; do
  echo "Keycloak is not ready yet. Sleeping for 10 seconds..."
  sleep 10
done

echo "Keycloak is ready! Importing realm..."

# Get admin access token
ADMIN_TOKEN=$(curl -s -X POST \
  "http://keycloak:8080/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=Adm1n!SecureP@ss2024" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | \
  sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

echo "Admin token obtained successfully"

# Check if realm already exists
REALM_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  "http://keycloak:8080/admin/realms/shop-manager")

if [ "$REALM_EXISTS" = "200" ]; then
  echo "Realm 'shop-manager' already exists. Skipping import."
  exit 0
fi

# Import realm
echo "Importing shop-manager realm..."
IMPORT_RESULT=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d @/tmp/keycloak-realm.json \
  "http://keycloak:8080/admin/realms")

if [ "$IMPORT_RESULT" = "201" ]; then
  echo "Realm imported successfully"
else
  echo "Failed to import realm. HTTP status: $IMPORT_RESULT"
  exit 1
fi

echo "Keycloak realm setup completed successfully!"