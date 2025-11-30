#!/bin/bash
# ============================================================================
# SSL/TLS Certificate Installation Script
# ============================================================================
#
# This script generates and installs SSL/TLS certificates for Shop Manager
# Supports: macOS, Linux, and Windows (via WSL)
#
# Usage:
#   ./install-certs.sh
#   ./install-certs.sh --domain myshop.local --org "My Company"
#   ./install-certs.sh --skip-install  # Generate only, don't install
#
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Default configuration
APP_NAME=""
DOMAIN="localhost"
ORGANIZATION="Shop Manager"
COUNTRY="US"
STATE="California"
LOCALITY="San Francisco"
VALIDITY_DAYS=365
SKIP_INSTALL=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="${SCRIPT_DIR}/../certs"

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
        --org)
            ORGANIZATION="$2"
            shift 2
            ;;
        --country)
            COUNTRY="$2"
            shift 2
            ;;
        --state)
            STATE="$2"
            shift 2
            ;;
        --city)
            LOCALITY="$2"
            shift 2
            ;;
        --days)
            VALIDITY_DAYS="$2"
            shift 2
            ;;
        --skip-install)
            SKIP_INSTALL=true
            shift
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --app-name NAME       Application name (e.g., myshop)"
            echo "  --domain DOMAIN       Domain name (default: localhost)"
            echo "  --org ORGANIZATION    Organization name (default: Shop Manager)"
            echo "  --country CODE        Country code (default: US)"
            echo "  --state STATE         State/Province (default: California)"
            echo "  --city CITY           City/Locality (default: San Francisco)"
            echo "  --days DAYS           Certificate validity in days (default: 365)"
            echo "  --skip-install        Generate only, don't install to system"
            echo "  --help                Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 --app-name myshop --domain shop.local"
            echo "  $0 --domain myshop.local --org \"My Company\""
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

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ  $1${NC}"
}

# ============================================================================
# Step 1: Generate Certificates
# ============================================================================

print_header "SSL/TLS Certificate Generator & Installer"

echo "Certificate Configuration:"
echo "  Domain:       ${DOMAIN}"
echo "  Organization: ${ORGANIZATION}"
echo "  Country:      ${COUNTRY}"
echo "  State:        ${STATE}"
echo "  Locality:     ${LOCALITY}"
echo "  Valid for:    ${VALIDITY_DAYS} days"
echo ""

print_header "Step 1/3: Generating Certificates"

# Create certs directory
mkdir -p "${CERTS_DIR}"

# Generate private key
print_info "Generating private key..."
openssl genrsa -out "${CERTS_DIR}/localhost.key" 4096 2>/dev/null
print_success "Private key generated"

# Create certificate signing request config
cat > "${CERTS_DIR}/csr.conf" <<EOF
[req]
default_bits = 4096
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[dn]
C = ${COUNTRY}
ST = ${STATE}
L = ${LOCALITY}
O = ${ORGANIZATION}
CN = ${DOMAIN}

[req_ext]
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${DOMAIN}
DNS.2 = *.${DOMAIN}
DNS.3 = localhost
DNS.4 = *.localhost
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

# Add app-specific subdomains if app-name is provided
if [ -n "$APP_NAME" ]; then
    # Build full domain names
    if [[ "$APP_NAME" == *"$DOMAIN"* ]] || [[ "$DOMAIN" == *"$APP_NAME"* ]]; then
        # App name already in domain (e.g., myshop.local)
        FULL_DOMAIN="$DOMAIN"
    else
        # Build full domain with app name (e.g., myshop.shop.local)
        FULL_DOMAIN="${APP_NAME}.${DOMAIN}"
    fi

    print_info "Adding app-specific subdomains for: $APP_NAME"

    # Add subdomains to alt_names
    cat >> "${CERTS_DIR}/csr.conf" <<EOF_SUBDOMAINS
DNS.5 = ${FULL_DOMAIN}
DNS.6 = *.${FULL_DOMAIN}
DNS.7 = api.${FULL_DOMAIN}
DNS.8 = auth.${FULL_DOMAIN}
EOF_SUBDOMAINS

    print_success "Subdomains added: $FULL_DOMAIN, api.$FULL_DOMAIN, auth.$FULL_DOMAIN"
fi

# Generate certificate
print_info "Generating self-signed certificate..."
openssl req -new -x509 \
    -key "${CERTS_DIR}/localhost.key" \
    -out "${CERTS_DIR}/localhost.crt" \
    -days ${VALIDITY_DAYS} \
    -config "${CERTS_DIR}/csr.conf" \
    -extensions req_ext \
    2>/dev/null

print_success "Certificate generated"

# Display certificate info
print_info "Certificate Information:"
openssl x509 -in "${CERTS_DIR}/localhost.crt" -noout -subject -issuer -dates

# ============================================================================
# Step 2: Install to System Trust Store
# ============================================================================

if [ "$SKIP_INSTALL" = true ]; then
    print_header "Step 2/3: Installation (Skipped)"
    print_info "Certificate files are in: ${CERTS_DIR}"
    print_info "To install manually, see documentation"
    exit 0
fi

print_header "Step 2/3: Installing to System Trust Store"

# Detect operating system
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    print_info "Detected macOS"

    print_info "Installing certificate to macOS keychain..."
    print_warning "This will require your administrator password"

    # Add to system keychain
    sudo security add-trusted-cert \
        -d -r trustRoot \
        -k /Library/Keychains/System.keychain \
        "${CERTS_DIR}/localhost.crt"

    print_success "Certificate installed to macOS keychain"

    # Update browser certificate stores
    print_info "Updating browser certificate stores..."

    # Chrome/Brave (uses system keychain)
    print_success "Chrome/Brave will use system keychain"

    # Firefox (has its own certificate store)
    if [ -d "$HOME/Library/Application Support/Firefox" ]; then
        print_info "Firefox detected - you may need to manually trust the certificate"
        print_info "  1. Open Firefox → Settings → Privacy & Security"
        print_info "  2. Certificates → View Certificates → Authorities"
        print_info "  3. Import: ${CERTS_DIR}/localhost.crt"
    fi

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    print_info "Detected Linux"

    # Detect distribution
    if [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        print_info "Debian/Ubuntu based distribution"

        print_info "Installing certificate to CA store..."
        sudo cp "${CERTS_DIR}/localhost.crt" /usr/local/share/ca-certificates/shop-manager.crt
        sudo update-ca-certificates

        print_success "Certificate installed to system CA store"

    elif [ -f /etc/redhat-release ]; then
        # RHEL/CentOS/Fedora
        print_info "RHEL/Fedora based distribution"

        print_info "Installing certificate to CA store..."
        sudo cp "${CERTS_DIR}/localhost.crt" /etc/pki/ca-trust/source/anchors/shop-manager.crt
        sudo update-ca-trust

        print_success "Certificate installed to system CA store"

    else
        print_warning "Unknown Linux distribution"
        print_info "Please manually install certificate to your CA store"
    fi

    # Browser-specific instructions
    print_info "Browser Certificate Installation:"

    # Chrome/Chromium
    if command -v google-chrome &> /dev/null || command -v chromium &> /dev/null; then
        print_info "  Chrome/Chromium: Will use system certificate store after restart"
    fi

    # Firefox
    if [ -d "$HOME/.mozilla/firefox" ]; then
        print_info "  Firefox: Import manually"
        print_info "    Settings → Privacy & Security → Certificates → View Certificates"
        print_info "    → Authorities → Import → ${CERTS_DIR}/localhost.crt"
    fi

elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # Windows (Git Bash or WSL)
    print_info "Detected Windows"

    print_info "Installing certificate to Windows certificate store..."
    print_warning "This requires administrator privileges"

    # Use certutil to install
    certutil.exe -addstore -f "ROOT" "${CERTS_DIR}/localhost.crt"

    print_success "Certificate installed to Windows certificate store"

    # Browser info
    print_info "Browser Certificate Installation:"
    print_info "  Chrome/Edge: Will use Windows certificate store"
    print_info "  Firefox: Import manually in Firefox settings"

else
    print_warning "Unknown operating system: $OSTYPE"
    print_info "Please manually install the certificate"
    print_info "Certificate location: ${CERTS_DIR}/localhost.crt"
fi

# ============================================================================
# Step 3: Verification
# ============================================================================

print_header "Step 3/3: Verification"

print_info "Verifying certificate..."

# Test certificate
if openssl verify -CAfile "${CERTS_DIR}/localhost.crt" "${CERTS_DIR}/localhost.crt" &>/dev/null; then
    print_success "Certificate is valid"
else
    print_warning "Certificate verification returned warnings (this is normal for self-signed certificates)"
fi

# ============================================================================
# Completion
# ============================================================================

print_header "Certificate Installation Complete"

echo "Certificate files:"
echo "  Private Key:  ${CERTS_DIR}/localhost.key"
echo "  Certificate:  ${CERTS_DIR}/localhost.crt"
echo ""

print_warning "Important Notes:"
echo "  1. This is a self-signed certificate for development/testing"
echo "  2. For production, use a certificate from a trusted CA (Let's Encrypt, DigiCert, etc.)"
echo "  3. You may need to restart your browser for changes to take effect"
echo "  4. Some browsers (Firefox) require manual certificate import"
echo ""

echo "To use these certificates with Docker:"
echo "  1. Copy to docker/certs/ directory"
echo "  2. Update docker-compose.yml to mount certificates"
echo "  3. Restart Docker containers"
echo ""

print_success "Certificate installation completed successfully!"
