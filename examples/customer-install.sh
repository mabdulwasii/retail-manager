#!/bin/bash

# ============================================================================
# Shop Manager - Customer Installation Script
# ============================================================================
#
# This script automates the installation of Shop Manager on Kubernetes
# using Helm from Docker Hub (no repository clone needed).
#
# Prerequisites:
#   - Docker Desktop with Kubernetes enabled
#   - Helm 3 installed
#
# Usage:
#   ./customer-install.sh
#
# ============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
CHART_REPO="oci://registry-1.docker.io/princely/shop-manager"
CHART_VERSION="0.0.46"
RELEASE_NAME="retail"
NAMESPACE="gomco"
VALUES_FILE="my-values.yaml"

# ============================================================================
# Helper Functions
# ============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

check_command() {
    if ! command -v $1 &> /dev/null; then
        print_error "$1 is not installed. Please install it first."
        exit 1
    fi
}

# ============================================================================
# Step 1: Check Prerequisites
# ============================================================================

print_header "Step 1/8: Checking Prerequisites"

# Check kubectl
print_info "Checking kubectl..."
check_command kubectl
print_success "kubectl is installed"

# Check helm
print_info "Checking Helm..."
check_command helm
print_success "Helm is installed"

# Check Kubernetes cluster
print_info "Checking Kubernetes cluster..."
if ! kubectl cluster-info &> /dev/null; then
    print_error "Kubernetes cluster is not accessible"
    print_info "Please enable Kubernetes in Docker Desktop:"
    print_info "  Docker Desktop → Settings → Kubernetes → Enable Kubernetes"
    exit 1
fi
print_success "Kubernetes cluster is accessible"

# ============================================================================
# Step 2: Gather Configuration
# ============================================================================

print_header "Step 2/8: Configuration"

# Check if values file exists
if [ -f "$VALUES_FILE" ]; then
    print_info "Found existing values file: $VALUES_FILE"
    read -p "Use existing values file? (y/n): " use_existing
    if [ "$use_existing" != "y" ]; then
        rm $VALUES_FILE
    fi
fi

# Download template if needed
if [ ! -f "$VALUES_FILE" ]; then
    print_info "Downloading customer values template..."
    curl -sL https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-values.yaml -o customer-values-template.yaml

    # Interactive configuration
    echo ""
    echo "Let's configure your Shop Manager installation:"
    echo ""

    read -p "Your company domain (e.g., mycompany.com): " company_domain
    read -p "Platform name (e.g., Acme Retail Pro): " platform_name
    read -p "Company name (e.g., Acme Corporation): " company_name

    # Ask about test users
    echo ""
    print_warning "Test users provide pre-configured accounts for testing."
    print_warning "For production deployments, it's recommended to disable them."
    read -p "Enable test users? (y/n, default: n): " enable_test_users
    enable_test_users=${enable_test_users:-n}

    if [ "$enable_test_users" = "y" ]; then
        TEST_USERS_ENABLED="true"
        print_info "Test users will be created (admin@shopmanager.com, etc.)"
    else
        TEST_USERS_ENABLED="false"
        print_info "Test users will be disabled (production mode)"
    fi

    # Create values file
    cat > $VALUES_FILE <<EOF
global:
  appName: "retail"
  domain: "${company_domain}"

backend:
  image:
    repository: princely/shop-manager
    tag: backend-latest
    pullPolicy: Always
  podAnnotations:
    configVersion: "32"
  env:
    SWAGGER_SECURITY_ENABLED: "false"

frontend:
  enabled: true
  image:
    repository: princely/shop-manager
    tag: frontend-latest
    pullPolicy: Always

branding:
  platformName: "${platform_name}"
  companyName: "${company_name}"
  platformDescription: "Advanced Retail Management System"
  colors:
    primary: "#2E7D32"
    secondary: "#FF6F00"

application:
  testUsers:
    enabled: ${TEST_USERS_ENABLED}

tls:
  enabled: true
  issuer: "local-ca-issuer"
  email: "admin@${company_domain}"
  localCertInstallation:
    enabled: true

postgresql:
  auth:
    existingSecret: ""

minio:
  enabled: false
  auth:
    existingSecret: ""

kafka:
  enabled: false
EOF

    print_success "Created values file: $VALUES_FILE"
fi

# ============================================================================
# Step 3: Install cert-manager
# ============================================================================

print_header "Step 3/8: Installing cert-manager"

if kubectl get namespace cert-manager &> /dev/null; then
    print_info "cert-manager already installed"
else
    print_info "Installing cert-manager..."
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

    print_info "Waiting for cert-manager to be ready (this may take a minute)..."
    kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all

    print_success "cert-manager installed successfully"
fi

# ============================================================================
# Step 4: Install NGINX Ingress Controller
# ============================================================================

print_header "Step 4/8: Installing NGINX Ingress Controller"

if helm list -n ingress-nginx | grep -q ingress-nginx; then
    print_info "ingress-nginx already installed"
else
    print_info "Adding ingress-nginx Helm repository..."
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
    helm repo update

    print_info "Installing ingress-nginx..."
    helm install ingress-nginx ingress-nginx/ingress-nginx \
        --namespace ingress-nginx \
        --create-namespace \
        --wait \
        --timeout 5m

    print_success "ingress-nginx installed successfully"
fi

# ============================================================================
# Step 5: Create Namespace
# ============================================================================

print_header "Step 5/8: Creating Namespace"

if kubectl get namespace $NAMESPACE &> /dev/null; then
    print_info "Namespace $NAMESPACE already exists"
else
    kubectl create namespace $NAMESPACE
    print_success "Created namespace: $NAMESPACE"
fi

# ============================================================================
# Step 6: Install Shop Manager
# ============================================================================

print_header "Step 6/8: Installing Shop Manager"

print_info "Installing Shop Manager from Docker Hub..."
print_info "Chart: $CHART_REPO"
print_info "Version: $CHART_VERSION"
print_info "This may take 5-10 minutes depending on your internet speed..."

helm install $RELEASE_NAME $CHART_REPO \
    --version $CHART_VERSION \
    -n $NAMESPACE \
    -f $VALUES_FILE \
    --wait \
    --timeout 10m

print_success "Shop Manager installed successfully!"

# ============================================================================
# Step 7: Setup SSL and DNS
# ============================================================================

print_header "Step 7/8: Setting up SSL and DNS"

# Get domain from values file
DOMAIN=$(grep 'domain:' $VALUES_FILE | head -1 | awk '{print $2}' | tr -d '"')

print_info "Extracting SSL certificate..."
kubectl get secret local-ca-key-pair -n cert-manager \
    -o jsonpath='{.data.tls\.crt}' | base64 -d > /tmp/shop-manager-ca.crt

# Install certificate based on OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    print_info "Installing certificate to macOS keychain..."
    sudo security add-trusted-cert -d -r trustRoot \
        -k /Library/Keychains/System.keychain \
        /tmp/shop-manager-ca.crt
    print_success "Certificate installed"
elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    print_info "Installing certificate to Linux CA store..."
    sudo cp /tmp/shop-manager-ca.crt /usr/local/share/ca-certificates/shop-manager-ca.crt
    sudo update-ca-certificates
    print_success "Certificate installed"
else
    print_warning "Please manually install certificate from /tmp/shop-manager-ca.crt"
fi

# Add DNS entries
print_info "Adding DNS entries to /etc/hosts..."

DNS_ENTRIES="
# Shop Manager DNS entries
127.0.0.1 retail.${DOMAIN}
127.0.0.1 api.retail.${DOMAIN}
127.0.0.1 auth.retail.${DOMAIN}
"

# Remove old entries
sudo sed -i.bak '/# Shop Manager DNS entries/,/^$/d' /etc/hosts 2>/dev/null || true

# Add new entries
echo "$DNS_ENTRIES" | sudo tee -a /etc/hosts > /dev/null

print_success "DNS entries added to /etc/hosts"

# ============================================================================
# Step 8: Verification and Next Steps
# ============================================================================

print_header "Step 8/8: Verification"

print_info "Checking deployment status..."

# Wait a bit for everything to settle
sleep 5

# Check pod status
if kubectl get pods -n $NAMESPACE | grep -q Running; then
    print_success "All pods are running"
else
    print_warning "Some pods may still be starting up"
    kubectl get pods -n $NAMESPACE
fi

# ============================================================================
# Installation Complete
# ============================================================================

print_header "🎉 Installation Complete!"

echo ""
echo -e "${GREEN}Shop Manager has been successfully installed!${NC}"
echo ""
echo "📌 Important: Please restart your browser completely before accessing"
echo ""
echo "🌐 Access URLs:"
echo "   Frontend:  https://retail.${DOMAIN}"
echo "   API Docs:  https://api.retail.${DOMAIN}/swagger-ui/index.html"
echo "   Keycloak:  https://auth.retail.${DOMAIN}"
echo ""
echo "🔑 Default Login Credentials:"
echo "   Email:     admin@shopmanager.com"
echo "   Password:  DevAdmin@2024!Test"
echo ""
echo "📝 Configuration file saved to: $VALUES_FILE"
echo ""
echo "🔄 To upgrade in the future:"
echo "   helm upgrade $RELEASE_NAME $CHART_REPO --version <new-version> -n $NAMESPACE -f $VALUES_FILE"
echo ""
echo "🗑️  To uninstall:"
echo "   helm uninstall $RELEASE_NAME -n $NAMESPACE"
echo ""

# Offer to open browser
read -p "Open browser now? (y/n): " open_browser
if [ "$open_browser" = "y" ]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open "https://retail.${DOMAIN}"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        xdg-open "https://retail.${DOMAIN}"
    fi
fi

print_success "Installation script completed successfully!"
