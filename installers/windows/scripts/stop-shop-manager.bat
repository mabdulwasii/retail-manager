@echo off
REM ============================================================================
REM Shop Manager - Windows Stop Script
REM ============================================================================
REM This script stops all running Shop Manager instances
REM ============================================================================

echo =========================================
echo Shop Manager Stop Script
echo =========================================
echo.

REM Check if running as administrator
net session >nul 2>&1
if %errorLevel% == 0 (
    echo Running with administrator privileges
) else (
    echo Note: Not running as administrator
    echo Some operations may require elevation
)

echo.

REM Stop Shop Manager service
echo Checking for Shop Manager service...
sc query "Shop Manager" >nul 2>&1
if %errorLevel% == 0 (
    echo Found Shop Manager service
    echo Stopping service...
    net stop "Shop Manager" >nul 2>&1
    if %errorLevel% == 0 (
        echo [OK] Service stopped successfully
    ) else (
        echo [WARN] Service stop failed or already stopped
    )
    timeout /t 2 /nobreak >nul
) else (
    echo [INFO] No Shop Manager service found
)

echo.

REM Kill Shop Manager Java processes
echo Stopping Shop Manager Java processes...
taskkill /F /FI "WINDOWTITLE eq Shop Manager*" >nul 2>&1
if %errorLevel% == 0 (
    echo [OK] Stopped Shop Manager windows
)

taskkill /F /FI "IMAGENAME eq javaw.exe" /FI "MEMUSAGE gt 50000" >nul 2>&1
if %errorLevel% == 0 (
    echo [OK] Stopped Java processes
)

timeout /t 2 /nobreak >nul

echo.

REM Stop embedded PostgreSQL
echo Stopping embedded PostgreSQL...
taskkill /F /FI "IMAGENAME eq postgres.exe" >nul 2>&1
if %errorLevel% == 0 (
    echo [OK] Stopped PostgreSQL processes
)

taskkill /F /FI "WINDOWTITLE eq *postgres*" >nul 2>&1

timeout /t 1 /nobreak >nul

echo.

REM Clean up PID files
echo Cleaning up lock files...

if exist "%USERPROFILE%\.shopmanager\shop-manager.pid" (
    del /F "%USERPROFILE%\.shopmanager\shop-manager.pid" >nul 2>&1
    echo [OK] Removed shop-manager.pid
)

if exist "%USERPROFILE%\.shopmanager\data\postgres\postmaster.pid" (
    del /F "%USERPROFILE%\.shopmanager\data\postgres\postmaster.pid" >nul 2>&1
    echo [OK] Removed postmaster.pid
)

echo.

REM Check if ports are freed
echo Verifying ports are available...

netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>&1
if %errorLevel% == 0 (
    echo [WARN] Port 8081 is still in use
    netstat -ano | findstr ":8081" | findstr "LISTENING"
) else (
    echo [OK] Port 8081 is available
)

netstat -ano | findstr ":5433" | findstr "LISTENING" >nul 2>&1
if %errorLevel% == 0 (
    echo [WARN] Port 5433 is still in use
    netstat -ano | findstr ":5433" | findstr "LISTENING"
) else (
    echo [OK] Port 5433 is available
)

echo.
echo =========================================
echo Shop Manager stop completed
echo =========================================
echo.

pause
