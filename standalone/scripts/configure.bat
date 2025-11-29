@echo off
setlocal enabledelayedexpansion

REM Shop Manager - Interactive Configuration Wizard for Windows
REM This script helps clients configure Shop Manager with their business details

cls
echo ============================================
echo   Shop Manager - Configuration Wizard
echo ============================================
echo.
echo This wizard will help you configure Shop Manager
echo with your business details.
echo.
pause

cls
echo ============================================
echo   Step 1: Company Information
echo ============================================
echo.

REM Company Name
:company_name_prompt
set /p COMPANY_NAME="Company Name: "
if "%COMPANY_NAME%"=="" (
    echo Error: Company name cannot be empty
    goto company_name_prompt
)

REM Platform Name (optional)
set /p PLATFORM_NAME="Platform Name [%COMPANY_NAME% Retail Manager]: "
if "%PLATFORM_NAME%"=="" set PLATFORM_NAME=%COMPANY_NAME% Retail Manager

echo.
echo ============================================
echo   Step 2: Administrator Account
echo ============================================
echo.

REM Admin Email
:admin_email_prompt
set /p ADMIN_EMAIL="Administrator Email: "
if "%ADMIN_EMAIL%"=="" (
    echo Error: Email cannot be empty
    goto admin_email_prompt
)

REM Validate email format (basic check)
echo %ADMIN_EMAIL% | findstr /R "^[a-zA-Z0-9._%+-]*@[a-zA-Z0-9.-]*\.[a-zA-Z][a-zA-Z]*$" >nul
if errorlevel 1 (
    echo Error: Invalid email format
    goto admin_email_prompt
)

REM Admin Password
echo.
echo Password Requirements:
echo - Minimum 8 characters
echo - At least one uppercase letter
echo - At least one lowercase letter
echo - At least one digit
echo.

:admin_password_prompt
set /p ADMIN_PASSWORD="Administrator Password: "
if "%ADMIN_PASSWORD%"=="" (
    echo Error: Password cannot be empty
    goto admin_password_prompt
)

REM Check minimum length
set "PASSWORD_LENGTH=0"
set "temp=%ADMIN_PASSWORD%"
:password_length_loop
if defined temp (
    set /a PASSWORD_LENGTH+=1
    set "temp=%temp:~1%"
    goto password_length_loop
)

if %PASSWORD_LENGTH% LSS 8 (
    echo Error: Password must be at least 8 characters
    goto admin_password_prompt
)

REM Confirm password
set /p ADMIN_PASSWORD_CONFIRM="Confirm Password: "
if not "%ADMIN_PASSWORD%"=="%ADMIN_PASSWORD_CONFIRM%" (
    echo Error: Passwords do not match
    goto admin_password_prompt
)

echo.
echo ============================================
echo   Step 3: Business Settings
echo ============================================
echo.

REM Currency Selection
echo Select your business currency:
echo 1) USD - US Dollar
echo 2) EUR - Euro
echo 3) GBP - British Pound
echo 4) NGN - Nigerian Naira
echo.

:currency_prompt
set /p CURRENCY_CHOICE="Select currency (1-4): "

if "%CURRENCY_CHOICE%"=="1" (
    set CURRENCY=USD
    set CURRENCY_NAME=US Dollar
) else if "%CURRENCY_CHOICE%"=="2" (
    set CURRENCY=EUR
    set CURRENCY_NAME=Euro
) else if "%CURRENCY_CHOICE%"=="3" (
    set CURRENCY=GBP
    set CURRENCY_NAME=British Pound
) else if "%CURRENCY_CHOICE%"=="4" (
    set CURRENCY=NGN
    set CURRENCY_NAME=Nigerian Naira
) else (
    echo Error: Invalid choice
    goto currency_prompt
)

REM Optional: Domain (default to localhost)
echo.
set /p CUSTOM_DOMAIN="Custom domain [localhost]: "
if "%CUSTOM_DOMAIN%"=="" set CUSTOM_DOMAIN=localhost

echo.
echo ============================================
echo   Configuration Summary
echo ============================================
echo.
echo Company Name:     %COMPANY_NAME%
echo Platform Name:    %PLATFORM_NAME%
echo Admin Email:      %ADMIN_EMAIL%
echo Currency:         %CURRENCY_NAME% (%CURRENCY%)
echo Domain:           %CUSTOM_DOMAIN%
echo.
echo ============================================
echo.

set /p CONFIRM="Is this information correct? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo.
    echo Configuration cancelled. Please run this script again.
    pause
    exit /b 1
)

echo.
echo Generating configuration files...
echo.

REM Check if Python is available
python --version >nul 2>&1
if errorlevel 1 (
    echo Error: Python 3 is required but not found
    echo.
    echo Please install Python 3 from https://python.org
    echo Make sure to check "Add Python to PATH" during installation
    pause
    exit /b 1
)

REM Run Python config generator
cd /d "%~dp0"
python generate-client-config.py ^
  --company "%COMPANY_NAME%" ^
  --platform "%PLATFORM_NAME%" ^
  --email "%ADMIN_EMAIL%" ^
  --password "%ADMIN_PASSWORD%" ^
  --currency "%CURRENCY%" ^
  --domain "%CUSTOM_DOMAIN%"

if errorlevel 1 (
    echo.
    echo Error: Configuration generation failed
    pause
    exit /b 1
)

echo.
echo ============================================
echo   Configuration Complete!
echo ============================================
echo.
echo Your Shop Manager is now configured with:
echo   Company: %COMPANY_NAME%
echo   Email:   %ADMIN_EMAIL%
echo.
echo Next step: Run install.bat to install Shop Manager
echo.
pause

REM Ask if user wants to install now
set /p INSTALL_NOW="Would you like to install Shop Manager now? (Y/N): "
if /i "%INSTALL_NOW%"=="Y" (
    echo.
    echo Starting installation...
    cd /d "%~dp0\.."
    call install.bat
) else (
    echo.
    echo To install later, run: install.bat
    echo.
)

endlocal
