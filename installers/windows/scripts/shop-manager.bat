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
if not "%JAVA%"=="java" (
    for %%i in ("%JAVA%") do set "JAVA_BIN_DIR=%%~dpi"
    set "JAVAW=!JAVA_BIN_DIR!javaw.exe"
    if exist "!JAVAW!" (
        echo Using javaw from JAVA directory: !JAVAW!
        goto :javaw_found
    )
)

REM Strategy 3: javaw not found - use VBScript wrapper to launch java hidden
echo.
echo Warning: javaw.exe not found, using VBScript wrapper for hidden launch
echo.

REM Create VBScript launcher with proper quote escaping
set "VBS_LAUNCHER=%TEMP%\shop-manager-launch.vbs"
(
echo Set WshShell = CreateObject^("WScript.Shell"^)
echo Dim cmd
echo cmd = "java %JAVA_OPTS% -Dspring.profiles.active=embedded"
echo cmd = cmd ^& " -Dserver.port=%BACKEND_PORT%"
echo cmd = cmd ^& " -Dapplication.jwt.secret=%JWT_SECRET%"
echo cmd = cmd ^& " -Dapplication.sync.enabled=%CLOUD_SYNC_ENABLED%"
echo cmd = cmd ^& " -Dapplication.sync.cloud-endpoint=%CLOUD_API_URL%"
echo cmd = cmd ^& " -Dapplication.sync.api-key=%CLOUD_API_KEY%"
echo cmd = cmd ^& " -Dapplication.sync.store-id=%STORE_ID%"
echo cmd = cmd ^& " -jar ""%JAR_FILE%"""
echo WshShell.Run cmd, 0, False
) > "%VBS_LAUNCHER%"

REM Launch using VBScript (window hidden)
cscript //nologo "%VBS_LAUNCHER%"

REM Wait and check if application started
timeout /t 5 /nobreak >nul
curl -s http://localhost:%BACKEND_PORT%/actuator/health >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Shop Manager started successfully
    echo Access the application at http://localhost:%BACKEND_PORT%
) else (
    echo Shop Manager may be starting... Please check logs if issues persist
    echo Logs location: %APP_DIR%\data\logs
)

REM Cleanup and exit
del "%VBS_LAUNCHER%" 2>nul
endlocal
exit /b 0

:javaw_found
REM javaw.exe found and verified

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
