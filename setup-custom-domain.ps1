# ============================================================================
# Setup Custom Domain for Docker Lite (PowerShell - Windows)
# ============================================================================
# This script helps configure custom domain access for Shop Manager Docker Lite
# ============================================================================

# Set error action preference
$ErrorActionPreference = "Stop"

# Colors for output
$ColorRed = "Red"
$ColorGreen = "Green"
$ColorYellow = "Yellow"
$ColorBlue = "Cyan"

# Get custom domain from .env file
if (Test-Path .env) {
    $CustomDomain = (Get-Content .env | Select-String -Pattern "^CUSTOM_DOMAIN=" | ForEach-Object { $_ -replace "CUSTOM_DOMAIN=", "" }).Trim()
} else {
    Write-Host "Error: .env file not found" -ForegroundColor $ColorRed
    Write-Host "Please create .env file from .env.lite template:" -ForegroundColor $ColorRed
    Write-Host "  Copy-Item .env.lite .env" -ForegroundColor $ColorYellow
    exit 1
}

if ([string]::IsNullOrEmpty($CustomDomain)) {
    Write-Host "Error: CUSTOM_DOMAIN not set in .env file" -ForegroundColor $ColorRed
    exit 1
}

Write-Host "========================================" -ForegroundColor $ColorBlue
Write-Host "Shop Manager - Custom Domain Setup" -ForegroundColor $ColorBlue
Write-Host "========================================" -ForegroundColor $ColorBlue
Write-Host ""
Write-Host "Custom Domain: " -NoNewline
Write-Host "$CustomDomain" -ForegroundColor $ColorGreen
Write-Host ""

# Check if domain is localhost (no hosts file needed)
if ($CustomDomain -eq "localhost") {
    Write-Host "✓ Using localhost - no hosts file configuration needed" -ForegroundColor $ColorGreen
    Write-Host ""
    Write-Host "Access your application at:"
    Write-Host "  Frontend: " -NoNewline
    Write-Host "http://localhost/" -ForegroundColor $ColorBlue
    Write-Host "  Backend API: " -NoNewline
    Write-Host "http://localhost/api" -ForegroundColor $ColorBlue
    exit 0
}

# Hosts file path
$HostsFile = "$env:SystemRoot\System32\drivers\etc\hosts"

Write-Host "Hosts file: " -NoNewline
Write-Host "$HostsFile" -ForegroundColor $ColorYellow
Write-Host ""

# Check if entry already exists
$HostsContent = Get-Content $HostsFile -ErrorAction SilentlyContinue
if ($HostsContent | Select-String -Pattern $CustomDomain -Quiet) {
    Write-Host "✓ Domain already configured in hosts file" -ForegroundColor $ColorGreen
    $HostsContent | Select-String -Pattern $CustomDomain
} else {
    Write-Host "⚠ Domain not found in hosts file" -ForegroundColor $ColorYellow
    Write-Host ""
    Write-Host "To access your application via custom domain, add this line to $HostsFile" + ":"
    Write-Host ""
    Write-Host "127.0.0.1   $CustomDomain" -ForegroundColor $ColorGreen
    Write-Host ""

    # Offer to add automatically (requires admin)
    $AddAutomatically = Read-Host "Would you like to add it automatically? (requires Administrator privileges) [y/N]"

    if ($AddAutomatically -match "^[Yy]$") {
        # Check if running as administrator
        $IsAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

        if (-not $IsAdmin) {
            Write-Host ""
            Write-Host "Error: This script must be run as Administrator to modify the hosts file" -ForegroundColor $ColorRed
            Write-Host ""
            Write-Host "Please:" -ForegroundColor $ColorYellow
            Write-Host "1. Right-click PowerShell and select 'Run as Administrator'" -ForegroundColor $ColorYellow
            Write-Host "2. Navigate to the project directory" -ForegroundColor $ColorYellow
            Write-Host "3. Run: .\setup-custom-domain.ps1" -ForegroundColor $ColorYellow
            exit 1
        }

        try {
            # Add entry to hosts file
            Add-Content -Path $HostsFile -Value "127.0.0.1   $CustomDomain"
            Write-Host "✓ Domain added to hosts file" -ForegroundColor $ColorGreen

            # Flush DNS cache
            Write-Host "Flushing DNS cache..." -ForegroundColor $ColorYellow
            ipconfig /flushdns | Out-Null
            Write-Host "✓ DNS cache flushed" -ForegroundColor $ColorGreen
        } catch {
            Write-Host "Error adding entry to hosts file: $_" -ForegroundColor $ColorRed
            exit 1
        }
    } else {
        Write-Host ""
        Write-Host "Manual setup required:" -ForegroundColor $ColorYellow
        Write-Host ""
        Write-Host "1. Open Notepad as Administrator" -ForegroundColor $ColorYellow
        Write-Host "   (Right-click Notepad → Run as Administrator)"
        Write-Host ""
        Write-Host "2. Open file: " -NoNewline -ForegroundColor $ColorYellow
        Write-Host "$HostsFile"
        Write-Host ""
        Write-Host "3. Add this line:" -ForegroundColor $ColorYellow
        Write-Host "   127.0.0.1   $CustomDomain"
        Write-Host ""
        Write-Host "4. Save the file"
        Write-Host ""
        Write-Host "5. Flush DNS cache:" -ForegroundColor $ColorYellow
        Write-Host "   ipconfig /flushdns"
        Write-Host ""
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor $ColorBlue
Write-Host "Access Your Application" -ForegroundColor $ColorBlue
Write-Host "========================================" -ForegroundColor $ColorBlue
Write-Host ""
Write-Host "  Frontend: " -NoNewline
Write-Host "http://$CustomDomain/" -ForegroundColor $ColorGreen
Write-Host "  Backend API: " -NoNewline
Write-Host "http://$CustomDomain/api" -ForegroundColor $ColorGreen
Write-Host "  Health Check: " -NoNewline
Write-Host "http://$CustomDomain/actuator/health" -ForegroundColor $ColorGreen
Write-Host ""
Write-Host "Default Credentials:" -ForegroundColor $ColorBlue
Write-Host "  System Admin: " -NoNewline
Write-Host "superadmin" -ForegroundColor $ColorYellow -NoNewline
Write-Host " / " -NoNewline
Write-Host "changeme" -ForegroundColor $ColorYellow
Write-Host "  Tenant Admin: " -NoNewline
Write-Host "admin" -ForegroundColor $ColorYellow -NoNewline
Write-Host " / " -NoNewline
Write-Host "admin123" -ForegroundColor $ColorYellow
Write-Host ""
Write-Host "⚠ IMPORTANT: Change default passwords after first login!" -ForegroundColor $ColorRed
Write-Host ""
Write-Host "For more information, see DOCKER_LITE_CUSTOM_DOMAIN.md"
Write-Host ""
