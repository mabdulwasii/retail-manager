#!/bin/bash

# Automated script to update /etc/hosts with dynamic ingress URLs
# This script will attempt to automatically update the hosts file

set -e

echo "🔍 Fetching dynamic ingress URLs from Kubernetes..."

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
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

# Check which entries need to be added
ENTRIES_TO_ADD=""
while IFS= read -r host; do
    if ! grep -q "$host" /etc/hosts 2>/dev/null; then
        ENTRIES_TO_ADD="${ENTRIES_TO_ADD}${INGRESS_IP}   ${host}\n"
    fi
done <<< "$INGRESS_HOSTS"

if [ -z "$ENTRIES_TO_ADD" ]; then
    echo -e "${GREEN}✅ All ingress URLs are already in /etc/hosts!${NC}"
    echo ""
    echo -e "${GREEN}🌐 You can access:${NC}"
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
    exit 0
fi

# Create a temporary file with the new entries
TEMP_FILE=$(mktemp)
echo "" >> "$TEMP_FILE"
echo "# GOMCO Services - Dynamic Ingress URLs (Generated $(date))" >> "$TEMP_FILE"
echo -e "$ENTRIES_TO_ADD" >> "$TEMP_FILE"

echo ""
echo -e "${YELLOW}📝 The following entries need to be added to /etc/hosts:${NC}"
cat "$TEMP_FILE"

echo ""
echo -e "${BLUE}🔧 Attempting to update /etc/hosts automatically...${NC}"

# Try to update /etc/hosts
if [ -w /etc/hosts ]; then
    # If we have write permission (unlikely)
    cat "$TEMP_FILE" >> /etc/hosts
    echo -e "${GREEN}✅ Successfully updated /etc/hosts!${NC}"
else
    # Try with sudo (will prompt for password)
    echo -e "${YELLOW}⚠️  Need sudo permission to update /etc/hosts${NC}"
    echo ""

    # Check if we're in an interactive terminal
    if [ -t 0 ]; then
        echo "Please enter your password when prompted:"
        if sudo sh -c "cat '$TEMP_FILE' >> /etc/hosts"; then
            echo -e "${GREEN}✅ Successfully updated /etc/hosts!${NC}"
        else
            echo -e "${RED}❌ Failed to update /etc/hosts${NC}"
            echo ""
            echo -e "${YELLOW}📋 Please run this command manually:${NC}"
            echo ""
            echo "sudo sh -c 'cat >> /etc/hosts << EOF"
            cat "$TEMP_FILE"
            echo "EOF'"
        fi
    else
        echo -e "${YELLOW}📋 Not running in interactive mode. Please run this command manually:${NC}"
        echo ""
        echo "sudo sh -c 'cat >> /etc/hosts << EOF"
        cat "$TEMP_FILE"
        echo "EOF'"
    fi
fi

# Clean up temp file
rm -f "$TEMP_FILE"

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