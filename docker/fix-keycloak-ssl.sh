#!/bin/bash

# Script to fix Keycloak SSL requirements for development

# Wait for Keycloak to be ready
until curl -s http://localhost:8080/health/ready; do
    echo "Waiting for Keycloak to be ready..."
    sleep 2
done

echo "Keycloak is ready! Configuring SSL settings..."

# Get admin token
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin&grant_type=password&client_id=admin-cli" | \
  grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Failed to get admin token"
  exit 1
fi

echo "Admin token obtained successfully"

# Configure master realm to not require SSL
echo "Configuring master realm SSL settings..."
curl -s -X PUT "http://localhost:8080/admin/realms/master" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sslRequired":"none"}'

# Configure shop-manager realm to not require SSL
echo "Configuring shop-manager realm SSL settings..."
curl -s -X PUT "http://localhost:8080/admin/realms/shop-manager" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sslRequired":"none"}'

echo "SSL configuration completed successfully!"