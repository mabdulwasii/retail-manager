@echo off
REM ============================================================================
REM setup-custom-domain.bat - Windows Launcher for Custom Domain Setup
REM ============================================================================
REM This batch file makes it easy to run the PowerShell custom domain script
REM Double-click this file to configure custom domain access
REM ============================================================================

echo.
echo ============================================================================
echo Shop Manager - Custom Domain Setup
echo ============================================================================
echo.
echo Starting PowerShell custom domain configuration script...
echo.

REM Run PowerShell script with bypass execution policy for this session only
powershell.exe -ExecutionPolicy Bypass -NoExit -File "%~dp0setup-custom-domain.ps1"

REM Note: -NoExit keeps the PowerShell window open after completion
REM The script itself also has pauses for user to review output
