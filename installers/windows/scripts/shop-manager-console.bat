@echo off
REM ============================================================================
REM Shop Manager - Windows Console Launcher Script
REM ============================================================================
REM This script launches Shop Manager with console output for debugging
REM ============================================================================

setlocal enabledelayedexpansion

REM Get installation directory
set "APP_DIR=%~dp0"
set "APP_DIR=%APP_DIR:~0,-1%"

echo ========================================================================
echo Shop Manager - Embedded Edition
echo ========================================================================
echo.

REM Check Java installation
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo [ERROR] Please install Java 21 or higher from https://adoptium.net
    echo.
    pause
    exit /b 1
)

REM Display Java version
echo [INFO] Checking Java version...
java -version 2>&1 | findstr "version"
echo.

REM Load environment variables
if exist "%APP_DIR%\config\.env" (
    echo [INFO] Loading configuration from %APP_DIR%\config\.env
    for /f "usebackq tokens=1,* delims==" %%a in ("%APP_DIR%\config\.env") do (
        set "%%a=%%b"
    )
) else (
    echo [WARN] Configuration file not found, using defaults
)
echo.

REM Set defaults
if not defined BACKEND_PORT set BACKEND_PORT=8081
if not defined JAVA_OPTS set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC

REM Create data directories
echo [INFO] Creating data directories...
if not exist "%APP_DIR%\data\h2" mkdir "%APP_DIR%\data\h2"
if not exist "%APP_DIR%\data\uploads" mkdir "%APP_DIR%\data\uploads"
if not exist "%APP_DIR%\data\logs" mkdir "%APP_DIR%\data\logs"
echo.

REM Display configuration
echo [INFO] Configuration:
echo        Backend Port: %BACKEND_PORT%
echo        Java Options: %JAVA_OPTS%
echo        Data Directory: %APP_DIR%\data
echo        Cloud Sync: %CLOUD_SYNC_ENABLED%
echo.

echo [INFO] Starting Shop Manager...
echo [INFO] Press Ctrl+C to stop
echo ========================================================================
echo.

REM Launch application with console output
java %JAVA_OPTS% ^
    -Dspring.profiles.active=embedded ^
    -Dserver.port=%BACKEND_PORT% ^
    -Dapplication.jwt.secret=%JWT_SECRET% ^
    -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED% ^
    -Dapplication.sync.cloud-endpoint=%CLOUD_API_URL% ^
    -Dapplication.sync.api-key=%CLOUD_API_KEY% ^
    -Dapplication.sync.store-id=%STORE_ID% ^
    -jar "%APP_DIR%\lib\shop-manager-1.0.0-SNAPSHOT-embedded.jar"

echo.
echo ========================================================================
echo Shop Manager stopped
echo ========================================================================
pause

endlocal
exit /b 0
