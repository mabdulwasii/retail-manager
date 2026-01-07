@echo off
REM ============================================================================
REM Shop Manager - Windows Launcher Script
REM ============================================================================
REM This script launches Shop Manager in GUI mode (no console window)
REM ============================================================================

setlocal enabledelayedexpansion

REM Get installation directory
set "APP_DIR=%~dp0"
set "APP_DIR=%APP_DIR:~0,-1%"

REM Check Java installation
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    REM Try to find Java in common installation locations
    set "JAVA_CMD="

    REM Check Program Files
    for /d %%d in ("%ProgramFiles%\Java\jdk-*", "%ProgramFiles%\Eclipse Adoptium\jdk-*", "%ProgramFiles%\Temurin\jdk-*") do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_CMD=%%d\bin\java.exe"
            goto :java_found
        )
    )

    REM Check Program Files (x86)
    for /d %%d in ("%ProgramFiles(x86)%\Java\jdk-*", "%ProgramFiles(x86)%\Eclipse Adoptium\jdk-*") do (
        if exist "%%d\bin\java.exe" (
            set "JAVA_CMD=%%d\bin\java.exe"
            goto :java_found
        )
    )

    REM Java not found anywhere
    echo.
    echo ========================================
    echo ERROR: Java not found
    echo ========================================
    echo.
    echo Java 21 or higher is required to run Shop Manager.
    echo.
    echo Please install Java from:
    echo https://adoptium.net/temurin/releases/?version=21
    echo.
    echo After installation, either:
    echo 1. Add Java to your PATH environment variable
    echo 2. Or run this installer again
    echo.
    pause
    exit /b 1

    :java_found
    set "JAVA=!JAVA_CMD!"
) else (
    set "JAVA=java"
)

REM Load environment variables
if exist "%APP_DIR%\config\.env" (
    REM Check if old format (missing POSTGRES_DATA_DIR)
    findstr /C:"POSTGRES_DATA_DIR" "%APP_DIR%\config\.env" >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo.
        echo ========================================
        echo Detected old configuration format
        echo ========================================
        echo.
        echo Migrating to new format with embedded PostgreSQL...
        echo.

        REM Backup old config
        copy "%APP_DIR%\config\.env" "%APP_DIR%\config\.env.backup" >nul 2>&1
        echo Backed up old config: config\.env.backup

        REM Copy new template
        copy /Y "%APP_DIR%\config\.env.template" "%APP_DIR%\config\.env" >nul
        echo Created new configuration with PostgreSQL settings

        echo.
        echo Configuration upgraded to v0.1.49 format
        echo NOTE: You will need to log in again with your credentials
        echo.
    )

    REM Load all environment variables
    for /f "usebackq tokens=1,* delims==" %%a in ("%APP_DIR%\config\.env") do (
        set "%%a=%%b"
    )

    REM Migrate specific old values
    set MIGRATED=false

    REM Fix old port (8081 → 80) - Use batch string replacement
    findstr /C:"BACKEND_PORT=8081" "%APP_DIR%\config\.env" >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "(Get-Content '%APP_DIR%\config\.env') -replace 'BACKEND_PORT=8081', 'BACKEND_PORT=80' | Set-Content '%APP_DIR%\config\.env'"
        echo Migrated BACKEND_PORT: 8081 to 80
        set MIGRATED=true
    )

    REM Fix old postgres path (./data/postgres → postgres)
    findstr /C:"POSTGRES_DATA_DIR=./data/postgres" "%APP_DIR%\config\.env" >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "(Get-Content '%APP_DIR%\config\.env') -replace 'POSTGRES_DATA_DIR=./data/postgres', 'POSTGRES_DATA_DIR=postgres' | Set-Content '%APP_DIR%\config\.env'"
        echo Migrated POSTGRES_DATA_DIR: ./data/postgres to postgres
        set MIGRATED=true
    )

    REM Fix unquoted cron expressions (causes app startup failure)
    findstr /R "^CLOUD_SYNC_CRON=[^^']" "%APP_DIR%\config\.env" >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "(Get-Content '%APP_DIR%\config\.env') -replace '^CLOUD_SYNC_CRON=(.*)$', 'CLOUD_SYNC_CRON=''$1''' | Set-Content '%APP_DIR%\config\.env'"
        echo Fixed CLOUD_SYNC_CRON quotes
        set MIGRATED=true
    )

    findstr /R "^UPDATE_CHECK_CRON=[^^']" "%APP_DIR%\config\.env" >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        powershell -Command "(Get-Content '%APP_DIR%\config\.env') -replace '^UPDATE_CHECK_CRON=(.*)$', 'UPDATE_CHECK_CRON=''$1''' | Set-Content '%APP_DIR%\config\.env'"
        echo Fixed UPDATE_CHECK_CRON quotes
        set MIGRATED=true
    )

    REM Add config version if missing
    findstr /C:"CONFIG_VERSION" "%APP_DIR%\config\.env" >nul 2>&1
    if %ERRORLEVEL% NEQ 0 (
        echo. >> "%APP_DIR%\config\.env"
        echo # Configuration Version >> "%APP_DIR%\config\.env"
        echo CONFIG_VERSION=1.0 >> "%APP_DIR%\config\.env"
        set MIGRATED=true
    )

    if "%MIGRATED%"=="true" (
        echo Configuration migrated to latest version
        REM Reload after migration
        for /f "usebackq tokens=1,* delims==" %%a in ("%APP_DIR%\config\.env") do (
            set "%%a=%%b"
        )
    )
)

REM Set defaults if not configured
if not defined BACKEND_PORT set BACKEND_PORT=80
if not defined JAVA_OPTS set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC

REM Create data directories if they don't exist
if not exist "%APP_DIR%\data\h2" mkdir "%APP_DIR%\data\h2"
if not exist "%APP_DIR%\data\uploads" mkdir "%APP_DIR%\data\uploads"
if not exist "%APP_DIR%\data\logs" mkdir "%APP_DIR%\data\logs"

REM Cleanup zombie postgres processes on port 5433
echo Checking for zombie postgres processes...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5433"') do (
    taskkill /F /PID %%a >nul 2>&1
)

REM Set Spring profile
set SPRING_PROFILES_ACTIVE=embedded

REM Find the JAR file (picks highest version if multiple exist)
set "JAR_FILE="
set "JAR_COUNT=0"
set "NEWEST_JAR="

REM Count JAR files and find the latest version (sort descending)
for /f "delims=" %%f in ('dir /b /o-n "%APP_DIR%\lib\shop-manager-*-embedded.jar" 2^>nul') do (
    if not defined NEWEST_JAR (
        set "NEWEST_JAR=%%f"
        set "JAR_FILE=%APP_DIR%\lib\%%f"
    )
    set /a JAR_COUNT+=1
)

if not defined JAR_FILE (
    echo.
    echo ========================================
    echo ERROR: Shop Manager JAR file not found
    echo ========================================
    echo.
    echo Expected location: %APP_DIR%\lib
    echo Expected pattern: shop-manager-*-embedded.jar
    echo.
    pause
    exit /b 1
)

REM Clean up old JAR files if multiple found
if %JAR_COUNT% GTR 1 (
    echo.
    echo ========================================
    echo Cleaning up old JAR files
    echo ========================================
    echo.
    echo Found %JAR_COUNT% JAR files, keeping only: !NEWEST_JAR!
    echo.

    REM Delete all JARs except the newest
    for /f "delims=" %%f in ('dir /b /o-n "%APP_DIR%\lib\shop-manager-*-embedded.jar" 2^>nul') do (
        if not "%%f"=="!NEWEST_JAR!" (
            echo Removing old JAR: %%f
            del /f /q "%APP_DIR%\lib\%%f" 2>nul
        )
    )

    echo.
    echo Cleanup complete
    echo.
    timeout /t 2 /nobreak >nul
)

echo Starting Shop Manager from: %JAR_FILE%

REM Verify JAVA variable is set
if not defined JAVA (
    echo.
    echo ========================================
    echo ERROR: JAVA variable not set
    echo ========================================
    echo.
    echo This is a critical error. Java detection failed.
    echo Please report this error with your system details.
    echo.
    pause
    exit /b 1
)

echo Using Java: !JAVA!

REM Determine javaw path using multi-strategy approach for maximum reliability
set "JAVAW="

REM Strategy 1: Use JAVA_HOME environment variable if set
if defined JAVA_HOME (
    set "JAVAW=%JAVA_HOME%\bin\javaw.exe"
    if exist "!JAVAW!" (
        echo Using javaw from JAVA_HOME: !JAVAW!
        goto :javaw_found
    )
)

REM Strategy 2: If JAVA variable contains full path, use same directory
if not "!JAVA!"=="java" (
    for %%i in ("!JAVA!") do set "JAVA_BIN_DIR=%%~dpi"
    set "JAVAW=!JAVA_BIN_DIR!javaw.exe"
    if exist "!JAVAW!" (
        echo Using javaw from JAVA directory: !JAVAW!
        goto :javaw_found
    )
)

REM Strategy 3: javaw not found - use console mode (visible window)
echo.
echo ============================================
echo WARNING: javaw.exe not found
echo ============================================
echo.
echo Shop Manager will start with a console window.
echo The console window will remain visible while the application runs.
echo.
echo To hide the window, ensure javaw.exe is in your PATH or set JAVA_HOME.
echo.
echo Starting Shop Manager...
echo.

REM Launch with console window visible
start "Shop Manager Console" "!JAVA!" %JAVA_OPTS% ^
    -Dspring.profiles.active=embedded ^
    -Dserver.port=%BACKEND_PORT% ^
    -Dapplication.jwt.secret=%JWT_SECRET% ^
    -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED% ^
    -Dapplication.sync.cloud-endpoint=%CLOUD_API_URL% ^
    -Dapplication.sync.api-key=%CLOUD_API_KEY% ^
    -Dapplication.sync.store-id=%STORE_ID% ^
    -jar "%JAR_FILE%"

goto :check_startup

:javaw_found
REM javaw.exe found and verified

REM Launch application (no console window)
start "Shop Manager" /B "!JAVAW!" %JAVA_OPTS% ^
    -Dspring.profiles.active=embedded ^
    -Dserver.port=%BACKEND_PORT% ^
    -Dapplication.jwt.secret=%JWT_SECRET% ^
    -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED% ^
    -Dapplication.sync.cloud-endpoint=%CLOUD_API_URL% ^
    -Dapplication.sync.api-key=%CLOUD_API_KEY% ^
    -Dapplication.sync.store-id=%STORE_ID% ^
    -jar "%JAR_FILE%"

:check_startup
REM Wait a moment and check if application started
timeout /t 5 /nobreak >nul

REM Construct application URL
if not defined CUSTOM_DOMAIN set CUSTOM_DOMAIN=localhost
if "%BACKEND_PORT%"=="80" (
    set "APP_URL=http://%CUSTOM_DOMAIN%"
) else (
    set "APP_URL=http://%CUSTOM_DOMAIN%:%BACKEND_PORT%"
)

REM Try to check health endpoint
curl -s "%APP_URL%/actuator/health" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Shop Manager started successfully
    echo ========================================
    echo.
    echo Access the application at: %APP_URL%
    echo.

    REM Open browser automatically
    start "" "%APP_URL%"
) else (
    echo.
    echo ========================================
    echo Shop Manager is starting...
    echo ========================================
    echo.
    echo Application URL: %APP_URL%
    echo Logs location: %APP_DIR%\data\logs
    echo.
    echo Please wait a moment and check the logs if issues persist
    echo.
)

endlocal
exit /b 0
