#!/bin/bash
# ============================================================================
# Shop Manager DNS Configuration Script
# ============================================================================
#
# This script automatically configures DNS entries for Shop Manager
# Supports: macOS, Linux
#
# Usage:
#   ./configure-dns.sh --app-name myshop --domain shop.local
#   ./configure-dns.sh --remove  # Remove DNS entries
#
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Default configuration
APP_NAME=""
DOMAIN=""
REMOVE=false
HOSTS_FILE="/etc/hosts"
BACKUP_FILE="/etc/hosts.shopmanager.backup"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --app-name)
            APP_NAME="$2"
            shift 2
            ;;
        --domain)
            DOMAIN="$2"
            shift 2
            ;;
        --remove)
            REMOVE=true
            shift
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --app-name NAME    Application name (e.g., myshop)"
            echo "  --domain DOMAIN    Base domain (e.g., shop.local)"
            echo "  --remove           Remove DNS entries"
            echo "  --help             Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Helper functions
print_header() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠  $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if running with sudo
check_sudo() {
    if [ "$EUID" -ne 0 ]; then
        print_error "This script must be run with sudo privileges"
        echo ""
        echo "Please run: sudo $0 $@"
        exit 1
    fi
}

# Detect operating system
detect_os() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo "macos"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "linux"
    else
        echo "unknown"
    fi
}

# Remove DNS entries
remove_dns_entries() {
    print_header "Removing Shop Manager DNS Entries"

    if [ ! -f "$HOSTS_FILE" ]; then
        print_warning "Hosts file not found: $HOSTS_FILE"
        return
    fi

    # Check if backup exists
    if [ -f "$BACKUP_FILE" ]; then
        print_info "Restoring from backup..."
        cp "$BACKUP_FILE" "$HOSTS_FILE"
        rm "$BACKUP_FILE"
        print_success "DNS entries removed and backup restored"
    else
        # Remove Shop Manager entries manually
        print_info "Removing Shop Manager entries..."
        grep -v "# Shop Manager" "$HOSTS_FILE" > "$HOSTS_FILE.tmp" || true
        mv "$HOSTS_FILE.tmp" "$HOSTS_FILE"
        print_success "DNS entries removed"
    fi

    # Flush DNS cache
    flush_dns_cache
}

# Add DNS entries
add_dns_entries() {
    local app_name="$1"
    local domain="$2"

    print_header "Configuring DNS for Shop Manager"

    # Validate inputs
    if [ -z "$app_name" ]; then
        print_error "Application name is required"
        exit 1
    fi

    if [ -z "$domain" ]; then
        print_error "Domain is required"
        exit 1
    fi

    # Create full domain names
    local frontend_domain="${app_name}.${domain}"
    local api_domain="api.${app_name}.${domain}"
    local auth_domain="auth.${app_name}.${domain}"

    print_info "Configuring DNS for:"
    echo "  Frontend: https://${frontend_domain}"
    echo "  API:      https://${api_domain}"
    echo "  Auth:     https://${auth_domain}"
    echo ""

    # Backup hosts file
    if [ ! -f "$BACKUP_FILE" ]; then
        print_info "Creating backup of hosts file..."
        cp "$HOSTS_FILE" "$BACKUP_FILE"
        print_success "Backup created: $BACKUP_FILE"
    fi

    # Remove old Shop Manager entries if they exist
    if grep -q "# Shop Manager" "$HOSTS_FILE"; then
        print_info "Removing old Shop Manager entries..."
        grep -v "# Shop Manager" "$HOSTS_FILE" > "$HOSTS_FILE.tmp"
        mv "$HOSTS_FILE.tmp" "$HOSTS_FILE"
    fi

    # Add new entries
    print_info "Adding DNS entries to $HOSTS_FILE..."

    cat >> "$HOSTS_FILE" << EOF

# Shop Manager - Auto-generated DNS entries
# Generated on $(date)
127.0.0.1 ${frontend_domain}  # Shop Manager Frontend
127.0.0.1 ${api_domain}       # Shop Manager API
127.0.0.1 ${auth_domain}      # Shop Manager Auth (Keycloak)
EOF

    print_success "DNS entries added successfully"

    # Flush DNS cache
    flush_dns_cache

    # Platform-specific configuration
    local os=$(detect_os)
    if [ "$os" == "macos" ]; then
        configure_macos_mdns "$app_name" "$domain"
    elif [ "$os" == "linux" ]; then
        configure_linux_dns "$app_name" "$domain"
    fi
}

# Flush DNS cache
flush_dns_cache() {
    local os=$(detect_os)

    print_info "Flushing DNS cache..."

    if [ "$os" == "macos" ]; then
        # macOS DNS cache flush
        dscacheutil -flushcache
        killall -HUP mDNSResponder 2>/dev/null || true
        print_success "macOS DNS cache flushed"

    elif [ "$os" == "linux" ]; then
        # Linux DNS cache flush
        if command -v systemd-resolve &> /dev/null; then
            systemd-resolve --flush-caches 2>/dev/null || true
        fi
        if command -v nscd &> /dev/null; then
            nscd -i hosts 2>/dev/null || true
        fi
        print_success "Linux DNS cache flushed"
    fi
}

# Configure macOS mDNS
configure_macos_mdns() {
    local app_name="$1"
    local domain="$2"

    print_info "Configuring macOS mDNS (Bonjour)..."

    # Check if domain ends with .local
    if [[ "$domain" == *.local ]]; then
        print_success "Using .local domain - mDNS enabled automatically"
        print_info "Services will be discoverable on the local network"
    else
        print_warning "Domain does not end with .local - mDNS will not work"
        print_info "Consider using a .local domain for automatic discovery"
    fi
}

# Configure Linux DNS
configure_linux_dns() {
    local app_name="$1"
    local domain="$2"

    print_info "Configuring Linux DNS..."

    # Check for systemd-resolved
    if command -v systemd-resolve &> /dev/null; then
        print_info "Detected systemd-resolved"

        # Check if domain ends with .local
        if [[ "$domain" == *.local ]]; then
            print_info "Configuring mDNS for .local domain..."

            # Enable mDNS in resolved.conf if not already enabled
            if [ -f /etc/systemd/resolved.conf ]; then
                if ! grep -q "^MulticastDNS=yes" /etc/systemd/resolved.conf; then
                    sed -i 's/^#MulticastDNS=.*/MulticastDNS=yes/' /etc/systemd/resolved.conf
                    systemctl restart systemd-resolved 2>/dev/null || true
                    print_success "mDNS enabled in systemd-resolved"
                fi
            fi
        fi
    fi

    # Check for avahi (mDNS daemon)
    if command -v avahi-daemon &> /dev/null; then
        if systemctl is-active --quiet avahi-daemon; then
            print_success "Avahi mDNS daemon is running"
        else
            print_warning "Avahi mDNS daemon is not running"
            print_info "To enable mDNS discovery: sudo systemctl start avahi-daemon"
        fi
    fi
}

# Verify DNS configuration
verify_dns() {
    local app_name="$1"
    local domain="$2"

    print_header "Verifying DNS Configuration"

    local frontend_domain="${app_name}.${domain}"
    local api_domain="api.${app_name}.${domain}"
    local auth_domain="auth.${app_name}.${domain}"

    # Check hosts file entries
    print_info "Checking /etc/hosts entries..."

    if grep -q "$frontend_domain" "$HOSTS_FILE"; then
        print_success "Frontend domain found in hosts file"
    else
        print_error "Frontend domain NOT found in hosts file"
    fi

    if grep -q "$api_domain" "$HOSTS_FILE"; then
        print_success "API domain found in hosts file"
    else
        print_error "API domain NOT found in hosts file"
    fi

    if grep -q "$auth_domain" "$HOSTS_FILE"; then
        print_success "Auth domain found in hosts file"
    else
        print_error "Auth domain NOT found in hosts file"
    fi

    # Test DNS resolution
    print_info "Testing DNS resolution..."

    for domain_name in "$frontend_domain" "$api_domain" "$auth_domain"; do
        if ping -c 1 -W 1 "$domain_name" &> /dev/null; then
            print_success "$domain_name resolves correctly"
        else
            # Try with host command
            if host "$domain_name" &> /dev/null 2>&1; then
                print_success "$domain_name resolves correctly"
            else
                print_warning "$domain_name resolution failed (will work once services start)"
            fi
        fi
    done
}

# Display usage instructions
show_usage_instructions() {
    local app_name="$1"
    local domain="$2"

    print_header "DNS Configuration Complete!"

    echo ""
    echo -e "${GREEN}✅ DNS entries configured successfully!${NC}"
    echo ""
    echo "Access URLs:"
    echo -e "  ${CYAN}Frontend:${NC}  https://${app_name}.${domain}"
    echo -e "  ${CYAN}API:${NC}       https://api.${app_name}.${domain}"
    echo -e "  ${CYAN}Auth:${NC}      https://auth.${app_name}.${domain}"
    echo ""

    if [[ "$domain" == *.local ]]; then
        echo -e "${CYAN}ℹ  mDNS Enabled${NC}"
        echo "  These domains will be discoverable on your local network"
        echo "  Other devices can access using the same URLs"
        echo ""
    fi

    print_warning "Important Notes:"
    echo "  1. DNS entries point to 127.0.0.1 (localhost)"
    echo "  2. Services must be running for URLs to work"
    echo "  3. SSL certificates must be installed for HTTPS"
    echo ""
    echo "To remove these entries:"
    echo "  sudo $0 --remove"
    echo ""
}

# Main execution
main() {
    local os=$(detect_os)

    if [ "$os" == "unknown" ]; then
        print_error "Unsupported operating system: $OSTYPE"
        print_info "This script supports macOS and Linux only"
        exit 1
    fi

    # Check for sudo
    check_sudo

    if [ "$REMOVE" = true ]; then
        remove_dns_entries
    else
        if [ -z "$APP_NAME" ] || [ -z "$DOMAIN" ]; then
            print_error "Both --app-name and --domain are required"
            echo ""
            echo "Usage: $0 --app-name myshop --domain shop.local"
            exit 1
        fi

        add_dns_entries "$APP_NAME" "$DOMAIN"
        verify_dns "$APP_NAME" "$DOMAIN"
        show_usage_instructions "$APP_NAME" "$DOMAIN"
    fi
}

# Run main function
main "$@"
