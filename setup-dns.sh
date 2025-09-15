#!/bin/bash

# Script to add DNS entries for Shop Manager local development

echo "Adding DNS entries to /etc/hosts for Shop Manager..."

# Check if entries already exist
if grep -q "shop-manager.local" /etc/hosts; then
    echo "DNS entries already exist in /etc/hosts"
else
    echo "Adding DNS entries to /etc/hosts (requires sudo password):"
    echo "127.0.0.1 shop-manager.local" | sudo tee -a /etc/hosts
    echo "127.0.0.1 api.shop-manager.local" | sudo tee -a /etc/hosts
    echo "127.0.0.1 auth.shop-manager.local" | sudo tee -a /etc/hosts
    echo "DNS entries added successfully!"
fi

echo ""
echo "DNS entries configured:"
echo "- Frontend:  https://shop-manager.local"
echo "- API:       https://api.shop-manager.local"
echo "- Keycloak:  https://auth.shop-manager.local"
echo ""
echo "Note: You may need to accept the self-signed certificates in your browser"