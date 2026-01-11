#!/bin/bash
set -e

# ============================================================================
# Shop Manager - Kubernetes Uninstallation Script
# ============================================================================
# This script removes Shop Manager from your Kubernetes cluster
# ============================================================================

echo "============================================================================"
echo "Shop Manager - Kubernetes Uninstallation"
echo "============================================================================"
echo ""

# Configuration
NAMESPACE="${NAMESPACE:-gomco}"
RELEASE_NAME="${RELEASE_NAME:-retail}"

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

# Check prerequisites
if ! command_exists kubectl; then
    print_error "kubectl not found"
    exit 1
fi

if ! command_exists helm; then
    print_error "helm not found"
    exit 1
fi

# Check if cluster is accessible
if ! kubectl cluster-info &>/dev/null; then
    print_error "Cannot connect to Kubernetes cluster"
    exit 1
fi

print_info "Connected to Kubernetes cluster"
echo ""

# Check if namespace exists
if ! kubectl get namespace "${NAMESPACE}" &>/dev/null; then
    print_warning "Namespace '${NAMESPACE}' does not exist"
    echo ""
    print_info "Shop Manager may not be installed or already uninstalled"
    exit 0
fi

# Display current resources
echo "Current Shop Manager Resources:"
echo "============================================================================"
kubectl get pods,svc,ingress -n "${NAMESPACE}" 2>/dev/null || true
echo ""

# Confirm uninstallation
print_warning "This will remove Shop Manager from your cluster!"
echo ""
read -p "Are you sure you want to uninstall? (y/N): " CONFIRM

if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    print_info "Uninstallation cancelled"
    exit 0
fi
echo ""

# Step 1: Uninstall Helm release
echo "Step 1: Uninstalling Helm release..."
if helm list -n "${NAMESPACE}" | grep -q "${RELEASE_NAME}"; then
    helm uninstall "${RELEASE_NAME}" -n "${NAMESPACE}"
    print_success "Helm release '${RELEASE_NAME}' uninstalled"
else
    print_warning "Helm release '${RELEASE_NAME}' not found"
fi
echo ""

# Step 2: Ask about namespace deletion
echo "Step 2: Namespace cleanup"
print_warning "Delete namespace '${NAMESPACE}' and all resources?"
read -p "This will remove ALL data. Continue? (y/N): " DELETE_NS

if [[ "$DELETE_NS" =~ ^[Yy]$ ]]; then
    kubectl delete namespace "${NAMESPACE}"
    print_success "Namespace '${NAMESPACE}' deleted"
else
    print_info "Namespace '${NAMESPACE}' kept"
    print_warning "Some resources may still exist in this namespace"
fi
echo ""

# Step 3: Ask about persistent volumes
echo "Step 3: Persistent volumes"
echo "Checking for persistent volume claims..."
PVCS=$(kubectl get pvc -n "${NAMESPACE}" 2>/dev/null | grep -v NAME | wc -l || echo "0")

if [ "$PVCS" -gt 0 ]; then
    print_warning "Found ${PVCS} persistent volume claims with data"
    print_warning "These may contain database data and backups"
    echo ""
    read -p "Delete persistent volumes and ALL DATA? (y/N): " DELETE_PVC

    if [[ "$DELETE_PVC" =~ ^[Yy]$ ]]; then
        kubectl delete pvc --all -n "${NAMESPACE}" 2>/dev/null || true
        print_success "Persistent volumes deleted"
    else
        print_info "Persistent volumes kept"
        print_warning "You may need to manually delete PVCs later"
    fi
else
    print_info "No persistent volume claims found"
fi
echo ""

# Step 4: Completion
echo "============================================================================"
print_success "Shop Manager Uninstallation Complete!"
echo "============================================================================"
echo ""

print_info "Remaining cluster resources:"
echo "  cert-manager (shared): kubectl get pods -n cert-manager"
echo "  ingress-nginx (shared): kubectl get pods -n ingress-nginx"
echo ""

print_info "To completely remove Shop Manager infrastructure:"
echo "  kubectl delete namespace cert-manager"
echo "  kubectl delete namespace ingress-nginx"
echo ""

print_warning "Note: Only delete cert-manager and ingress-nginx if no other"
print_warning "applications are using them!"
echo ""

print_success "Done!"
