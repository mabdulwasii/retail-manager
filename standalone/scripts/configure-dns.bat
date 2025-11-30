@echo off
REM ============================================================================
REM Shop Manager DNS Configuration Script for Windows
REM ============================================================================
REM
REM This script automatically configures DNS entries for Shop Manager
REM Must be run as Administrator
REM
REM Usage:
REM   configure-dns.bat --app-name myshop --domain shop.local
REM   configure-dns.bat --remove
REM
REM ============================================================================

setlocal enabledelayedexpansion

REM Configuration
set APP_NAME=
set DOMAIN=
set REMOVE=0
set HOSTS_FILE=%SystemRoot%\System32\drivers\etc\hosts
set BACKUP_FILE=%SystemRoot%\System32\drivers\etc\hosts.shopmanager.backup

REM Parse arguments
:parse_args
if "%~1"=="" goto end_parse_args
if "%~1"=="--app-name" (
    set APP_NAME=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--domain" (
    set DOMAIN=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--remove" (
    set REMOVE=1
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo Usage: configure-dns.bat [options]
    echo.
    echo Options:
    echo   --app-name NAME    Application name (e.g., myshop)
    echo   --domain DOMAIN    Base domain (e.g., shop.local)
    echo   --remove           Remove DNS entries
    echo   --help             Show this help message
    exit /b 0
)
echo Unknown option: %~1
exit /b 1

:end_parse_args

REM Check for Administrator privileges
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [X] This script must be run as Administrator
    echo.
    echo Please right-click and select "Run as Administrator"
    pause
    exit /b 1
)

REM Main execution
if %REMOVE%==1 (
    call :remove_dns_entries
) else (
    if "%APP_NAME%"=="" (
        echo [X] Application name is required
        echo.
        echo Usage: configure-dns.bat --app-name myshop --domain shop.local
        exit /b 1
    )
    if "%DOMAIN%"=="" (
        echo [X] Domain is required
        echo.
        echo Usage: configure-dns.bat --app-name myshop --domain shop.local
        exit /b 1
    )

    call :add_dns_entries
    call :flush_dns_cache
    call :show_usage_instructions
)

exit /b 0

REM ============================================================================
REM Functions
REM ============================================================================

:remove_dns_entries
echo.
echo ========================================================================
echo   Removing Shop Manager DNS Entries
echo ========================================================================
echo.

if not exist "%HOSTS_FILE%" (
    echo [!] Hosts file not found: %HOSTS_FILE%
    exit /b 1
)

REM Restore from backup if exists
if exist "%BACKUP_FILE%" (
    echo [*] Restoring from backup...
    copy /Y "%BACKUP_FILE%" "%HOSTS_FILE%" >nul
    del "%BACKUP_FILE%"
    echo [+] DNS entries removed and backup restored
) else (
    REM Remove Shop Manager entries manually
    echo [*] Removing Shop Manager entries...
    findstr /V /C:"# Shop Manager" "%HOSTS_FILE%" > "%HOSTS_FILE%.tmp"
    move /Y "%HOSTS_FILE%.tmp" "%HOSTS_FILE%" >nul
    echo [+] DNS entries removed
)

call :flush_dns_cache
exit /b 0

:add_dns_entries
echo.
echo ========================================================================
echo   Configuring DNS for Shop Manager
echo ========================================================================
echo.

REM Create full domain names
set FRONTEND_DOMAIN=%APP_NAME%.%DOMAIN%
set API_DOMAIN=api.%APP_NAME%.%DOMAIN%
set AUTH_DOMAIN=auth.%APP_NAME%.%DOMAIN%

echo [*] Configuring DNS for:
echo   Frontend: https://%FRONTEND_DOMAIN%
echo   API:      https://%API_DOMAIN%
echo   Auth:     https://%AUTH_DOMAIN%
echo.

REM Backup hosts file
if not exist "%BACKUP_FILE%" (
    echo [*] Creating backup of hosts file...
    copy "%HOSTS_FILE%" "%BACKUP_FILE%" >nul
    echo [+] Backup created: %BACKUP_FILE%
)

REM Remove old Shop Manager entries if they exist
findstr /C:"# Shop Manager" "%HOSTS_FILE%" >nul 2>&1
if %errorLevel%==0 (
    echo [*] Removing old Shop Manager entries...
    findstr /V /C:"# Shop Manager" "%HOSTS_FILE%" > "%HOSTS_FILE%.tmp"
    move /Y "%HOSTS_FILE%.tmp" "%HOSTS_FILE%" >nul
)

REM Add new entries
echo [*] Adding DNS entries to %HOSTS_FILE%...

echo. >> "%HOSTS_FILE%"
echo # Shop Manager - Auto-generated DNS entries >> "%HOSTS_FILE%"
echo # Generated on %DATE% %TIME% >> "%HOSTS_FILE%"
echo 127.0.0.1 %FRONTEND_DOMAIN%  # Shop Manager Frontend >> "%HOSTS_FILE%"
echo 127.0.0.1 %API_DOMAIN%       # Shop Manager API >> "%HOSTS_FILE%"
echo 127.0.0.1 %AUTH_DOMAIN%      # Shop Manager Auth (Keycloak) >> "%HOSTS_FILE%"

echo [+] DNS entries added successfully
exit /b 0

:flush_dns_cache
echo [*] Flushing DNS cache...
ipconfig /flushdns >nul 2>&1
echo [+] DNS cache flushed
exit /b 0

:show_usage_instructions
echo.
echo ========================================================================
echo   DNS Configuration Complete!
echo ========================================================================
echo.
echo [+] DNS entries configured successfully!
echo.
echo Access URLs:
echo   Frontend:  https://%FRONTEND_DOMAIN%
echo   API:       https://%API_DOMAIN%
echo   Auth:      https://%AUTH_DOMAIN%
echo.
echo [!] Important Notes:
echo   1. DNS entries point to 127.0.0.1 (localhost)
echo   2. Services must be running for URLs to work
echo   3. SSL certificates must be installed for HTTPS
echo.
echo To remove these entries:
echo   configure-dns.bat --remove
echo.
exit /b 0
