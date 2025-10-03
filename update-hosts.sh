#!/bin/bash

# Script to dynamically update /etc/hosts with ingress URLs
# This script fetches ingress resources and adds their hostnames to /etc/hosts

set -e

echo "🔍 Fetching dynamic ingress URLs from Kubernetes..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get all ingress hosts across all namespaces for gomco-services
INGRESS_HOSTS=$(kubectl get ingress --all-namespaces -o json | \
    jq -r '.items[] | select(.metadata.name | contains("gomco-services")) | .spec.rules[].host' | \
    sort -u)

if [ -z "$INGRESS_HOSTS" ]; then
    echo "❌ No ingress resources found for gomco-services"
    exit 1
fi

# Get the ingress controller IP (usually localhost for local development)
INGRESS_IP="127.0.0.1"

echo -e "${BLUE}📋 Found the following dynamic ingress URLs:${NC}"
echo "$INGRESS_HOSTS" | while read -r host; do
    echo "   • $host"
done

# Create the hosts entries
HOSTS_ENTRIES=""
while IFS= read -r host; do
    HOSTS_ENTRIES="${HOSTS_ENTRIES}${INGRESS_IP}   ${host}\n"
done <<< "$INGRESS_HOSTS"

# Check if entries already exist
echo ""
echo "🔍 Checking existing entries in /etc/hosts..."
EXISTING_ENTRIES=""
while IFS= read -r host; do
    if grep -q "$host" /etc/hosts 2>/dev/null; then
        EXISTING_ENTRIES="${EXISTING_ENTRIES}${host}\n"
        echo -e "${YELLOW}   ⚠️  $host already exists in /etc/hosts${NC}"
    else
        echo -e "${GREEN}   ✅ $host will be added${NC}"
    fi
done <<< "$INGRESS_HOSTS"

# Generate the command to add missing entries
echo ""
echo -e "${BLUE}📝 To update your /etc/hosts file, run this command:${NC}"
echo ""
echo "sudo sh -c 'cat >> /etc/hosts << EOF"
echo ""
echo "# GOMCO Services - Dynamic Ingress URLs (Generated $(date))"
while IFS= read -r host; do
    if ! grep -q "$host" /etc/hosts 2>/dev/null; then
        echo "${INGRESS_IP}   ${host}"
    fi
done <<< "$INGRESS_HOSTS"
echo "EOF'"

echo ""
echo -e "${GREEN}🌐 After updating, you can access:${NC}"
while IFS= read -r host; do
    case "$host" in
        *frontend* | gomco-services.*)
            echo -e "   🖥️  Frontend:    http://$host"
            ;;
        *api.*)
            echo -e "   📊 Backend API: http://$host"
            ;;
        *auth.* | *keycloak*)
            echo -e "   🔐 Keycloak:    http://$host"
            ;;
        *)
            echo -e "   🌐 Service:     http://$host"
            ;;
    esac
done <<< "$INGRESS_HOSTS"

echo ""
echo "💡 TIP: You can also access services via port-forwarding:"
echo "   • Frontend:  http://localhost:3001"
echo "   • Backend:   http://localhost:8081"
echo "   • Keycloak:  http://localhost:8080"