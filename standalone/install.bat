@echo off
REM ============================================================================
REM Shop Manager Standalone Installer for Windows
REM ============================================================================
REM
REM This script automates the installation of Shop Manager using Docker Compose
REM
REM Prerequisites:
REM   - Docker Desktop for Windows
REM   - Python 3.7+ (for configuration generation)
REM
REM Usage:
REM   install.bat
REM   install.bat --config custom-config.yaml
REM   install.bat --skip-config
REM
REM ============================================================================

setlocal enabledelayedexpansion

REM Configuration
set SCRIPT_DIR=%~dp0
set CONFIG_FILE=%SCRIPT_DIR%config.yaml
set SKIP_CONFIG_GENERATION=0

REM Parse arguments
:parse_args
if "%~1"=="" goto end_parse_args
if "%~1"=="--config" (
    set CONFIG_FILE=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="--skip-config" (
    set SKIP_CONFIG_GENERATION=1
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo Usage: install.bat [options]
    echo.
    echo Options:
    echo   --config FILE      Use custom config file (default: config.yaml)
    echo   --skip-config      Skip configuration generation
    echo   --help             Show this help message
    exit /b 0
)
echo Unknown option: %~1
echo Use --help for usage information
exit /b 1

:end_parse_args

REM ============================================================================
REM Welcome Message
REM ============================================================================

echo.
echo ========================================================================
echo   Shop Manager Standalone Installer
echo ========================================================================
echo.
echo Welcome to the Shop Manager installation wizard!
echo This installer will set up Shop Manager on your Windows machine.
echo.

REM ============================================================================
REM Step 1: Check Prerequisites
REM ============================================================================

echo ========================================================================
echo   Step 1/7: Checking Prerequisites
echo ========================================================================
echo.

REM Check Docker
echo [*] Checking for Docker...
where docker >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] Docker is not installed
    echo.
    echo Please install Docker Desktop for Windows first:
    echo   https://docs.docker.com/desktop/windows/install/
    echo.
    pause
    exit /b 1
)

REM Check if Docker is running
docker info >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] Docker daemon is not running
    echo.
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('docker --version') do set DOCKER_VERSION=%%i
echo [+] Docker is installed and running (%DOCKER_VERSION%)

REM Check Docker Compose
echo [*] Checking for Docker Compose...
docker compose version >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [X] Docker Compose is not available
    echo.
    echo Docker Compose v2 is required. Please update Docker Desktop.
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('docker compose version --short') do set COMPOSE_VERSION=%%i
echo [+] Docker Compose is available (v%COMPOSE_VERSION%)

REM Check Python
echo [*] Checking for Python...
where python >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [!] Python 3 is not installed
    echo [!] Configuration generation will be skipped
    set SKIP_CONFIG_GENERATION=1
) else (
    for /f "tokens=2" %%i in ('python --version 2^>^&1') do set PYTHON_VERSION=%%i
    echo [+] Python is installed (!PYTHON_VERSION!)

    REM Check for required Python packages
    echo [*] Checking Python dependencies...

    python -c "import yaml" >nul 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [*] Installing PyYAML...
        python -m pip install --quiet PyYAML
    )

    python -c "import jinja2" >nul 2>nul
    if %ERRORLEVEL% neq 0 (
        echo [*] Installing Jinja2...
        python -m pip install --quiet Jinja2
    )

    echo [+] Python dependencies are installed
)

REM ============================================================================
REM Step 2: Configuration Generation
REM ============================================================================

if %SKIP_CONFIG_GENERATION%==0 (
    echo.
    echo ========================================================================
    echo   Step 2/7: Generating Configuration
    echo ========================================================================
    echo.

    if not exist "%CONFIG_FILE%" (
        echo [X] Configuration file not found: %CONFIG_FILE%
        echo.
        echo Please create a config.yaml file or use --config to specify a custom file.
        pause
        exit /b 1
    )

    echo [*] Using configuration file: %CONFIG_FILE%

    REM Run configuration generator
    echo [*] Generating configuration files...

    cd /d "%SCRIPT_DIR%"
    python scripts\generate-config.py --config "%CONFIG_FILE%"
    if %ERRORLEVEL% neq 0 (
        echo [X] Configuration generation failed
        pause
        exit /b 1
    )

    echo [+] Configuration files generated successfully
) else (
    echo.
    echo ========================================================================
    echo   Step 2/7: Configuration (Skipped)
    echo ========================================================================
    echo.
    echo [*] Using existing configuration files
)

REM ============================================================================
REM Step 3: Copy Generated Files
REM ============================================================================

echo.
echo ========================================================================
echo   Step 3/7: Copying Configuration Files
echo ========================================================================
echo.

set GENERATED_DIR=%SCRIPT_DIR%generated

if not exist "%GENERATED_DIR%" (
    echo [X] Generated files directory not found: %GENERATED_DIR%
    echo Please run configuration generation first (without --skip-config)
    pause
    exit /b 1
)

REM Copy .env file
if exist "%GENERATED_DIR%\.env" (
    echo [*] Copying .env file...
    copy /Y "%GENERATED_DIR%\.env" "%SCRIPT_DIR%..\.env" >nul
    echo [+] .env file copied
) else (
    echo [!] .env file not found in generated directory
)

REM Copy Keycloak realm
if exist "%GENERATED_DIR%\keycloak-realm.json" (
    echo [*] Copying Keycloak realm configuration...
    if not exist "%SCRIPT_DIR%..\docker" mkdir "%SCRIPT_DIR%..\docker"
    copy /Y "%GENERATED_DIR%\keycloak-realm.json" "%SCRIPT_DIR%..\docker\keycloak-realm.json" >nul
    echo [+] Keycloak realm configuration copied
) else (
    echo [!] Keycloak realm file not found in generated directory
)

REM Copy Docker Compose override
if exist "%GENERATED_DIR%\docker-compose.override.yml" (
    echo [*] Copying Docker Compose override...
    copy /Y "%GENERATED_DIR%\docker-compose.override.yml" "%SCRIPT_DIR%..\docker-compose.override.yml" >nul
    echo [+] Docker Compose override copied
)

REM ============================================================================
REM Step 4: Pull Docker Images
REM ============================================================================

echo.
echo ========================================================================
echo   Step 4/7: Pulling Docker Images
echo ========================================================================
echo.
echo [*] This may take several minutes depending on your internet connection...

cd /d "%SCRIPT_DIR%.."

docker compose pull
if %ERRORLEVEL% neq 0 (
    echo [!] Some images could not be pulled, will try to build locally
) else (
    echo [+] Docker images pulled successfully
)

REM ============================================================================
REM Step 5: Build Custom Images
REM ============================================================================

echo.
echo ========================================================================
echo   Step 5/7: Building Application Images
echo ========================================================================
echo.

echo [*] Building backend and frontend images...

docker compose build
if %ERRORLEVEL% neq 0 (
    echo [X] Failed to build application images
    pause
    exit /b 1
)

echo [+] Application images built successfully

REM ============================================================================
REM Step 6: Start Services
REM ============================================================================

echo.
echo ========================================================================
echo   Step 6/7: Starting Services
echo ========================================================================
echo.

echo [*] Starting all services...
echo [*] This may take 2-3 minutes for all services to initialize...

docker compose up -d
if %ERRORLEVEL% neq 0 (
    echo [X] Failed to start services
    pause
    exit /b 1
)

echo [+] Services started successfully

REM Wait for services to initialize
echo [*] Waiting for services to become healthy...
timeout /t 10 /nobreak >nul

REM Check service status
echo.
echo [*] Service Status:
docker compose ps

REM ============================================================================
REM Step 7: Verification
REM ============================================================================

echo.
echo ========================================================================
echo   Step 7/7: Verification
echo ========================================================================
echo.

REM Check if key services are running
docker compose ps | findstr /C:"shop-manager-postgres" | findstr /C:"Up" >nul
if %ERRORLEVEL%==0 (
    echo [+] Database is running
) else (
    echo [!] Database may not be ready yet
)

docker compose ps | findstr /C:"shop-manager-keycloak" | findstr /C:"Up" >nul
if %ERRORLEVEL%==0 (
    echo [+] Keycloak is running
) else (
    echo [!] Keycloak may not be ready yet
)

docker compose ps | findstr /C:"shop-manager-backend" | findstr /C:"Up" >nul
if %ERRORLEVEL%==0 (
    echo [+] Backend is running
) else (
    echo [!] Backend may not be ready yet
)

docker compose ps | findstr /C:"shop-manager-frontend" | findstr /C:"Up" >nul
if %ERRORLEVEL%==0 (
    echo [+] Frontend is running
) else (
    echo [!] Frontend may not be ready yet
)

REM ============================================================================
REM Installation Complete
REM ============================================================================

echo.
echo ========================================================================
echo   Installation Complete!
echo ========================================================================
echo.

REM Read domain from .env if it exists
set DOMAIN=localhost
if exist "%SCRIPT_DIR%..\.env" (
    for /f "tokens=2 delims==" %%i in ('findstr /C:"DOMAIN=" "%SCRIPT_DIR%..\.env"') do set DOMAIN=%%i
)

echo Access URLs:
echo   Frontend:  http://%DOMAIN%:3001
echo   Backend:   http://%DOMAIN%:8081
echo   API Docs:  http://%DOMAIN%:8081/swagger-ui/index.html
echo   Keycloak:  http://%DOMAIN%:8080
echo.

echo Test User Credentials (if enabled):
echo   Email:     admin@shopmanager.com
echo   Password:  admin123
echo.

echo Useful Commands:
echo   View logs:       docker compose logs -f
echo   Stop services:   docker compose down
echo   Restart:         docker compose restart
echo   Update:          docker compose pull ^&^& docker compose up -d
echo.

echo Documentation:
echo   Installation guide:    docs\INSTALL.md
echo   Configuration guide:   docs\CUSTOMIZE.md
echo   Troubleshooting:       docs\TROUBLESHOOTING.md
echo.

REM Offer to open browser
set /p OPEN_BROWSER="Would you like to open the application in your browser now? (y/n): "
if /i "%OPEN_BROWSER%"=="y" (
    start http://%DOMAIN%:3001
)

echo.
echo [+] Installation completed successfully!
echo.
pause
