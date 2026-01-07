#!/bin/bash
# ============================================================================
# Setup Custom Domain for Docker Lite
# ============================================================================
# This script helps configure custom domain access for Shop Manager Docker Lite
# ============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get custom domain from .env file
if [ -f .env ]; then
    CUSTOM_DOMAIN=$(grep "^CUSTOM_DOMAIN=" .env | cut -d '=' -f 2)
else
    echo -e "${RED}Error: .env file not found${NC}"
    echo "Please create .env file from .env.lite template:"
    echo "  cp .env.lite .env"
    exit 1
fi

if [ -z "$CUSTOM_DOMAIN" ]; then
    echo -e "${RED}Error: CUSTOM_DOMAIN not set in .env file${NC}"
    exit 1
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Shop Manager - Custom Domain Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Custom Domain: ${GREEN}$CUSTOM_DOMAIN${NC}"
echo ""

# Check if domain is localhost (no hosts file needed)
if [ "$CUSTOM_DOMAIN" = "localhost" ]; then
    echo -e "${GREEN}✓ Using localhost - no hosts file configuration needed${NC}"
    echo ""
    echo "Access your application at:"
    echo -e "  Frontend: ${BLUE}http://localhost/${NC}"
    echo -e "  Backend API: ${BLUE}http://localhost/api${NC}"
    exit 0
fi

# Detect OS
OS="$(uname -s)"
case "${OS}" in
    Linux*)     HOSTS_FILE="/etc/hosts";;
    Darwin*)    HOSTS_FILE="/etc/hosts";;
    CYGWIN*)    HOSTS_FILE="C:/Windows/System32/drivers/etc/hosts";;
    MINGW*)     HOSTS_FILE="C:/Windows/System32/drivers/etc/hosts";;
    *)          echo -e "${RED}Error: Unsupported OS${NC}"; exit 1;;
esac

echo -e "Detected OS: ${GREEN}${OS}${NC}"
echo -e "Hosts file: ${YELLOW}${HOSTS_FILE}${NC}"
echo ""

# Check if entry already exists
if grep -q "$CUSTOM_DOMAIN" "$HOSTS_FILE" 2>/dev/null; then
    echo -e "${GREEN}✓ Domain already configured in hosts file${NC}"
    grep "$CUSTOM_DOMAIN" "$HOSTS_FILE"
else
    echo -e "${YELLOW}⚠ Domain not found in hosts file${NC}"
    echo ""
    echo "To access your application via custom domain, add this line to $HOSTS_FILE:"
    echo ""
    echo -e "${GREEN}127.0.0.1   $CUSTOM_DOMAIN${NC}"
    echo ""

    # Offer to add automatically
    read -p "Would you like to add it automatically? (requires sudo) [y/N]: " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "127.0.0.1   $CUSTOM_DOMAIN" | sudo tee -a "$HOSTS_FILE" > /dev/null
        echo -e "${GREEN}✓ Domain added to hosts file${NC}"

        # Flush DNS cache
        case "${OS}" in
            Darwin*)
                echo "Flushing DNS cache..."
                sudo dscacheutil -flushcache
                sudo killall -HUP mDNSResponder 2>/dev/null || true
                echo -e "${GREEN}✓ DNS cache flushed${NC}"
                ;;
            Linux*)
                # Linux systemd-resolved
                if command -v systemd-resolve &> /dev/null; then
                    sudo systemd-resolve --flush-caches
                    echo -e "${GREEN}✓ DNS cache flushed${NC}"
                fi
                ;;
        esac
    else
        echo ""
        echo "Manual setup required:"
        echo ""
        if [[ "${OS}" == "Darwin"* ]] || [[ "${OS}" == "Linux"* ]]; then
            echo "1. Edit hosts file:"
            echo "   sudo nano $HOSTS_FILE"
            echo ""
            echo "2. Add this line:"
            echo "   127.0.0.1   $CUSTOM_DOMAIN"
            echo ""
            echo "3. Save and exit (Ctrl+X, then Y, then Enter)"
            echo ""
            if [[ "${OS}" == "Darwin"* ]]; then
                echo "4. Flush DNS cache:"
                echo "   sudo dscacheutil -flushcache"
                echo "   sudo killall -HUP mDNSResponder"
            fi
        else
            echo "1. Open Notepad as Administrator"
            echo ""
            echo "2. Open file: $HOSTS_FILE"
            echo ""
            echo "3. Add this line:"
            echo "   127.0.0.1   $CUSTOM_DOMAIN"
            echo ""
            echo "4. Save the file"
            echo ""
            echo "5. Flush DNS cache:"
            echo "   ipconfig /flushdns"
        fi
    fi
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Access Your Application${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "  Frontend: ${GREEN}http://$CUSTOM_DOMAIN/${NC}"
echo -e "  Backend API: ${GREEN}http://$CUSTOM_DOMAIN/api${NC}"
echo -e "  Health Check: ${GREEN}http://$CUSTOM_DOMAIN/actuator/health${NC}"
echo ""
echo -e "${BLUE}Default Credentials:${NC}"
echo -e "  System Admin: ${YELLOW}superadmin${NC} / ${YELLOW}changeme${NC}"
echo -e "  Tenant Admin: ${YELLOW}admin${NC} / ${YELLOW}admin123${NC}"
echo ""
echo -e "${RED}⚠ IMPORTANT: Change default passwords after first login!${NC}"
echo ""
echo "For more information, see DOCKER_LITE_CUSTOM_DOMAIN.md"
echo ""
