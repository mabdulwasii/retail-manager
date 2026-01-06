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
    echo [WARN] Java not found in PATH, searching common locations...
    set "JAVA_CMD="

    REM Check Program Files
    for /d %%d in ("%ProgramFiles%\Java\jdk-*", "%ProgramFiles%\Eclipse Adoptium\jdk-*", "%ProgramFiles%\Temurin\jdk-*") do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_CMD=%%d\bin\java.exe"
            goto :java_found_console
        )
    )

    REM Check Program Files (x86)
    for /d %%d in ("%ProgramFiles(x86)%\Java\jdk-*", "%ProgramFiles(x86)%\Eclipse Adoptium\jdk-*") do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_CMD=%%d\bin\java.exe"
            goto :java_found_console
        )
    )

    REM Java not found
    echo.
    echo [ERROR] ========================================
    echo [ERROR] Java not found
    echo [ERROR] ========================================
    echo.
    echo [ERROR] Java 21 or higher is required to run Shop Manager.
    echo.
    echo [INFO] Please install Java from:
    echo [INFO] https://adoptium.net/temurin/releases/?version=21
    echo.
    echo [INFO] After installation:
    echo [INFO] 1. Add Java to your PATH environment variable
    echo [INFO] 2. Or run this launcher again
    echo.
    pause
    exit /b 1

    :java_found_console
    set "JAVA=!JAVA_CMD!"
    echo [INFO] Found Java at: !JAVA!
) else (
    set "JAVA=java"
)

REM Display Java version
echo [INFO] Checking Java version...
"%JAVA%" -version 2>&1 | findstr "version"
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

REM Find the JAR file (picks highest version if multiple exist)
set "JAR_FILE="
set "JAR_COUNT=0"

REM Count JAR files and find the latest version (sort descending)
for /f "delims=" %%f in ('dir /b /o-n "%APP_DIR%\lib\shop-manager-*-embedded.jar" 2^>nul') do (
    if not defined JAR_FILE (
        set "JAR_FILE=%APP_DIR%\lib\%%f"
    )
    set /a JAR_COUNT+=1
)

if not defined JAR_FILE (
    echo.
    echo ========================================================================
    echo [ERROR] Shop Manager JAR file not found
    echo ========================================================================
    echo.
    echo [ERROR] Expected location: %APP_DIR%\lib
    echo [ERROR] Expected pattern: shop-manager-*-embedded.jar
    echo.
    pause
    exit /b 1
)

REM Warn if multiple JARs found
if %JAR_COUNT% GTR 1 (
    echo.
    echo ========================================================================
    echo [WARNING] Multiple JAR files detected
    echo ========================================================================
    echo.
    echo [WARN] Found %JAR_COUNT% JAR files in %APP_DIR%\lib
    echo [WARN] Using latest version: %JAR_FILE%
    echo.
    echo [WARN] This may indicate an incomplete upgrade.
    echo [WARN] Consider reinstalling Shop Manager.
    echo.
)

echo [INFO] JAR File: %JAR_FILE%
echo [INFO] Starting Shop Manager...
echo [INFO] Press Ctrl+C to stop
echo ========================================================================
echo.

REM Launch application with console output
"%JAVA%" %JAVA_OPTS% ^
    -Dspring.profiles.active=embedded ^
    -Dserver.port=%BACKEND_PORT% ^
    -Dapplication.jwt.secret=%JWT_SECRET% ^
    -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED% ^
    -Dapplication.sync.cloud-endpoint=%CLOUD_API_URL% ^
    -Dapplication.sync.api-key=%CLOUD_API_KEY% ^
    -Dapplication.sync.store-id=%STORE_ID% ^
    -jar "%JAR_FILE%"

echo.
echo ========================================================================
echo Shop Manager stopped
echo ========================================================================
pause

endlocal
exit /b 0
