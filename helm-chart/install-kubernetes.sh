#!/bin/bash
set -e

# ============================================================================
# Shop Manager - Kubernetes Automated Installation Script
# ============================================================================
# This script automates the complete installation of Shop Manager on Kubernetes
# using the public Helm chart from Docker Hub OCI registry.
# ============================================================================

echo "============================================================================"
echo "Shop Manager - Kubernetes Installation"
echo "============================================================================"
echo ""

# Configuration
NAMESPACE="${NAMESPACE:-gomco}"
RELEASE_NAME="${RELEASE_NAME:-retail}"
VALUES_FILE="${VALUES_FILE:-values-template.yaml}"
TIMEOUT="${TIMEOUT:-10m}"
CHART_REPO="oci://registry-1.docker.io/princely/shop-manager"

# Detect version from script directory name or use latest
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
VERSION=$(basename "$SCRIPT_DIR" | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' || echo "")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}[OK] $1${NC}"
}

print_error() {
    echo -e "${RED}[ERROR] $1${NC}"
}

print_info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Step 1: Verify prerequisites
echo "Step 1: Verifying prerequisites..."
if ! command_exists kubectl; then
    print_error "kubectl not found. Please install kubectl first."
    echo ""
    echo "Install kubectl: https://kubernetes.io/docs/tasks/tools/"
    exit 1
fi

if ! command_exists helm; then
    print_error "helm not found. Please install helm first."
    echo ""
    echo "Install Helm: https://helm.sh/docs/intro/install/"
    exit 1
fi

print_success "Prerequisites verified (kubectl and helm found)"
echo ""

# Step 2: Check Kubernetes cluster connectivity
echo "Step 2: Checking Kubernetes cluster connectivity..."
if ! kubectl cluster-info &>/dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    echo ""
    echo "Configure kubectl: kubectl config view"
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
    kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all 2>/dev/null || true
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

# Step 6: Prompt to edit values file
echo "Step 6: Configuration"
echo "============================================================================"
print_warning "IMPORTANT: You must edit ${VALUES_FILE} before installation!"
echo ""
echo "Required changes:"
echo "  - domain: YOUR-DOMAIN.com → your-actual-domain.com"
echo "  - All passwords marked with CHANGE-ME"
echo "  - Email for TLS certificates"
echo ""
read -p "Have you edited ${VALUES_FILE}? (y/N): " EDITED

if [[ ! "$EDITED" =~ ^[Yy]$ ]]; then
    print_warning "Please edit ${VALUES_FILE} and run this script again."
    echo ""
    echo "Example:"
    echo "  nano ${VALUES_FILE}"
    echo "  ./install-kubernetes.sh"
    echo ""
    exit 0
fi
echo ""

# Verify values file exists
if [ ! -f "${VALUES_FILE}" ]; then
    print_error "Values file not found: ${VALUES_FILE}"
    exit 1
fi

# Step 7: Deploy Shop Manager with Helm
echo "Step 7: Deploying Shop Manager with Helm..."
print_info "Release name: ${RELEASE_NAME}"
print_info "Namespace: ${NAMESPACE}"
print_info "Values file: ${VALUES_FILE}"
print_info "Chart repository: ${CHART_REPO}"
if [ -n "$VERSION" ]; then
    print_info "Version: ${VERSION}"
fi
print_info "Timeout: ${TIMEOUT}"
echo ""

# Build helm install command
HELM_CMD="helm install ${RELEASE_NAME} ${CHART_REPO}"
if [ -n "$VERSION" ]; then
    HELM_CMD="${HELM_CMD} --version ${VERSION}"
fi
HELM_CMD="${HELM_CMD} -n ${NAMESPACE} -f ${VALUES_FILE} --wait --timeout ${TIMEOUT}"

if helm list -n "${NAMESPACE}" | grep -q "${RELEASE_NAME}"; then
    print_info "Helm release '${RELEASE_NAME}' already exists. Upgrading..."
    HELM_CMD=$(echo "$HELM_CMD" | sed 's/install/upgrade/')
    eval "$HELM_CMD"
    print_success "Helm release upgraded"
else
    print_info "Installing new Helm release..."
    print_info "This may take several minutes as Kubernetes pulls Docker images..."
    eval "$HELM_CMD"
    print_success "Helm release installed"
fi
echo ""

# Step 8: Verify deployment
echo "Step 8: Verifying deployment..."
print_info "Checking pod status..."
kubectl get pods -n "${NAMESPACE}"
echo ""

print_info "Checking services..."
kubectl get svc -n "${NAMESPACE}"
echo ""

print_info "Checking ingress..."
kubectl get ingress -n "${NAMESPACE}"
echo ""

# Step 9: Display completion message
echo "============================================================================"
echo "[OK] Shop Manager Installation Complete!"
echo "============================================================================"
echo ""
print_info "Your Shop Manager is now deployed!"
echo ""

# Get domain from values file
DOMAIN=$(grep -A 10 "^global:" "${VALUES_FILE}" | grep "domain:" | awk '{print $2}' | tr -d '"')
APP_NAME=$(grep -A 10 "^global:" "${VALUES_FILE}" | grep "appName:" | awk '{print $2}' | tr -d '"')
TEST_USERS_ENABLED=$(grep -A 5 "testUsers:" "${VALUES_FILE}" | grep "enabled:" | awk '{print $2}' | tr -d ' ')

echo "Access URLs:"
echo "  Frontend:  https://${APP_NAME}.${DOMAIN}"
echo "  API:       https://api.${APP_NAME}.${DOMAIN}/swagger-ui.html"
echo "  Keycloak:  https://auth.${APP_NAME}.${DOMAIN}"
echo ""

# Check DNS resolution
print_info "Checking DNS resolution..."
DNS_ISSUE=false
for host in "${APP_NAME}.${DOMAIN}" "api.${APP_NAME}.${DOMAIN}" "auth.${APP_NAME}.${DOMAIN}"; do
    if ! host "$host" &>/dev/null && ! nslookup "$host" &>/dev/null; then
        DNS_ISSUE=true
        break
    fi
done

if [ "$DNS_ISSUE" = true ]; then
    print_warning "DNS resolution failed for ${DOMAIN}"
    echo ""
    print_info "For local testing, add these entries to /etc/hosts:"
    echo ""
    echo "  # Get your cluster's ingress IP:"
    echo "  kubectl get ingress -n ${NAMESPACE}"
    echo ""
    echo "  # Add to /etc/hosts (replace <INGRESS-IP> with actual IP):"
    echo "  <INGRESS-IP> ${APP_NAME}.${DOMAIN}"
    echo "  <INGRESS-IP> api.${APP_NAME}.${DOMAIN}"
    echo "  <INGRESS-IP> auth.${APP_NAME}.${DOMAIN}"
    echo ""
    echo "  # Example command (requires sudo):"
    echo "  echo \"<INGRESS-IP> ${APP_NAME}.${DOMAIN} api.${APP_NAME}.${DOMAIN} auth.${APP_NAME}.${DOMAIN}\" | sudo tee -a /etc/hosts"
    echo ""
else
    print_success "DNS resolution successful"
    echo ""
fi

# Show login credentials
echo "Login Credentials:"
echo ""
echo "  System Admin (always created):"
echo "    superadmin / changeme"
echo ""

if [ "$TEST_USERS_ENABLED" = "true" ]; then
    echo "  Test Users (testUsers.enabled=true):"
    echo "    admin@shopmanager.com / DevAdmin@2024!Test (Tenant Admin)"
    echo "    manager@shopmanager.com / DevManager@2024!Test (Manager)"
    echo "    employee@shopmanager.com / DevEmployee@2024!Test (Employee)"
    echo "    + 5 more test users (see keycloak-users.json)"
    echo ""
    print_warning "IMPORTANT: Change default passwords after first login!"
    print_warning "IMPORTANT: Disable test users in production (set testUsers.enabled: false)"
    echo ""
else
    print_warning "Test users are DISABLED (testUsers.enabled=false)"
    print_info "To create users manually in Keycloak:"
    echo "  1. Access Keycloak admin: https://auth.${APP_NAME}.${DOMAIN}"
    echo "  2. Login with Keycloak admin credentials (from values file)"
    echo "  3. Select realm: ${APP_NAME}"
    echo "  4. Create users with appropriate roles"
    echo ""
    print_info "Or enable test users by setting testUsers.enabled: true in values file"
    echo ""
fi

echo "Useful Commands:"
echo "  Check status: kubectl get pods -n ${NAMESPACE}"
echo "  View logs:    kubectl logs -f deployment/shop-manager-backend -n ${NAMESPACE}"
echo "  Uninstall:    ./uninstall-kubernetes.sh"
echo ""

echo "Documentation:"
echo "  - QUICKSTART.md"
echo "  - PREREQUISITES.md"
echo ""

print_success "All done! Enjoy Shop Manager!"
