@echo off
REM ============================================================================
REM Shop Manager - Windows Service Uninstallation Script
REM ============================================================================
REM This script removes Shop Manager Windows service
REM ============================================================================

setlocal enabledelayedexpansion

REM Check administrator privileges
net session >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] This script requires administrator privileges
    echo [ERROR] Please run as administrator
    pause
    exit /b 1
)

echo ========================================================================
echo Shop Manager - Service Uninstallation
echo ========================================================================
echo.

REM Check if service exists
sc query "ShopManager" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] Service 'ShopManager' not found
    echo [INFO] Nothing to uninstall
    pause
    exit /b 0
)

echo [INFO] Stopping service...
sc stop "ShopManager" >nul 2>&1

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Service stopped
) else (
    echo [WARN] Service may not be running
)

REM Wait for service to stop
timeout /t 3 /nobreak >nul

echo [INFO] Removing service...
sc delete "ShopManager"

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Service removed successfully
) else (
    echo [ERROR] Failed to remove service
)

echo.
echo ========================================================================
pause

endlocal
exit /b 0
