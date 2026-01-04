@echo off
REM ============================================================================
REM Shop Manager - Windows Service Installation Script
REM ============================================================================
REM This script installs Shop Manager as a Windows service using NSSM
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

REM Get installation directory
set "APP_DIR=%~dp0"
set "APP_DIR=%APP_DIR:~0,-1%"

echo ========================================================================
echo Shop Manager - Service Installation
echo ========================================================================
echo.

REM Check if service already exists
sc query "ShopManager" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [WARN] Service 'ShopManager' already exists
    echo [INFO] Stopping existing service...
    sc stop "ShopManager" >nul 2>&1
    timeout /t 3 /nobreak >nul
    echo [INFO] Removing existing service...
    sc delete "ShopManager" >nul 2>&1
    timeout /t 2 /nobreak >nul
)

REM Load environment variables
if exist "%APP_DIR%\config\.env" (
    for /f "usebackq tokens=1,* delims==" %%a in ("%APP_DIR%\config\.env") do (
        set "%%a=%%b"
    )
)

REM Set defaults
if not defined BACKEND_PORT set BACKEND_PORT=8081
if not defined JAVA_OPTS set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC

REM Find Java executable
for %%i in (java.exe) do set JAVA_EXE=%%~$PATH:i
if not defined JAVA_EXE (
    echo [ERROR] Java not found in PATH
    pause
    exit /b 1
)

echo [INFO] Java: %JAVA_EXE%
echo [INFO] Port: %BACKEND_PORT%
echo.

REM Create service using sc command
echo [INFO] Installing service...

set "JAR_FILE=%APP_DIR%\lib\shop-manager-1.0.0-SNAPSHOT-embedded.jar"
set "SERVICE_ARGS=%JAVA_OPTS% -Dspring.profiles.active=embedded -Dserver.port=%BACKEND_PORT% -Dapplication.jwt.secret=%JWT_SECRET% -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED% -jar \"%JAR_FILE%\""

sc create "ShopManager" binPath= "\"%JAVA_EXE%\" %SERVICE_ARGS%" DisplayName= "Shop Manager" start= auto

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Service installed successfully
    echo.
    echo [INFO] Starting service...
    sc start "ShopManager"

    if %ERRORLEVEL% EQU 0 (
        echo [SUCCESS] Service started successfully
        echo.
        echo Service Details:
        echo   Name: ShopManager
        echo   Display Name: Shop Manager
        echo   Startup Type: Automatic
        echo   Status: Running
        echo.
        echo Access the application at http://localhost:%BACKEND_PORT%
    ) else (
        echo [ERROR] Failed to start service
        echo [INFO] Check Event Viewer for error details
    )
) else (
    echo [ERROR] Failed to install service
)

echo.
echo ========================================================================
pause

endlocal
exit /b 0
