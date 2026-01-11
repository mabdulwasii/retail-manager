# ============================================================================
# check-prerequisites.ps1 - Shop Manager Prerequisites Checker (Windows)
# ============================================================================
# This script validates that your system meets all requirements for
# installing Shop Manager on Kubernetes
# ============================================================================
#
# USAGE:
#   Right-click this file and select "Run with PowerShell"
#   OR open PowerShell in this directory and run:
#     .\check-prerequisites.ps1
#
# ============================================================================

[CmdletBinding()]
param()

# Set error action preference
$ErrorActionPreference = "Continue"

# Counters
$PASSED = 0
$FAILED = 0
$WARNINGS = 0

# Functions
function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "============================================================================" -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "============================================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Pass {
    param([string]$Message)
    Write-Host "[PASS] $Message" -ForegroundColor Green
    $script:PASSED++
}

function Write-Fail {
    param([string]$Message)
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    $script:FAILED++
}

function Write-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
    $script:WARNINGS++
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

# Function to compare versions
function Compare-Version {
    param(
        [string]$Version1,
        [string]$Version2
    )
    $v1 = [Version]($Version1 -replace '[^0-9.]', '')
    $v2 = [Version]($Version2 -replace '[^0-9.]', '')
    return $v1 -ge $v2
}

# Main check function
function Test-Prerequisites {
    Clear-Host
    Write-Header "Shop Manager - Prerequisites Checker (Windows)"
    Write-Host "Checking System Prerequisites..." -ForegroundColor Cyan
    Write-Host "============================================================================"
    Write-Host ""

    # 1. Check PowerShell execution policy
    Write-Info "Checking PowerShell execution policy..."
    $executionPolicy = Get-ExecutionPolicy
    if ($executionPolicy -ne "Restricted") {
        Write-Pass "PowerShell execution policy: $executionPolicy"
    } else {
        Write-Warn "PowerShell execution policy is Restricted"
        Write-Host "       Run: Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser"
    }
    Write-Host ""

    # 2. Check kubectl
    Write-Info "Checking kubectl..."
    if (Test-Command "kubectl") {
        try {
            $kubectlVersion = kubectl version --client -o json 2>$null | ConvertFrom-Json
            $version = $kubectlVersion.clientVersion.gitVersion -replace 'v', ''
            Write-Pass "kubectl is installed (version: $version)"

            # Check minimum version (1.24)
            if (Compare-Version $version "1.24.0") {
                Write-Pass "kubectl version meets minimum requirement (>=1.24)"
            } else {
                Write-Warn "kubectl version $version is below recommended 1.24"
            }
        } catch {
            Write-Pass "kubectl is installed (version check failed)"
        }
    } else {
        Write-Fail "kubectl is not installed"
        Write-Host "       Install: https://kubernetes.io/docs/tasks/tools/"
    }
    Write-Host ""

    # 3. Check Helm
    Write-Info "Checking Helm..."
    if (Test-Command "helm") {
        try {
            $helmVersion = helm version --short 2>$null
            if ($helmVersion -match 'v(\d+\.\d+\.\d+)') {
                $version = $Matches[1]
                Write-Pass "Helm is installed (version: $version)"

                # Check minimum version (3.10)
                if (Compare-Version $version "3.10.0") {
                    Write-Pass "Helm version meets minimum requirement (>=3.10)"
                } else {
                    Write-Warn "Helm version $version is below recommended 3.10"
                }
            } else {
                Write-Pass "Helm is installed (version check failed)"
            }
        } catch {
            Write-Pass "Helm is installed (version check failed)"
        }
    } else {
        Write-Fail "Helm is not installed"
        Write-Host "       Install: https://helm.sh/docs/intro/install/"
    }
    Write-Host ""

    # 4. Check Kubernetes cluster connectivity
    Write-Info "Checking Kubernetes cluster connectivity..."
    if (Test-Command "kubectl") {
        try {
            kubectl cluster-info 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Pass "Can connect to Kubernetes cluster"

                # Get cluster version
                try {
                    $serverVersion = kubectl version -o json 2>$null | ConvertFrom-Json
                    $version = $serverVersion.serverVersion.gitVersion -replace 'v', ''
                    Write-Pass "Cluster version: $version"

                    if (Compare-Version $version "1.24.0") {
                        Write-Pass "Cluster version meets minimum requirement (>=1.24)"
                    } else {
                        Write-Warn "Cluster version $version is below recommended 1.24"
                    }
                } catch {
                    Write-Info "Could not determine cluster version"
                }
            } else {
                Write-Fail "Cannot connect to Kubernetes cluster"
                Write-Host "       Configure: kubectl config view"
            }
        } catch {
            Write-Fail "Cannot connect to Kubernetes cluster"
            Write-Host "       Configure: kubectl config view"
        }
    } else {
        Write-Fail "kubectl not available (skipping cluster checks)"
    }
    Write-Host ""

    # 5. Check cluster resources
    Write-Info "Checking cluster resources..."
    if (Test-Command "kubectl") {
        try {
            kubectl cluster-info 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) {
                # Get node count
                $nodes = kubectl get nodes --no-headers 2>$null
                if ($nodes) {
                    $nodeCount = ($nodes | Measure-Object).Count
                    Write-Pass "Cluster has $nodeCount node(s)"

                    # Check for metrics-server
                    try {
                        $topNodes = kubectl top nodes 2>$null
                        if ($LASTEXITCODE -eq 0) {
                            Write-Pass "Metrics server is available"
                        } else {
                            Write-Warn "Metrics server not available (cannot check resource usage)"
                        }
                    } catch {
                        Write-Warn "Metrics server not available (cannot check resource usage)"
                    }

                    # Check allocatable resources
                    try {
                        $nodesJson = kubectl get nodes -o json 2>$null | ConvertFrom-Json
                        $totalCpu = 0
                        $totalMemGb = 0

                        foreach ($node in $nodesJson.items) {
                            $cpuStr = $node.status.allocatable.cpu
                            $memStr = $node.status.allocatable.memory

                            # Parse CPU (handle 'm' suffix for millicores)
                            if ($cpuStr -match '(\d+)m') {
                                $totalCpu += [int]$Matches[1] / 1000
                            } elseif ($cpuStr -match '(\d+)') {
                                $totalCpu += [int]$Matches[1]
                            }

                            # Parse memory (handle Ki suffix)
                            if ($memStr -match '(\d+)Ki') {
                                $totalMemGb += [int]$Matches[1] / 1024 / 1024
                            }
                        }

                        if ($totalCpu -ge 4) {
                            Write-Pass "Cluster has sufficient CPU ($([math]::Round($totalCpu, 1)) cores, minimum 4)"
                        } else {
                            Write-Warn "Cluster may not have sufficient CPU ($([math]::Round($totalCpu, 1)) cores, recommended 4+)"
                        }

                        if ($totalMemGb -ge 8) {
                            Write-Pass "Cluster has sufficient memory ($([math]::Round($totalMemGb, 1))GB, minimum 8GB)"
                        } else {
                            Write-Warn "Cluster may not have sufficient memory ($([math]::Round($totalMemGb, 1))GB, recommended 8GB+)"
                        }
                    } catch {
                        Write-Warn "Could not determine cluster resources"
                    }
                } else {
                    Write-Fail "No nodes found in cluster"
                }
            } else {
                Write-Warn "Cluster not accessible (skipping resource checks)"
            }
        } catch {
            Write-Warn "Cluster not accessible (skipping resource checks)"
        }
    } else {
        Write-Warn "kubectl not available (skipping resource checks)"
    }
    Write-Host ""

    # 6. Check internet connectivity
    Write-Info "Checking internet connectivity..."
    try {
        $response = Invoke-WebRequest -Uri "https://registry-1.docker.io/v2/" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 401) {
            Write-Pass "Can reach Docker Hub (registry-1.docker.io)"
        } else {
            Write-Warn "Unexpected response from Docker Hub: $($response.StatusCode)"
        }
    } catch {
        Write-Fail "Cannot reach Docker Hub"
        Write-Host "       Shop Manager requires internet access to pull images"
    }
    Write-Host ""

    # 7. Check for existing installations
    Write-Info "Checking for existing Shop Manager installations..."
    if (Test-Command "kubectl") {
        try {
            kubectl cluster-info 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) {
                kubectl get namespace gomco 2>$null | Out-Null
                if ($LASTEXITCODE -eq 0) {
                    Write-Warn "Namespace 'gomco' already exists"

                    if (Test-Command "helm") {
                        helm list -n gomco 2>$null | Select-String -Pattern "retail" -Quiet
                        if ($LASTEXITCODE -eq 0) {
                            Write-Warn "Shop Manager (retail release) is already installed"
                        }
                    }
                } else {
                    Write-Pass "No existing installation found (gomco namespace does not exist)"
                }
            } else {
                Write-Warn "Cannot check for existing installations (cluster not accessible)"
            }
        } catch {
            Write-Warn "Cannot check for existing installations (cluster not accessible)"
        }
    } else {
        Write-Warn "Cannot check for existing installations (kubectl not available)"
    }
    Write-Host ""

    # Summary
    Write-Header "Prerequisites Check Summary"
    Write-Host "Passed:   " -NoNewline
    Write-Host $PASSED -ForegroundColor Green
    Write-Host "Warnings: " -NoNewline
    Write-Host $WARNINGS -ForegroundColor Yellow
    Write-Host "Failed:   " -NoNewline
    Write-Host $FAILED -ForegroundColor Red
    Write-Host ""

    if ($FAILED -eq 0) {
        Write-Host "[OK] Your system meets the prerequisites for Shop Manager!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Next Steps:" -ForegroundColor Cyan
        Write-Host "  1. Edit values-template.yaml with your configuration"
        Write-Host "  2. Run: .\install-kubernetes.ps1"
        Write-Host ""
    } else {
        Write-Host "[FAIL] Your system does not meet some prerequisites" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please fix the failed checks above before installing Shop Manager"
        Write-Host ""
        Write-Host "For more information, see:"
        Write-Host "  - PREREQUISITES.md"
        Write-Host "  - QUICKSTART.md"
        Write-Host ""
    }

    if ($WARNINGS -gt 0) {
        Write-Host "Note: There are $WARNINGS warnings. Installation may still work," -ForegroundColor Yellow
        Write-Host "but you may experience issues or degraded performance." -ForegroundColor Yellow
        Write-Host ""
    }
}

# Run main function
try {
    Test-Prerequisites
} catch {
    Write-Host ""
    Write-Host "[ERROR] Prerequisites check failed: $_" -ForegroundColor Red
    Write-Host ""
}

Read-Host "Press Enter to exit"
