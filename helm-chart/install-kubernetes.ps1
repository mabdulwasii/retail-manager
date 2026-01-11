# ============================================================================
# install-kubernetes.ps1 - Shop Manager Kubernetes Installation (Windows)
# ============================================================================
# This script automates the complete installation of Shop Manager on Kubernetes
# using the public Helm chart from Docker Hub OCI registry.
# ============================================================================
#
# USAGE:
#   Right-click this file and select "Run with PowerShell"
#   OR open PowerShell in this directory and run:
#     .\install-kubernetes.ps1
#
# IMPORTANT:
#   If you get "execution policy" errors, run this first:
#     Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
#
# ============================================================================

[CmdletBinding()]
param()

# Set error action preference
$ErrorActionPreference = "Stop"

# Configuration
$NAMESPACE = if ($env:NAMESPACE) { $env:NAMESPACE } else { "gomco" }
$RELEASE_NAME = if ($env:RELEASE_NAME) { $env:RELEASE_NAME } else { "retail" }
$VALUES_FILE = if ($env:VALUES_FILE) { $env:VALUES_FILE } else { "values-template.yaml" }
$TIMEOUT = if ($env:TIMEOUT) { $env:TIMEOUT } else { "10m" }
$CHART_REPO = "oci://registry-1.docker.io/princely/shop-manager"

# Detect version from directory name
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DirName = Split-Path -Leaf $ScriptDir
$VERSION = if ($DirName -match '(\d+\.\d+\.\d+)') { $Matches[1] } else { "" }

# Functions
function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "============================================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "============================================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Success {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-ErrorMessage {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

# Function to check if command exists
function Test-Command {
    param([string]$Command)
    try {
        Get-Command $Command -ErrorAction Stop | Out-Null
        return $true
    } catch {
        return $false
    }
}

# Main installation function
function Install-ShopManager {
    Clear-Host
    Write-Header "Shop Manager - Kubernetes Installation (Windows)"

    # Step 1: Check PowerShell execution policy
    $executionPolicy = Get-ExecutionPolicy
    if ($executionPolicy -eq "Restricted") {
        Write-Warning "PowerShell execution policy is Restricted."
        Write-Host "To allow this script to run, please execute:" -ForegroundColor Yellow
        Write-Host "  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser" -ForegroundColor Cyan
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Success "PowerShell execution policy: $executionPolicy"
    Write-Host ""

    # Step 2: Verify prerequisites
    Write-Header "Step 1: Verifying Prerequisites"

    if (!(Test-Command "kubectl")) {
        Write-ErrorMessage "kubectl not found. Please install kubectl first."
        Write-Host ""
        Write-Host "Install kubectl: https://kubernetes.io/docs/tasks/tools/" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Success "kubectl is installed"

    if (!(Test-Command "helm")) {
        Write-ErrorMessage "helm not found. Please install Helm first."
        Write-Host ""
        Write-Host "Install Helm: https://helm.sh/docs/intro/install/" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Success "Helm is installed"
    Write-Host ""

    # Step 3: Check Kubernetes cluster connectivity
    Write-Header "Step 2: Checking Kubernetes Cluster Connectivity"
    try {
        kubectl cluster-info | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "kubectl cluster-info failed"
        }
        Write-Success "Connected to Kubernetes cluster"
    } catch {
        Write-ErrorMessage "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        Write-Host ""
        Write-Host "Configure kubectl: kubectl config view" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host ""

    # Step 4: Install cert-manager
    Write-Header "Step 3: Checking cert-manager Installation"
    try {
        kubectl get namespace cert-manager 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Info "cert-manager namespace already exists"
        } else {
            Write-Info "Installing cert-manager..."
            kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.2/cert-manager.yaml

            Write-Info "Waiting for cert-manager to be ready..."
            kubectl wait --for=condition=available --timeout=300s deployment -n cert-manager --all 2>$null
            Write-Success "cert-manager installed and ready"
        }
    } catch {
        Write-Warning "cert-manager check failed: $_"
    }
    Write-Host ""

    # Step 5: Install NGINX Ingress Controller
    Write-Header "Step 4: Checking NGINX Ingress Controller Installation"
    try {
        kubectl get namespace ingress-nginx 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Info "ingress-nginx namespace already exists"
        } else {
            Write-Info "Installing NGINX Ingress Controller..."
            helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx 2>$null | Out-Null
            helm repo update 2>$null | Out-Null

            helm install ingress-nginx ingress-nginx/ingress-nginx `
                --namespace ingress-nginx `
                --create-namespace `
                --set controller.service.type=LoadBalancer `
                --wait `
                --timeout 5m

            Write-Success "NGINX Ingress Controller installed"
        }
    } catch {
        Write-Warning "NGINX Ingress Controller check failed: $_"
    }
    Write-Host ""

    # Step 6: Create namespace
    Write-Header "Step 5: Creating Namespace '$NAMESPACE'"
    try {
        kubectl get namespace $NAMESPACE 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) {
            Write-Info "Namespace '$NAMESPACE' already exists"
        } else {
            kubectl create namespace $NAMESPACE
            Write-Success "Namespace '$NAMESPACE' created"
        }
    } catch {
        Write-ErrorMessage "Failed to create namespace: $_"
    }
    Write-Host ""

    # Step 7: Prompt to edit values file
    Write-Header "Step 6: Configuration"
    Write-Warning "IMPORTANT: You must edit $VALUES_FILE before installation!"
    Write-Host ""
    Write-Host "Required changes:" -ForegroundColor Yellow
    Write-Host "  - domain: YOUR-DOMAIN.com -> your-actual-domain.com"
    Write-Host "  - All passwords marked with CHANGE-ME"
    Write-Host "  - Email for TLS certificates"
    Write-Host ""

    $edited = Read-Host "Have you edited $VALUES_FILE? (y/N)"
    if ($edited -notmatch '^[Yy]$') {
        Write-Warning "Please edit $VALUES_FILE and run this script again."
        Write-Host ""
        Write-Host "Example:" -ForegroundColor Yellow
        Write-Host "  notepad $VALUES_FILE"
        Write-Host "  .\install-kubernetes.ps1"
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 0
    }
    Write-Host ""

    # Verify values file exists
    if (!(Test-Path $VALUES_FILE)) {
        Write-ErrorMessage "Values file not found: $VALUES_FILE"
        Read-Host "Press Enter to exit"
        exit 1
    }

    # Step 8: Deploy Shop Manager with Helm
    Write-Header "Step 7: Deploying Shop Manager with Helm"
    Write-Info "Release name: $RELEASE_NAME"
    Write-Info "Namespace: $NAMESPACE"
    Write-Info "Values file: $VALUES_FILE"
    Write-Info "Chart repository: $CHART_REPO"
    if ($VERSION) {
        Write-Info "Version: $VERSION"
    }
    Write-Info "Timeout: $TIMEOUT"
    Write-Host ""

    try {
        # Check if release exists
        helm list -n $NAMESPACE 2>$null | Select-String -Pattern $RELEASE_NAME -Quiet
        $releaseExists = $LASTEXITCODE -eq 0

        if ($releaseExists) {
            Write-Info "Helm release '$RELEASE_NAME' already exists. Upgrading..."
            $action = "upgrade"
        } else {
            Write-Info "Installing new Helm release..."
            Write-Info "This may take several minutes as Kubernetes pulls Docker images..."
            $action = "install"
        }

        # Build helm command
        $helmArgs = @(
            $action,
            $RELEASE_NAME,
            $CHART_REPO,
            "-n", $NAMESPACE,
            "-f", $VALUES_FILE,
            "--wait",
            "--timeout", $TIMEOUT
        )

        if ($VERSION) {
            $helmArgs += "--version"
            $helmArgs += $VERSION
        }

        & helm $helmArgs

        if ($LASTEXITCODE -ne 0) {
            throw "Helm $action failed"
        }

        Write-Success "Helm release $action completed successfully"
    } catch {
        Write-ErrorMessage "Failed to deploy Shop Manager: $_"
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Host ""

    # Step 9: Verify deployment
    Write-Header "Step 8: Verifying Deployment"
    Write-Info "Checking pod status..."
    kubectl get pods -n $NAMESPACE
    Write-Host ""

    Write-Info "Checking services..."
    kubectl get svc -n $NAMESPACE
    Write-Host ""

    Write-Info "Checking ingress..."
    kubectl get ingress -n $NAMESPACE
    Write-Host ""

    # Step 10: Display completion message
    Write-Header "[OK] Shop Manager Installation Complete!"
    Write-Info "Your Shop Manager is now deployed!"
    Write-Host ""

    # Get domain from values file
    try {
        $valuesContent = Get-Content $VALUES_FILE -Raw
        $DOMAIN = if ($valuesContent -match 'domain:\s+(\S+)') { $Matches[1] -replace '[''"]','' } else { "YOUR-DOMAIN.com" }
        $APP_NAME = if ($valuesContent -match 'appName:\s+(\S+)') { $Matches[1] -replace '[''"]','' } else { "retail" }
        $TEST_USERS_ENABLED = if ($valuesContent -match 'testUsers:[\s\S]*?enabled:\s+(\S+)') { $Matches[1] -replace '[''"]','' } else { "false" }

        Write-Host "Access URLs:" -ForegroundColor Cyan
        Write-Host "  Frontend:  https://$APP_NAME.$DOMAIN"
        Write-Host "  API:       https://api.$APP_NAME.$DOMAIN/swagger-ui.html"
        Write-Host "  Keycloak:  https://auth.$APP_NAME.$DOMAIN"
        Write-Host ""

        # Check DNS resolution
        Write-Info "Checking DNS resolution..."
        $dnsIssue = $false
        $hosts = @("$APP_NAME.$DOMAIN", "api.$APP_NAME.$DOMAIN", "auth.$APP_NAME.$DOMAIN")
        foreach ($host in $hosts) {
            try {
                [System.Net.Dns]::GetHostEntry($host) | Out-Null
            } catch {
                $dnsIssue = $true
                break
            }
        }

        if ($dnsIssue) {
            Write-Warning "DNS resolution failed for $DOMAIN"
            Write-Host ""
            Write-Info "For local testing, add these entries to hosts file:"
            Write-Host ""
            Write-Host "  # Get your cluster's ingress IP:" -ForegroundColor Yellow
            Write-Host "  kubectl get ingress -n $NAMESPACE"
            Write-Host ""
            Write-Host "  # Add to C:\Windows\System32\drivers\etc\hosts (replace <INGRESS-IP>):" -ForegroundColor Yellow
            Write-Host "  <INGRESS-IP> $APP_NAME.$DOMAIN"
            Write-Host "  <INGRESS-IP> api.$APP_NAME.$DOMAIN"
            Write-Host "  <INGRESS-IP> auth.$APP_NAME.$DOMAIN"
            Write-Host ""
            Write-Host "  # Example PowerShell command (requires Administrator):" -ForegroundColor Yellow
            Write-Host "  Add-Content -Path C:\Windows\System32\drivers\etc\hosts -Value '<INGRESS-IP> $APP_NAME.$DOMAIN api.$APP_NAME.$DOMAIN auth.$APP_NAME.$DOMAIN'"
            Write-Host ""
        } else {
            Write-Success "DNS resolution successful"
            Write-Host ""
        }

        # Show credentials based on testUsers setting
        if ($TEST_USERS_ENABLED -eq "true") {
            Write-Host "Default Test Credentials:" -ForegroundColor Cyan
            Write-Host "  System Admin: superadmin / changeme"
            Write-Host "  Tenant Admin: admin@shopmanager.com / admin123"
            Write-Host ""
            Write-Warning "IMPORTANT: Change default passwords after first login!"
            Write-Warning "IMPORTANT: Disable test users in production (set testUsers.enabled: false)"
            Write-Host ""
        } else {
            Write-Warning "Test users are DISABLED"
            Write-Info "You need to create users manually in Keycloak:"
            Write-Host "  1. Access Keycloak admin: https://auth.$APP_NAME.$DOMAIN"
            Write-Host "  2. Login with Keycloak admin credentials (from values file)"
            Write-Host "  3. Select realm: $APP_NAME"
            Write-Host "  4. Create users with appropriate roles"
            Write-Host ""
            Write-Info "Or enable test users by setting testUsers.enabled: true in values file"
            Write-Host ""
        }
    } catch {
        Write-Warning "Could not parse configuration from values file"
    }

    Write-Host "Useful Commands:" -ForegroundColor Cyan
    Write-Host "  Check status: kubectl get pods -n $NAMESPACE"
    Write-Host "  View logs:    kubectl logs -f deployment/shop-manager-backend -n $NAMESPACE"
    Write-Host "  Uninstall:    .\uninstall-kubernetes.ps1"
    Write-Host ""

    Write-Host "Documentation:" -ForegroundColor Cyan
    Write-Host "  - QUICKSTART.md"
    Write-Host "  - PREREQUISITES.md"
    Write-Host ""

    Write-Success "All done! Enjoy Shop Manager!"
    Write-Host ""
}

# Run main function
try {
    Install-ShopManager
} catch {
    Write-Host ""
    Write-ErrorMessage "Installation failed: $_"
    Write-Host ""
    Write-Host "For troubleshooting, see QUICKSTART.md" -ForegroundColor Yellow
    Write-Host ""
}

Read-Host "Press Enter to exit"
