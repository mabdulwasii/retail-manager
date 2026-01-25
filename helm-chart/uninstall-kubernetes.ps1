# ============================================================================
# uninstall-kubernetes.ps1 - Shop Manager Kubernetes Uninstallation (Windows)
# ============================================================================
# This script removes Shop Manager from your Kubernetes cluster
# ============================================================================
#
# USAGE:
#   Right-click this file and select "Run with PowerShell"
#   OR open PowerShell in this directory and run:
#     .\uninstall-kubernetes.ps1
#
# ============================================================================

[CmdletBinding()]
param()

# Set error action preference
$ErrorActionPreference = "Stop"

# Configuration
$NAMESPACE = if ($env:NAMESPACE) { $env:NAMESPACE } else { "gomco" }
$RELEASE_NAME = if ($env:RELEASE_NAME) { $env:RELEASE_NAME } else { "retail" }

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

# Main uninstallation function
function Uninstall-ShopManager {
    Clear-Host
    Write-Header "Shop Manager - Kubernetes Uninstallation (Windows)"

    # Check prerequisites
    if (!(Test-Command "kubectl")) {
        Write-ErrorMessage "kubectl not found"
        Read-Host "Press Enter to exit"
        exit 1
    }

    if (!(Test-Command "helm")) {
        Write-ErrorMessage "helm not found"
        Read-Host "Press Enter to exit"
        exit 1
    }

    # Check cluster connectivity
    & {
        $ErrorActionPreference = 'Continue'
        kubectl cluster-info 2>&1 | Out-Null
    }
    if ($LASTEXITCODE -ne 0) {
        Write-ErrorMessage "Cannot connect to Kubernetes cluster"
        Write-Host ""
        Write-Host "Please check your kubeconfig and cluster connectivity." -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Info "Connected to Kubernetes cluster"
    Write-Host ""

    # Check if namespace exists
    & {
        $ErrorActionPreference = 'Continue'
        kubectl get namespace $NAMESPACE 2>&1 | Out-Null
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Namespace '$NAMESPACE' does not exist"
        Write-Host ""
        Write-Info "Shop Manager may not be installed or already uninstalled"
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 0
    }

    # Display current resources
    Write-Host "Current Shop Manager Resources:" -ForegroundColor Cyan
    Write-Host "============================================================================"
    kubectl get pods,svc,ingress -n $NAMESPACE 2>$null
    Write-Host ""

    # Confirm uninstallation
    Write-Warning "This will remove Shop Manager from your cluster!"
    Write-Host ""
    $confirm = Read-Host "Are you sure you want to uninstall? (y/N)"

    if ($confirm -notmatch '^[Yy]$') {
        Write-Info "Uninstallation cancelled"
        Read-Host "Press Enter to exit"
        exit 0
    }
    Write-Host ""

    # Step 1: Uninstall Helm release
    Write-Header "Step 1: Uninstalling Helm Release"

    # Check if helm release exists
    $releaseExists = & {
        $ErrorActionPreference = 'Continue'
        helm list -n $NAMESPACE 2>&1 | Select-String -Pattern $RELEASE_NAME -Quiet
    }

    if ($releaseExists) {
        helm uninstall $RELEASE_NAME -n $NAMESPACE
        if ($LASTEXITCODE -ne 0) {
            Write-ErrorMessage "Helm uninstall failed"
            Write-Host ""
            Write-Warning "Some resources may remain. Please check manually with: kubectl get all -n $NAMESPACE"
        } else {
            Write-Success "Helm release '$RELEASE_NAME' uninstalled"
        }
    } else {
        Write-Warning "Helm release '$RELEASE_NAME' not found"
    }
    Write-Host ""

    # Step 2: Ask about namespace deletion
    Write-Header "Step 2: Namespace Cleanup"
    Write-Warning "Delete namespace '$NAMESPACE' and all resources?"
    $deleteNs = Read-Host "This will remove ALL data. Continue? (y/N)"

    if ($deleteNs -match '^[Yy]$') {
        try {
            kubectl delete namespace $NAMESPACE
            if ($LASTEXITCODE -ne 0) {
                throw "Namespace deletion failed"
            }
            Write-Success "Namespace '$NAMESPACE' deleted"
        } catch {
            Write-ErrorMessage "Failed to delete namespace: $_"
        }
    } else {
        Write-Info "Namespace '$NAMESPACE' kept"
        Write-Warning "Some resources may still exist in this namespace"
    }
    Write-Host ""

    # Step 3: Ask about persistent volumes
    Write-Header "Step 3: Persistent Volumes"

    # Check if namespace still exists
    & {
        $ErrorActionPreference = 'Continue'
        kubectl get namespace $NAMESPACE 2>&1 | Out-Null
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Info "Namespace already deleted - skipping PVC cleanup"
    } else {
        Write-Host "Checking for persistent volume claims..."

        # Get PVCs using JSON output for accurate parsing
        $pvcJson = & {
            $ErrorActionPreference = 'Continue'
            kubectl get pvc -n $NAMESPACE -o json 2>&1
        }
        if ($LASTEXITCODE -eq 0) {
            try {
                $pvcData = $pvcJson | ConvertFrom-Json
                $pvcCount = ($pvcData.items | Measure-Object).Count

                if ($pvcCount -gt 0) {
                    Write-Warning "Found $pvcCount persistent volume claim(s) with data"
                    Write-Warning "These may contain database data and backups"
                    Write-Host ""
                    $deletePvc = Read-Host "Delete persistent volumes and ALL DATA? (y/N)"

                    if ($deletePvc -match '^[Yy]$') {
                        kubectl delete pvc --all -n $NAMESPACE 2>&1 | Out-Null
                        if ($LASTEXITCODE -eq 0) {
                            Write-Success "Persistent volumes deleted"
                        } else {
                            Write-Warning "Some PVCs may not have been deleted. Check manually."
                        }
                    } else {
                        Write-Info "Persistent volumes kept"
                        Write-Warning "You may need to manually delete PVCs later"
                    }
                } else {
                    Write-Info "No persistent volume claims found"
                }
            } catch {
                Write-Info "Could not parse PVC data - no PVCs or namespace deleted"
            }
        } else {
            Write-Info "No persistent volume claims found"
        }
    }
    Write-Host ""

    # Step 4: Completion
    Write-Header "[OK] Shop Manager Uninstallation Complete!"

    Write-Info "Remaining cluster resources:"
    Write-Host "  cert-manager (shared): kubectl get pods -n cert-manager"
    Write-Host "  ingress-nginx (shared): kubectl get pods -n ingress-nginx"
    Write-Host ""

    Write-Info "To completely remove Shop Manager infrastructure:"
    Write-Host "  kubectl delete namespace cert-manager"
    Write-Host "  kubectl delete namespace ingress-nginx"
    Write-Host ""

    Write-Warning "Note: Only delete cert-manager and ingress-nginx if no other"
    Write-Warning "applications are using them!"
    Write-Host ""

    Write-Success "Done!"
    Write-Host ""
}

# Run main function
try {
    Uninstall-ShopManager
} catch {
    Write-Host ""
    Write-ErrorMessage "Uninstallation failed: $_"
    Write-Host ""
}

Read-Host "Press Enter to exit"
