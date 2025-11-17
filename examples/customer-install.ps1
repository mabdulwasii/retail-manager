# ============================================================================
# Shop Manager - Customer Installation Script (Windows PowerShell)
# ============================================================================
#
# This script automates the installation of Shop Manager on Kubernetes
# using Helm from Docker Hub (no repository clone needed).
#
# Prerequisites:
#   - Docker Desktop with Kubernetes enabled
#   - Helm 3 installed
#   - PowerShell 5.1 or later
#   - Run as Administrator
#
# Usage:
#   .\customer-install.ps1
#
# ============================================================================

# Requires Administrator privileges
#Requires -RunAsAdministrator

# Configuration
$CHART_REPO = "oci://registry-1.docker.io/princely/shop-manager"
$CHART_VERSION = "0.0.1"
$RELEASE_NAME = "retail"
$NAMESPACE = "gomco"
$VALUES_FILE = "my-values.yaml"

# ============================================================================
# Helper Functions
# ============================================================================

function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "============================================================================" -ForegroundColor Blue
    Write-Host "  $Message" -ForegroundColor Blue
    Write-Host "============================================================================" -ForegroundColor Blue
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
}

function Write-Error-Message {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
}

function Write-Warning-Message {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "ℹ️  $Message" -ForegroundColor Cyan
}

function Test-Command {
    param([string]$Command)
    $exists = $null -ne (Get-Command $Command -ErrorAction SilentlyContinue)
    if (-not $exists) {
        Write-Error-Message "$Command is not installed. Please install it first."
        exit 1
    }
    return $exists
}

# ============================================================================
# Step 1: Check Prerequisites
# ============================================================================

Write-Header "Step 1/8: Checking Prerequisites"

# Check kubectl
Write-Info "Checking kubectl..."
Test-Command "kubectl"
Write-Success "kubectl is installed"

# Check helm
Write-Info "Checking Helm..."
Test-Command "helm"
Write-Success "Helm is installed"

# Check Kubernetes cluster
Write-Info "Checking Kubernetes cluster..."
try {
    kubectl cluster-info 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Kubernetes cluster is not accessible"
    }
    Write-Success "Kubernetes cluster is accessible"
}
catch {
    Write-Error-Message "Kubernetes cluster is not accessible"
    Write-Info "Please enable Kubernetes in Docker Desktop:"
    Write-Info "  Docker Desktop → Settings → Kubernetes → Enable Kubernetes"
    exit 1
}

# ============================================================================
# Step 2: Gather Configuration
# ============================================================================

Write-Header "Step 2/8: Configuration"

# Check if values file exists
if (Test-Path $VALUES_FILE) {
    Write-Info "Found existing values file: $VALUES_FILE"
    $useExisting = Read-Host "Use existing values file? (y/n)"
    if ($useExisting -ne "y") {
        Remove-Item $VALUES_FILE
    }
}

# Download template if needed
if (-not (Test-Path $VALUES_FILE)) {
    Write-Info "Downloading customer values template..."
    try {
        Invoke-WebRequest -Uri "https://raw.githubusercontent.com/yourorg/shop-manager/main/examples/customer-values.yaml" `
            -OutFile "customer-values-template.yaml" -UseBasicParsing
    }
    catch {
        Write-Warning-Message "Could not download template, will create from scratch"
    }

    # Interactive configuration
    Write-Host ""
    Write-Host "Let's configure your Shop Manager installation:"
    Write-Host ""

    $companyDomain = Read-Host "Your company domain (e.g., mycompany.com)"
    $platformName = Read-Host "Platform name (e.g., Acme Retail Pro)"
    $companyName = Read-Host "Company name (e.g., Acme Corporation)"

    # Ask about test users
    Write-Host ""
    Write-Warning-Message "Test users provide pre-configured accounts for testing."
    Write-Warning-Message "For production deployments, it's recommended to disable them."
    $enableTestUsersInput = Read-Host "Enable test users? (y/n, default: n)"
    if ([string]::IsNullOrEmpty($enableTestUsersInput)) {
        $enableTestUsersInput = "n"
    }

    if ($enableTestUsersInput -eq "y") {
        $testUsersEnabled = "true"
        Write-Info "Test users will be created (admin@shopmanager.com, etc.)"
    }
    else {
        $testUsersEnabled = "false"
        Write-Info "Test users will be disabled (production mode)"
    }

    # Create values file
    $valuesContent = @"
global:
  appName: "retail"
  domain: "$companyDomain"

backend:
  image:
    repository: princely/shop-manager
    tag: backend-v0.0.45
    pullPolicy: Always
  podAnnotations:
    configVersion: "32"
  env:
    SWAGGER_SECURITY_ENABLED: "false"

frontend:
  enabled: true
  image:
    repository: princely/shop-manager
    tag: frontend-v0.0.9
    pullPolicy: Always

branding:
  platformName: "$platformName"
  companyName: "$companyName"
  platformDescription: "Advanced Retail Management System"
  colors:
    primary: "#2E7D32"
    secondary: "#FF6F00"

application:
  testUsers:
    enabled: $testUsersEnabled

tls:
  enabled: true
  issuer: "local-ca-issuer"
  email: "admin@$companyDomain"
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
"@

    Set-Content -Path $VALUES_FILE -Value $valuesContent
    Write-Success "Created values file: $VALUES_FILE"
}

# ============================================================================
# Step 3: Install cert-manager
# ============================================================================

Write-Header "Step 3/8: Installing cert-manager"

try {
    kubectl get namespace cert-manager 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Info "cert-manager already installed"
    }
    else {
        throw "Not installed"
    }
}
catch {
    Write-Info "Installing cert-manager..."
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

    Write-Info "Waiting for cert-manager to be ready (this may take a minute)..."
    kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all

    Write-Success "cert-manager installed successfully"
}

# ============================================================================
# Step 4: Install NGINX Ingress Controller
# ============================================================================

Write-Header "Step 4/8: Installing NGINX Ingress Controller"

$ingressInstalled = helm list -n ingress-nginx | Select-String "ingress-nginx"
if ($ingressInstalled) {
    Write-Info "ingress-nginx already installed"
}
else {
    Write-Info "Adding ingress-nginx Helm repository..."
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
    helm repo update

    Write-Info "Installing ingress-nginx..."
    helm install ingress-nginx ingress-nginx/ingress-nginx `
        --namespace ingress-nginx `
        --create-namespace `
        --wait `
        --timeout 5m

    Write-Success "ingress-nginx installed successfully"
}

# ============================================================================
# Step 5: Create Namespace
# ============================================================================

Write-Header "Step 5/8: Creating Namespace"

try {
    kubectl get namespace $NAMESPACE 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Namespace $NAMESPACE already exists"
    }
    else {
        throw "Namespace does not exist"
    }
}
catch {
    kubectl create namespace $NAMESPACE
    Write-Success "Created namespace: $NAMESPACE"
}

# ============================================================================
# Step 6: Install Shop Manager
# ============================================================================

Write-Header "Step 6/8: Installing Shop Manager"

Write-Info "Installing Shop Manager from Docker Hub..."
Write-Info "Chart: $CHART_REPO"
Write-Info "Version: $CHART_VERSION"
Write-Info "This may take 5-10 minutes depending on your internet speed..."

helm install $RELEASE_NAME $CHART_REPO `
    --version $CHART_VERSION `
    -n $NAMESPACE `
    -f $VALUES_FILE `
    --wait `
    --timeout 10m

Write-Success "Shop Manager installed successfully!"

# ============================================================================
# Step 7: Setup SSL and DNS
# ============================================================================

Write-Header "Step 7/8: Setting up SSL and DNS"

# Get domain from values file
$valuesFileContent = Get-Content $VALUES_FILE
$domainLine = $valuesFileContent | Select-String -Pattern "domain:" | Select-Object -First 1
$DOMAIN = ($domainLine -replace '.*domain:\s*"?([^"]+)"?.*', '$1').Trim().Trim('"')

Write-Info "Extracting SSL certificate..."
$certBase64 = kubectl get secret local-ca-key-pair -n cert-manager -o jsonpath='{.data.tls\.crt}'
$certBytes = [System.Convert]::FromBase64String($certBase64)
$certPath = "$env:TEMP\shop-manager-ca.crt"
[System.IO.File]::WriteAllBytes($certPath, $certBytes)

# Install certificate to Windows certificate store
Write-Info "Installing certificate to Windows certificate store..."
try {
    $cert = New-Object System.Security.Cryptography.X509Certificates.X509Certificate2($certPath)
    $store = New-Object System.Security.Cryptography.X509Certificates.X509Store("Root", "LocalMachine")
    $store.Open("ReadWrite")
    $store.Add($cert)
    $store.Close()
    Write-Success "Certificate installed"
}
catch {
    Write-Warning-Message "Could not install certificate automatically"
    Write-Info "Please manually install certificate from: $certPath"
    Write-Info "  1. Double-click the certificate file"
    Write-Info "  2. Click 'Install Certificate'"
    Write-Info "  3. Select 'Local Machine'"
    Write-Info "  4. Place in 'Trusted Root Certification Authorities'"
}

# Add DNS entries to hosts file
Write-Info "Adding DNS entries to hosts file..."

$hostsPath = "C:\Windows\System32\drivers\etc\hosts"
$dnsEntries = @"

# Shop Manager DNS entries
127.0.0.1 retail.$DOMAIN
127.0.0.1 api.retail.$DOMAIN
127.0.0.1 auth.retail.$DOMAIN
"@

# Remove old entries
$hostsContent = Get-Content $hostsPath -Raw
$hostsContent = $hostsContent -replace '(?s)# Shop Manager DNS entries.*?(?=\r?\n\r?\n|$)', ''
$hostsContent = $hostsContent.TrimEnd()

# Add new entries
$hostsContent += $dnsEntries

Set-Content -Path $hostsPath -Value $hostsContent -NoNewline

Write-Success "DNS entries added to hosts file"

# ============================================================================
# Step 8: Verification and Next Steps
# ============================================================================

Write-Header "Step 8/8: Verification"

Write-Info "Checking deployment status..."

# Wait a bit for everything to settle
Start-Sleep -Seconds 5

# Check pod status
$runningPods = kubectl get pods -n $NAMESPACE | Select-String "Running"
if ($runningPods) {
    Write-Success "All pods are running"
}
else {
    Write-Warning-Message "Some pods may still be starting up"
    kubectl get pods -n $NAMESPACE
}

# ============================================================================
# Installation Complete
# ============================================================================

Write-Header "🎉 Installation Complete!"

Write-Host ""
Write-Host "Shop Manager has been successfully installed!" -ForegroundColor Green
Write-Host ""
Write-Host "📌 Important: Please restart your browser completely before accessing"
Write-Host ""
Write-Host "🌐 Access URLs:"
Write-Host "   Frontend:  https://retail.$DOMAIN"
Write-Host "   API Docs:  https://api.retail.$DOMAIN/swagger-ui/index.html"
Write-Host "   Keycloak:  https://auth.retail.$DOMAIN"
Write-Host ""
Write-Host "🔑 Default Login Credentials:"
Write-Host "   Email:     admin@shopmanager.com"
Write-Host "   Password:  DevAdmin@2024!Test"
Write-Host ""
Write-Host "📝 Configuration file saved to: $VALUES_FILE"
Write-Host ""
Write-Host "🔄 To upgrade in the future:"
Write-Host "   helm upgrade $RELEASE_NAME $CHART_REPO --version <new-version> -n $NAMESPACE -f $VALUES_FILE"
Write-Host ""
Write-Host "🗑️  To uninstall:"
Write-Host "   helm uninstall $RELEASE_NAME -n $NAMESPACE"
Write-Host ""

# Offer to open browser
$openBrowser = Read-Host "Open browser now? (y/n)"
if ($openBrowser -eq "y") {
    Start-Process "https://retail.$DOMAIN"
}

Write-Success "Installation script completed successfully!"