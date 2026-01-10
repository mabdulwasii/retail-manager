# ============================================================================
# lite-init.ps1 - Docker Compose Lite Setup Script for Windows
# ============================================================================
# This script sets up the environment for running Shop Manager in lite mode
# ============================================================================
#
# USAGE:
#   Right-click this file and select "Run with PowerShell"
#   OR open PowerShell in this directory and run:
#     .\lite-init.ps1
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

# Generate a secure random secret
function New-SecureSecret {
    $bytes = New-Object Byte[] 48
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    return [Convert]::ToBase64String($bytes)
}

# Check prerequisites
function Test-Prerequisites {
    Write-Header "Checking Prerequisites"

    # Check PowerShell execution policy
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

    # Check Docker
    $dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerInstalled) {
        Write-ErrorMessage "Docker is not installed. Please install Docker Desktop from:"
        Write-Host "https://www.docker.com/products/docker-desktop/" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }
    Write-Success "Docker is installed"

    # Check Docker Compose
    try {
        docker compose version | Out-Null
        Write-Success "Docker Compose is available"
    } catch {
        Write-ErrorMessage "Docker Compose is not installed or not available."
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }

    # Check if Docker is running
    try {
        docker info | Out-Null
        Write-Success "Docker is running"
    } catch {
        Write-ErrorMessage "Docker is not running. Please start Docker Desktop."
        Write-Host ""
        Read-Host "Press Enter to exit"
        exit 1
    }

    Write-Host ""
}

# Create directory structure
function New-DirectoryStructure {
    Write-Header "Creating Directory Structure"

    $directories = @(
        ".\data",
        ".\data\uploads",
        ".\data\logs",
        ".\data\backups"
    )

    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Success "Created directory: $dir"
        } else {
            Write-Info "Directory already exists: $dir"
        }
    }

    Write-Success "Directory permissions set"
    Write-Host ""
}

# Create or update .env.lite file
function New-EnvFile {
    Write-Header "Creating Environment Configuration"

    if (Test-Path ".env.lite") {
        Write-Warning ".env.lite already exists. Creating backup..."
        $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
        Copy-Item .env.lite ".env.lite.backup.$timestamp"
        Write-Success "Backup created"
    }

    # Generate JWT secret
    $jwtSecret = New-SecureSecret

    # Read hostname for default custom domain
    $hostname = $env:COMPUTERNAME.ToLower()
    $defaultDomain = "shopmanager.local"

    $envContent = @"
# ============================================================================
# Shop Manager - Docker Compose Lite Configuration
# ============================================================================
# Generated on: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
# ============================================================================

# Application Version
APP_VERSION=1.0.0

# Domain Configuration
# Custom domain for professional URLs (e.g., shop.mystore.com, shopmanager.local)
# Leave as 'localhost' for local-only access
# Use .local domain for network discovery (requires Bonjour/mDNS)
CUSTOM_DOMAIN=$defaultDomain
SHOP_NAME=Shop Manager

# Port Configuration
BACKEND_PORT=8081
FRONTEND_PORT=3001

# Data Directory
DATA_DIR=./data

# PostgreSQL Database Configuration
POSTGRES_PASSWORD=shopmanager

# JWT Authentication (IMPORTANT: Keep this secret secure!)
JWT_SECRET=$jwtSecret

# Cloud Sync Configuration (Optional)
CLOUD_SYNC_ENABLED=false
CLOUD_API_URL=
CLOUD_API_KEY=
STORE_ID=
SYNC_CRON=0 0 * * * ?
ANONYMIZE_PII=false

# ============================================================================
# Advanced Configuration (Usually no need to change)
# ============================================================================

# JVM Memory Settings
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC
"@

    Set-Content -Path ".env.lite" -Value $envContent -Encoding UTF8
    Write-Success ".env.lite file created"
    Write-Warning "IMPORTANT: Keep your .env.lite file secure (contains JWT_SECRET)"
    Write-Host ""
}

# Load pre-built Docker images
function Import-DockerImages {
    Write-Header "Loading Docker Images"

    # Check if pre-built images exist
    $backendImage = Get-ChildItem -Path . -Filter "shop-manager-backend-lite-*.tar.gz" -ErrorAction SilentlyContinue | Select-Object -First 1
    $frontendImage = Get-ChildItem -Path . -Filter "shop-manager-frontend-lite-*.tar.gz" -ErrorAction SilentlyContinue | Select-Object -First 1

    if ($backendImage -and $frontendImage) {
        Write-Info "Found pre-built Docker images"

        # Load backend image
        Write-Info "Loading backend image: $($backendImage.Name)"
        Get-Content $backendImage.FullName -Raw | docker load

        # Load frontend image
        Write-Info "Loading frontend image: $($frontendImage.Name)"
        Get-Content $frontendImage.FullName -Raw | docker load

        Write-Success "Docker images loaded successfully"
    } else {
        Write-Warning "Pre-built Docker images not found"
        Write-Info "Skipping image loading - will pull from Docker Hub when starting"
    }
    Write-Host ""
}

# Verify configuration
function Test-Configuration {
    Write-Header "Verifying Configuration"

    if (-not (Test-Path ".env.lite")) {
        Write-ErrorMessage ".env.lite file not found"
        exit 1
    }
    Write-Success ".env.lite file found"

    if (-not (Test-Path "docker-compose-lite.yml")) {
        Write-ErrorMessage "docker-compose-lite.yml file not found"
        exit 1
    }
    Write-Success "docker-compose-lite.yml file found"

    if (-not (Test-Path "nginx-lite.conf")) {
        Write-ErrorMessage "nginx-lite.conf file not found"
        exit 1
    }
    Write-Success "nginx-lite.conf file found"

    Write-Host ""
}

# Display next steps
function Show-NextSteps {
    Write-Header "Setup Complete!"

    Write-Host ""
    Write-Host "Your Shop Manager Lite environment is ready!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. " -NoNewline -ForegroundColor White
    Write-Host "Review configuration:" -ForegroundColor Yellow
    Write-Host "   " -NoNewline
    Write-Host "notepad .env.lite" -ForegroundColor Blue
    Write-Host ""
    Write-Host "2. " -NoNewline -ForegroundColor White
    Write-Host "Start the application:" -ForegroundColor Yellow
    Write-Host "   " -NoNewline
    Write-Host "docker compose -f docker-compose-lite.yml --env-file .env.lite up -d" -ForegroundColor Blue
    Write-Host ""
    Write-Host "3. " -NoNewline -ForegroundColor White
    Write-Host "Access the application:" -ForegroundColor Yellow
    Write-Host "   Application: " -NoNewline
    Write-Host "http://shopmanager.local" -ForegroundColor Green
    Write-Host "   (or http://localhost if not using .local domain)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "4. " -NoNewline -ForegroundColor White
    Write-Host "Check logs:" -ForegroundColor Yellow
    Write-Host "   " -NoNewline
    Write-Host "docker compose -f docker-compose-lite.yml logs -f" -ForegroundColor Blue
    Write-Host ""
    Write-Host "5. " -NoNewline -ForegroundColor White
    Write-Host "Stop the application:" -ForegroundColor Yellow
    Write-Host "   " -NoNewline
    Write-Host "docker compose -f docker-compose-lite.yml down" -ForegroundColor Blue
    Write-Host ""
    Write-Host "For more information, see: " -NoNewline -ForegroundColor Yellow
    Write-Host "docs\DOCKER_LITE_WINDOWS_GUIDE.md" -ForegroundColor White
    Write-Host ""
    Write-Host ""
    Read-Host "Press Enter to exit"
}

# Main execution
function Main {
    Clear-Host
    Write-Header "Shop Manager - Docker Compose Lite Setup (Windows)"

    Test-Prerequisites
    New-DirectoryStructure
    New-EnvFile
    Import-DockerImages
    Test-Configuration
    Show-NextSteps
}

# Run main function
try {
    Main
} catch {
    Write-Host ""
    Write-ErrorMessage "Setup failed: $_"
    Write-Host ""
    Write-Host "For troubleshooting, see: docs\DOCKER_LITE_WINDOWS_GUIDE.md" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}
