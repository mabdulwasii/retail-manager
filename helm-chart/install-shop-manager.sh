#!/bin/bash
set -e

# Shop Manager - Automated Installation Script
# This script automates the complete installation of Shop Manager on Kubernetes

echo "🚀 Shop Manager - Automated Installation"
echo "========================================="
echo ""

# Configuration
NAMESPACE="${NAMESPACE:-gomco}"
RELEASE_NAME="${RELEASE_NAME:-retail}"
VALUES_FILE="${VALUES_FILE:-../gomco-values.yaml}"
TIMEOUT="${TIMEOUT:-10m}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Step 1: Verify prerequisites
echo "Step 1: Verifying prerequisites..."
if ! command_exists kubectl; then
    print_error "kubectl not found. Please install kubectl first."
    exit 1
fi

if ! command_exists helm; then
    print_error "helm not found. Please install helm first."
    exit 1
fi

print_success "Prerequisites verified (kubectl and helm found)"
echo ""

# Step 2: Check Kubernetes cluster connectivity
echo "Step 2: Checking Kubernetes cluster connectivity..."
if ! kubectl cluster-info &>/dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi
print_success "Connected to Kubernetes cluster"
echo ""

# Step 3: Install cert-manager (if not already installed)
echo "Step 3: Checking cert-manager installation..."
if kubectl get namespace cert-manager &>/dev/null; then
    print_info "cert-manager namespace already exists"
else
    print_info "Installing cert-manager..."
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

    print_info "Waiting for cert-manager to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all
    print_success "cert-manager installed and ready"
fi
echo ""

# Step 4: Install NGINX Ingress Controller (if not already installed)
echo "Step 4: Checking NGINX Ingress Controller installation..."
if kubectl get namespace ingress-nginx &>/dev/null; then
    print_info "ingress-nginx namespace already exists"
else
    print_info "Installing NGINX Ingress Controller..."
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx >/dev/null 2>&1 || true
    helm repo update >/dev/null 2>&1

    helm install ingress-nginx ingress-nginx/ingress-nginx \
        --namespace ingress-nginx \
        --create-namespace \
        --set controller.service.type=LoadBalancer \
        --wait \
        --timeout 5m

    print_success "NGINX Ingress Controller installed"
fi
echo ""

# Step 5: Create namespace
echo "Step 5: Creating namespace '${NAMESPACE}'..."
if kubectl get namespace "${NAMESPACE}" &>/dev/null; then
    print_info "Namespace '${NAMESPACE}' already exists"
else
    kubectl create namespace "${NAMESPACE}"
    print_success "Namespace '${NAMESPACE}' created"
fi
echo ""

# Step 6: Deploy Shop Manager with Helm
echo "Step 6: Deploying Shop Manager with Helm..."
print_info "Release name: ${RELEASE_NAME}"
print_info "Namespace: ${NAMESPACE}"
print_info "Values file: ${VALUES_FILE}"
print_info "Timeout: ${TIMEOUT}"
echo ""

cd "$(dirname "$0")"

if helm list -n "${NAMESPACE}" | grep -q "${RELEASE_NAME}"; then
    print_info "Helm release '${RELEASE_NAME}' already exists. Upgrading..."
    helm upgrade "${RELEASE_NAME}" ./shop-manager \
        -n "${NAMESPACE}" \
        -f "${VALUES_FILE}" \
        --wait \
        --timeout "${TIMEOUT}"
    print_success "Helm release upgraded"
else
    print_info "Installing new Helm release..."
    helm install "${RELEASE_NAME}" ./shop-manager \
        -n "${NAMESPACE}" \
        -f "${VALUES_FILE}" \
        --wait \
        --timeout "${TIMEOUT}"
    print_success "Helm release installed"
fi
echo ""

# Step 7: Verify deployment
echo "Step 7: Verifying deployment..."
print_info "Checking pod status..."
kubectl get pods -n "${NAMESPACE}"
echo ""

print_info "Checking services..."
kubectl get svc -n "${NAMESPACE}"
echo ""

print_info "Checking ingress..."
kubectl get ingress -n "${NAMESPACE}"
echo ""

print_info "Checking certificates..."
kubectl get certificate -n "${NAMESPACE}"
echo ""

# Step 8: Setup SSL/DNS (automated script generation)
echo "Step 8: Generating SSL/DNS installation script..."

# Get domain information
HOSTNAME=$(kubectl get ingress -n "${NAMESPACE}" -o jsonpath='{.items[0].spec.rules[0].host}')
APP_NAME=$(echo "$HOSTNAME" | cut -d'.' -f1)
DOMAIN=$(echo "$HOSTNAME" | cut -d'.' -f2-)

# Extract CA certificate
kubectl get secret local-ca-key-pair -n cert-manager \
    -o jsonpath='{.data.tls\.crt}' | base64 -d > /tmp/shop-manager-ca.crt

# Generate installation script for macOS/Linux with dynamic domain info
cat > /tmp/install-shop-manager-ssl.sh << EOF
#!/bin/bash
set -e

# Shop Manager SSL/DNS Installation Script
# Run this script with sudo to install CA certificate and DNS entries

CERT_FILE="/tmp/shop-manager-ca.crt"
DNS_FILE="/tmp/dns-entries.txt"

echo "🔐 Shop Manager SSL/DNS Installation"
echo "====================================="
echo ""
echo "Domain: ${DOMAIN}"
echo "App Name: ${APP_NAME}"
echo ""
echo "Installing:"
echo "  - https://${APP_NAME}.${DOMAIN}"
echo "  - https://api.${APP_NAME}.${DOMAIN}"
echo "  - https://auth.${APP_NAME}.${DOMAIN}"
echo ""

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    echo "Detected macOS"

    # Install certificate to system keychain
    echo "Installing CA certificate to system keychain..."
    sudo security add-trusted-cert -d -r trustRoot \
        -k /Library/Keychains/System.keychain \
        "${CERT_FILE}"

    # Install DNS entries
    echo "Installing DNS entries to /etc/hosts..."
    sudo sed -i.bak '/# Shop Manager DNS entries/,/^$/d' /etc/hosts 2>/dev/null || true
    echo "" | sudo tee -a /etc/hosts >/dev/null
    cat "${DNS_FILE}" | sudo tee -a /etc/hosts >/dev/null

    echo "✅ Installation complete!"
    echo ""
    echo "Please restart your browser for changes to take effect."

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    echo "Detected Linux"

    # Install certificate to system CA store
    echo "Installing CA certificate to system CA store..."
    sudo cp "${CERT_FILE}" /usr/local/share/ca-certificates/shop-manager-ca.crt
    sudo update-ca-certificates

    # Install DNS entries
    echo "Installing DNS entries to /etc/hosts..."
    sudo sed -i.bak '/# Shop Manager DNS entries/,/^$/d' /etc/hosts 2>/dev/null || true
    echo "" | sudo tee -a /etc/hosts >/dev/null
    cat "${DNS_FILE}" | sudo tee -a /etc/hosts >/dev/null

    echo "✅ Installation complete!"
    echo ""
    echo "Please restart your browser for changes to take effect."

else
    echo "❌ Unsupported OS: $OSTYPE"
    echo "Please install the certificate and DNS entries manually."
    exit 1
fi
EOF

chmod +x /tmp/install-shop-manager-ssl.sh

# Create DNS entries file
cat > /tmp/dns-entries.txt << EOF
# Shop Manager DNS entries for ${APP_NAME}
127.0.0.1 ${APP_NAME}.${DOMAIN}
127.0.0.1 api.${APP_NAME}.${DOMAIN}
127.0.0.1 auth.${APP_NAME}.${DOMAIN}
EOF

print_success "SSL/DNS installation script generated"
echo ""

# Step 9: Display completion message
echo "========================================="
echo "🎉 Shop Manager Installation Complete!"
echo "========================================="
echo ""
echo "📋 Next Steps:"
echo ""
echo "1. Install SSL certificate and DNS entries:"
echo "   sudo /tmp/install-shop-manager-ssl.sh"
echo ""
echo "2. Restart your browser"
echo ""
echo "3. Access Shop Manager:"
echo "   Frontend:  https://${APP_NAME}.${DOMAIN}"
echo "   API:       https://api.${APP_NAME}.${DOMAIN}/swagger-ui.html"
echo "   Keycloak:  https://auth.${APP_NAME}.${DOMAIN}"
echo ""
echo "4. Test with development credentials:"
echo "   Username: admin@shopmanager.com"
echo "   Password: admin123"
echo ""
echo "📄 Documentation:"
echo "   Deployment Guide: ../DEPLOYMENT_GUIDE.md"
echo "   Developer Guide:  ../DEVELOPER_GUIDE.md"
echo "   Helm Chart:       ./shop-manager/README.md"
echo ""
print_success "All done! 🚀"