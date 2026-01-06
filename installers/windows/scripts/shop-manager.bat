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
    for /f "usebackq tokens=1,* delims==" %%a in ("%APP_DIR%\config\.env") do (
        set "%%a=%%b"
    )
)

REM Set defaults if not configured
if not defined BACKEND_PORT set BACKEND_PORT=8081
if not defined JAVA_OPTS set JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC

REM Create data directories if they don't exist
if not exist "%APP_DIR%\data\h2" mkdir "%APP_DIR%\data\h2"
if not exist "%APP_DIR%\data\uploads" mkdir "%APP_DIR%\data\uploads"
if not exist "%APP_DIR%\data\logs" mkdir "%APP_DIR%\data\logs"

REM Set Spring profile
set SPRING_PROFILES_ACTIVE=embedded

REM Find the JAR file (supports any version)
for %%f in ("%APP_DIR%\lib\shop-manager-*-embedded.jar") do set "JAR_FILE=%%f"

if not exist "%JAR_FILE%" (
    echo ERROR: Shop Manager JAR file not found in %APP_DIR%\lib
    echo Expected: shop-manager-*-embedded.jar
    pause
    exit /b 1
)

echo Starting Shop Manager from: %JAR_FILE%

REM Determine javaw path (for windowless execution)
if "%JAVA%"=="java" (
    set "JAVAW=javaw"
) else (
    set "JAVAW=%JAVA:java.exe=javaw.exe%"
)

REM Launch application (no console window)
start "Shop Manager" /B "%JAVAW%" %JAVA_OPTS% ^
    -Dspring.profiles.active=embedded ^
    -Dserver.port=%BACKEND_PORT% ^
    -Dapplication.jwt.secret=%JWT_SECRET% ^
    -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED% ^
    -Dapplication.sync.cloud-endpoint=%CLOUD_API_URL% ^
    -Dapplication.sync.api-key=%CLOUD_API_KEY% ^
    -Dapplication.sync.store-id=%STORE_ID% ^
    -jar "%JAR_FILE%"

REM Wait a moment and check if application started
timeout /t 5 /nobreak >nul

REM Try to check health endpoint
curl -s http://localhost:%BACKEND_PORT%/actuator/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Shop Manager started successfully
    echo Access the application at http://localhost:%BACKEND_PORT%
) else (
    echo Shop Manager may be starting... Please check logs if issues persist
    echo Logs location: %APP_DIR%\data\logs
)

endlocal
exit /b 0
