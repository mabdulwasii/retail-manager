#!/bin/bash

# ============================================================================
# Shop Manager - Prerequisites Checker (Linux/macOS)
# ============================================================================
# This script validates that your system meets all requirements for
# installing Shop Manager on Kubernetes
# ============================================================================

echo "============================================================================"
echo "Shop Manager - Prerequisites Checker"
echo "============================================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;36m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
WARNINGS=0

# Function to print colored output
print_success() {
    echo -e "${GREEN}[PASS] $1${NC}"
    ((PASSED++))
}

print_error() {
    echo -e "${RED}[FAIL] $1${NC}"
    ((FAILED++))
}

print_warning() {
    echo -e "${YELLOW}[WARN] $1${NC}"
    ((WARNINGS++))
}

print_info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to get version
get_version() {
    local cmd=$1
    local version_flag=${2:---version}
    $cmd $version_flag 2>/dev/null | head -1
}

# Function to compare versions
version_ge() {
    printf '%s\n%s' "$2" "$1" | sort -V -C
}

echo "Checking System Prerequisites..."
echo "============================================================================"
echo ""

# 1. Check kubectl
print_info "Checking kubectl..."
if command_exists kubectl; then
    KUBECTL_VERSION=$(kubectl version --client --short 2>/dev/null | grep "Client Version" | awk '{print $3}' | sed 's/v//')
    if [ -z "$KUBECTL_VERSION" ]; then
        KUBECTL_VERSION=$(kubectl version --client -o json 2>/dev/null | grep -o '"gitVersion":"v[^"]*' | cut -d'"' -f4 | sed 's/v//')
    fi

    if [ -n "$KUBECTL_VERSION" ]; then
        print_success "kubectl is installed (version: $KUBECTL_VERSION)"

        # Check minimum version (1.24)
        if version_ge "$KUBECTL_VERSION" "1.24.0"; then
            print_success "kubectl version meets minimum requirement (≥1.24)"
        else
            print_warning "kubectl version $KUBECTL_VERSION is below recommended 1.24"
        fi
    else
        print_success "kubectl is installed"
    fi
else
    print_error "kubectl is not installed"
    echo "         Install: https://kubernetes.io/docs/tasks/tools/"
fi
echo ""

# 2. Check Helm
print_info "Checking Helm..."
if command_exists helm; then
    HELM_VERSION=$(helm version --short 2>/dev/null | awk '{print $1}' | sed 's/v//' | sed 's/+.*//')
    if [ -z "$HELM_VERSION" ]; then
        HELM_VERSION=$(helm version 2>/dev/null | grep -o 'Version:"v[^"]*' | cut -d'"' -f2 | sed 's/v//')
    fi

    if [ -n "$HELM_VERSION" ]; then
        print_success "Helm is installed (version: $HELM_VERSION)"

        # Check minimum version (3.10)
        if version_ge "$HELM_VERSION" "3.10.0"; then
            print_success "Helm version meets minimum requirement (≥3.10)"
        else
            print_warning "Helm version $HELM_VERSION is below recommended 3.10"
        fi
    else
        print_success "Helm is installed"
    fi
else
    print_error "Helm is not installed"
    echo "         Install: https://helm.sh/docs/intro/install/"
fi
echo ""

# 3. Check Kubernetes cluster connectivity
print_info "Checking Kubernetes cluster connectivity..."
if command_exists kubectl; then
    if kubectl cluster-info &>/dev/null; then
        print_success "Can connect to Kubernetes cluster"

        # Get cluster version
        CLUSTER_VERSION=$(kubectl version --short 2>/dev/null | grep "Server Version" | awk '{print $3}' | sed 's/v//')
        if [ -z "$CLUSTER_VERSION" ]; then
            CLUSTER_VERSION=$(kubectl version -o json 2>/dev/null | grep -o '"gitVersion":"v[^"]*' | tail -1 | cut -d'"' -f4 | sed 's/v//')
        fi

        if [ -n "$CLUSTER_VERSION" ]; then
            print_success "Cluster version: $CLUSTER_VERSION"

            if version_ge "$CLUSTER_VERSION" "1.24.0"; then
                print_success "Cluster version meets minimum requirement (≥1.24)"
            else
                print_warning "Cluster version $CLUSTER_VERSION is below recommended 1.24"
            fi
        fi
    else
        print_error "Cannot connect to Kubernetes cluster"
        echo "         Configure: kubectl config view"
    fi
else
    print_error "kubectl not available (skipping cluster checks)"
fi
echo ""

# 4. Check cluster resources
print_info "Checking cluster resources..."
if command_exists kubectl && kubectl cluster-info &>/dev/null; then
    # Get node info
    NODE_COUNT=$(kubectl get nodes --no-headers 2>/dev/null | wc -l | tr -d ' ')
    if [ "$NODE_COUNT" -gt 0 ]; then
        print_success "Cluster has $NODE_COUNT node(s)"

        # Check total CPU and memory
        TOTAL_CPU=$(kubectl top nodes 2>/dev/null | awk 'NR>1 {sum+=$3} END {print sum}' || echo "0")
        TOTAL_MEM=$(kubectl top nodes 2>/dev/null | awk 'NR>1 {sum+=$5} END {print sum}' || echo "0")

        if [ "$TOTAL_CPU" != "0" ] && [ "$TOTAL_MEM" != "0" ]; then
            print_success "Cluster resources: ${TOTAL_CPU} CPU, ${TOTAL_MEM}Mi memory"
        else
            print_warning "Could not determine cluster resources (metrics-server may not be installed)"
        fi

        # Check for sufficient resources
        ALLOCATABLE_CPU=$(kubectl get nodes -o json 2>/dev/null | grep -o '"cpu":"[^"]*' | cut -d'"' -f4 | sed 's/[^0-9]//g' | awk '{sum+=$1} END {print sum}')
        ALLOCATABLE_MEM=$(kubectl get nodes -o json 2>/dev/null | grep -o '"memory":"[^"]*' | cut -d'"' -f4 | sed 's/Ki//g' | awk '{sum+=$1/1024/1024} END {printf "%.0f", sum}')

        if [ -n "$ALLOCATABLE_CPU" ] && [ "$ALLOCATABLE_CPU" -ge 4 ]; then
            print_success "Cluster has sufficient CPU (${ALLOCATABLE_CPU} cores, minimum 4)"
        else
            print_warning "Cluster may not have sufficient CPU (found ${ALLOCATABLE_CPU:-?} cores, recommended 4+)"
        fi

        if [ -n "$ALLOCATABLE_MEM" ] && [ "$ALLOCATABLE_MEM" -ge 8 ]; then
            print_success "Cluster has sufficient memory (${ALLOCATABLE_MEM}GB, minimum 8GB)"
        else
            print_warning "Cluster may not have sufficient memory (found ${ALLOCATABLE_MEM:-?}GB, recommended 8GB+)"
        fi
    else
        print_error "No nodes found in cluster"
    fi
else
    print_warning "Cluster not accessible (skipping resource checks)"
fi
echo ""

# 5. Check internet connectivity
print_info "Checking internet connectivity..."
if command -v curl >/dev/null 2>&1; then
    if curl -s --max-time 5 https://registry-1.docker.io/v2/ >/dev/null; then
        print_success "Can reach Docker Hub (registry-1.docker.io)"
    else
        print_error "Cannot reach Docker Hub"
        echo "         Shop Manager requires internet access to pull images"
    fi
elif command -v wget >/dev/null 2>&1; then
    if wget -q --spider --timeout=5 https://registry-1.docker.io/v2/; then
        print_success "Can reach Docker Hub (registry-1.docker.io)"
    else
        print_error "Cannot reach Docker Hub"
        echo "         Shop Manager requires internet access to pull images"
    fi
else
    print_warning "curl/wget not available (skipping connectivity check)"
fi
echo ""

# 6. Check for existing installations
print_info "Checking for existing Shop Manager installations..."
if command_exists kubectl && kubectl cluster-info &>/dev/null; then
    if kubectl get namespace gomco &>/dev/null; then
        print_warning "Namespace 'gomco' already exists"
        if command_exists helm; then
            if helm list -n gomco 2>/dev/null | grep -q retail; then
                print_warning "Shop Manager (retail release) is already installed"
            fi
        fi
    else
        print_success "No existing installation found (gomco namespace does not exist)"
    fi
else
    print_warning "Cannot check for existing installations (cluster not accessible)"
fi
echo ""

# Summary
echo "============================================================================"
echo "Prerequisites Check Summary"
echo "============================================================================"
echo ""
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${YELLOW}Warnings: $WARNINGS${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}[OK] Your system meets the prerequisites for Shop Manager!${NC}"
    echo ""
    echo "Next Steps:"
    echo "  1. Edit values-template.yaml with your configuration"
    echo "  2. Run: ./install-kubernetes.sh"
    echo ""
else
    echo -e "${RED}[FAIL] Your system does not meet some prerequisites${NC}"
    echo ""
    echo "Please fix the failed checks above before installing Shop Manager"
    echo ""
    echo "For more information, see:"
    echo "  - PREREQUISITES.md"
    echo "  - QUICKSTART.md"
    echo ""
    exit 1
fi

if [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}Note: There are $WARNINGS warnings. Installation may still work,${NC}"
    echo -e "${YELLOW}but you may experience issues or degraded performance.${NC}"
    echo ""
fi
